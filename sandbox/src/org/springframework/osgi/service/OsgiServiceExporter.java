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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.osgi.context.BundleContextAware;

/**
 * A bean that transparently publishes other beans in the same
 * application context as OSGi services.
 * 
 * The service properties used when publishing the service are
 * determined by the OsgiServicePropertiesResolver. The default
 * implementation uses
 * <ul>
 *   <li>BundleSymbolicName=&lt;bundle symbolic name&gt;</li>
 *   <li>BundleVersion=&lt;bundle version&gt;</li>
 *   <li>org.springframework.osgi.beanname="&lt;bean name&gt;</li>
 * </ul>
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class OsgiServiceExporter implements BeanFactoryAware, BeanNameAware, InitializingBean, DisposableBean, BundleContextAware {

	private Log log = LogFactory.getLog(OsgiServiceExporter.class);
	
	private BundleContext bundleContext;
	private OsgiServicePropertiesResolver resolver = new BeanNameServicePropertiesResolver();
	private BeanFactory beanFactory;
	private List/*<String>*/ exportBeans = Collections.EMPTY_LIST;
	private Set/*<ServiceRegistration>*/ publishedServices = new HashSet();
	private String beanName = OsgiServiceExporter.class.getName();
	
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	public void setExportBeans(List /*<String>*/ beanNames) {
		if (beanNames == null) {
			this.exportBeans = Collections.EMPTY_LIST;
		}
		else {
			this.exportBeans = beanNames;
			for (Iterator iter = beanNames.iterator(); iter.hasNext();) {
				Object thisName = iter.next();
				if (! (thisName instanceof String)) {
					throw new IllegalArgumentException(
							"The exportBeans property requires a list of bean names as strings, " +
							"but the list contained an element of type '" + thisName.getClass().getName() +
							"'");
				}
				
			}
		}
	}

	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;
	}

	/**
	 * @return Returns the resolver.
	 */
	public OsgiServicePropertiesResolver getResolver() {
		return this.resolver;
	}

	/**
	 * @param resolver The resolver to set.
	 */
	public void setResolver(OsgiServicePropertiesResolver resolver) {
		this.resolver = resolver;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		this.beanName = name;		
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.beanFactory == null) {
			throw new IllegalArgumentException("Required property beanFactory has not been set");
		}
		if (this.bundleContext == null) {
			throw new IllegalArgumentException("Required property bundleContext has not been set");
		}
		if (this.resolver == null) {
			throw new IllegalArgumentException("Required property resolver was set to a null value");
		}
		if (this.exportBeans.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("OsgiServiceExporter '" + this.beanName + "' has no beans to export." +
						 "Use the exportBeans property to specify the list of bean names that should be exported.");
			}
		}
		publishBeans();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		for (Iterator iter = this.publishedServices.iterator(); iter.hasNext();) {
			ServiceRegistration sReg = (ServiceRegistration) iter.next();
			sReg.unregister();
		}
	}

	
	private void publishBeans() throws NoSuchBeanDefinitionException {
		for (Iterator iter = this.exportBeans.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			Object bean = this.beanFactory.getBean(name);
			publishBeanAsService(bean, this.resolver.getServiceProperties(bean, name));			
		}
	}
	
	protected void publishBeanAsService(Object bean, Properties serviceProperties) {
		// TODO : what type should we publish the service as???
		ServiceRegistration sReg = this.bundleContext.registerService(bean.getClass().getName(), bean, serviceProperties);
		this.publishedServices.add(sReg);
	}

}
