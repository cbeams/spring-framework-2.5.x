/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 23-Jan-2006 by Adrian Colyer
 */
package org.springframework.osgi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.util.StringUtils;

/**
 * Factory bean for OSGi services. Returns a proxy implementing the service
 * interface. This allows Spring to manage service lifecycle events
 * (such as the bundle providing the service being stopped and restarted) 
 * and transparently rebind to a new service instance if one is available.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class OsgiServiceProxyFactoryBean implements FactoryBean,
		InitializingBean, DisposableBean, BundleContextAware, ApplicationContextAware {

	public static final long DEFAULT_MILLIS_BETWEEN_RETRIES = 1000;
	public static final int DEFAULT_MAX_RETRIES = 3;

	/**
	 * Logger, available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());
	private BundleContext bundleContext;
	private ServiceReference serviceReference;
	private boolean retryOnUnregisteredService = true;
	private int maxRetries = DEFAULT_MAX_RETRIES;
	private long millisBetweenRetries = DEFAULT_MILLIS_BETWEEN_RETRIES;
	
	// not required to be an interface, but usually should be...
	private Class serviceType;
	
	// filter used to narrow service matches, may be null
	private String filter;

	// if looking for a bean published as a service, this is the name we're after
	private String beanName;
	
	// reference to our app context (we need the classloader for proxying...)
	private ApplicationContext applicationContext;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		// try to find the service
		String lookupFilter = getFilterStringForServiceLookup();
		this.serviceReference = OsgiServiceUtils.getService(this.bundleContext, getServiceType(), lookupFilter);
		Object target = this.bundleContext.getService(this.serviceReference);
		return getServiceProxyFor(target,lookupFilter);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return getServiceType();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws IllegalArgumentException {
		if (this.bundleContext == null) {
			throw new IllegalArgumentException("Required bundleContext property was not set");
		}
		if (getServiceType() == null) {
			throw new IllegalArgumentException("Required serviceType property was not set");
		}
		if (getFilter() != null) {
			// this call forces parsing of the filter string to generate an exception right
			// now if it is not well-formed
			try {
				FrameworkUtil.createFilter(getFilterStringForServiceLookup());
			} 
			catch (InvalidSyntaxException ex) {
				throw new IllegalArgumentException(
						"Filter string '" + getFilter() +
						"' set on OsgiServiceProxyFactoryBean has invalid syntax: " + 
						ex.getMessage(),ex);
			}
		}
		if (this.applicationContext == null) {
			throw new IllegalArgumentException("Required applicationContext property was not set");			
		}
		if (! (this.applicationContext instanceof DefaultResourceLoader)) {
			throw new IllegalArgumentException(
					"ApplicationContext does not provide access to classloader, " +
					"provided type was : '" + this.applicationContext.getClass().getName() + 
					"' which does not extend DefaultResourceLoader");
		}
	}

	/**
	 * @return Returns the serviceType.
	 */
	public Class getServiceType() {
		return this.serviceType;
	}

	/**
	 * The type that the OSGi service was registered with
	 */
	public void setServiceType(Class serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * @return Returns the filter.
	 */
	public String getFilter() {
		return this.filter;
	}

	/**
	 * An OSGi filter used to narrow service matches. If you
	 * just want to find a spring bean published as a service by
	 * the OsgiServiceExporter, use the beanName property instead.
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * @return Returns the beanName.
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * To find a bean published as a service by the OsgiServiceExporter,
	 * simply set this property. You may specify additional filtering 
	 * criteria if needed (using the filter property) but this is not
	 * required.
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.osgi.context.BundleContextAware#setBundleContext(org.osgi.framework.BundleContext)
	 */
	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * If the target OSGi service is unregistered, should we attempt to rebind
	 * to a replacement? (For example, if the bundle providing the service is 
	 * stopped and then subsequently started again).
	 * 
	 * By default retry *will* be attempted.
	 *  
	 * Changing this property after initialization is complete has no effect.
	 * 
	 * @param retryOnUnregisteredService The retryOnUnregisteredService to set.
	 */
	public void setRetryOnUnregisteredService(boolean retryOnUnregisteredService) {
		this.retryOnUnregisteredService = retryOnUnregisteredService;
	}

	/**
	 * How many times should we attempt to rebind to a target service if the
	 * service we are currently using is unregistered. Default is 3 times.
	 * 
	 * Changing this property after initialization is complete has no effect.
	 * 
	 * @param maxRetries The maxRetries to set.
	 */
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	/**
	 * How long should we wait between failed attempts at rebinding to a service
	 * that has been unregistered.
	 *  
	 * Changing this property after initialization is complete has no effect.
	 * 
	 * @param millisBetweenRetries The millisBetweenRetries to set.
	 */
	public void setMillisBetweenRetries(long millisBetweenRetries) {
		this.millisBetweenRetries = millisBetweenRetries;
	}
	
	// this is as nasty as dynamic sql generation. 
	// build an osgi filter string to find the service we are
	// looking for.
	private String getFilterStringForServiceLookup() {
		StringBuffer sb = new StringBuffer();
		boolean andFilterWithBeanName = ((getFilter() != null) && (getBeanName() != null)); 
		if (andFilterWithBeanName) {
			sb.append("(&");
		}
		if (getFilter() != null) {
			sb.append(getFilter());
		}
		if (getBeanName() != null) {
			sb.append("(");
			sb.append(BeanNameServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
			sb.append("=");
			sb.append(getBeanName());
			sb.append(")");
		}
		if (andFilterWithBeanName) {
			sb.append(")");
		}
		String filter = sb.toString();
		if (StringUtils.hasText(filter)) {
			return filter;
		} 
		else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		if (this.serviceReference != null) {
			this.bundleContext.ungetService(this.serviceReference);
		}
	}
	
	/**
	 * We proxy the actual service so that we can listen to service events
	 * and rebind transparently if the service goes down and comes back up
	 * for example
	 */
	private Object getServiceProxyFor(Object target, String lookupFilter) {
		ProxyFactory pf = new ProxyFactory();
		if (getServiceType().isInterface()) {
			pf.setInterfaces(new Class[] {getServiceType()});
		}
		HotSwappableTargetSource targetSource = new HotSwappableTargetSource(target);
		pf.setTargetSource(targetSource);
		OsgiServiceInterceptor interceptor = new OsgiServiceInterceptor(this.bundleContext,this.serviceReference,targetSource,getServiceType(),lookupFilter);
		interceptor.setMaxRetries(this.retryOnUnregisteredService ? this.maxRetries : 0);
		interceptor.setRetryIntervalMillis(this.millisBetweenRetries);
		pf.addAdvice(interceptor);
		ClassLoader classLoader = ((DefaultResourceLoader)this.applicationContext).getClassLoader();
		return pf.getProxy(classLoader);
	}

}
