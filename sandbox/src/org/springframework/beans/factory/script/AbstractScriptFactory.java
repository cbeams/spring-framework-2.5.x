/*
 * Copyright 2002-2004 the original author or authors.
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
 */

package org.springframework.beans.factory.script;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.dynamic.AutoRefreshDynamicBeanTargetSource;
import org.springframework.beans.factory.dynamic.DynamicBeanTargetSource;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AbstractScriptFactory.java,v 1.2 2004-08-04 16:49:48 johnsonr Exp $
 */
public abstract class AbstractScriptFactory implements ScriptContext, ApplicationContextAware, BeanFactoryAware,
		BeanPostProcessor {

	private ResourceLoader resourceLoader;

	private int expirySeconds;

	protected final Log log = LogFactory.getLog(getClass());

	/** Location to Script */
	private Map scripts = new HashMap();

	/**
	 * Object to Script: need to bound TODO
	 */
	private Map objectMap = new HashMap();

	private ConfigurableListableBeanFactory beanFactory;

	public void setApplicationContext(ApplicationContext ac) {
		this.resourceLoader = ac;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	/**
	 * Alternative to ApplicationContextAware
	 * 
	 * @param resourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * @return Returns the defaultPollIntervalSeconds.
	 */
	public int getPollIntervalSeconds() {
		return expirySeconds;
	}

	/**
	 * @param defaultPollIntervalSeconds
	 *            The defaultPollIntervalSeconds to set.
	 */
	public void setExpirySeconds(int defaultPollIntervalSeconds) {
		this.expirySeconds = defaultPollIntervalSeconds;
	}

	public Object create(String className, String[] interfaceNames) throws BeansException {
		Script script = configuredScript(className, interfaceNames);
		Object o = script.createObject();
		objectMap.put(o, script);
		return o;
	}

	public Object create(String className) throws BeansException {
		return create(className, new String[0]);
	}

	protected Script configuredScript(String location, String[] interfaceNames) throws BeansException {

		Script script = (Script) scripts.get(location);
		if (script == null) {
			script = createScript(location);

			// Add interfaces
			try {
				Class[] interfaces = AopUtils.toInterfaceArray(interfaceNames);
				for (int i = 0; i < interfaces.length; i++) {
					// TODO what loader
					script.addInterface(interfaces[i]);
				}
				return script;
			}
			catch (ClassNotFoundException ex) {
				throw new ScriptException("No interface found", ex) {
				};
			}
		}
		return script;
	}

	/**
	 * Subclasses must implement this, with knowledge about specific Scripts
	 */
	protected abstract Script createScript(String location) throws BeansException;

	
	protected boolean isDynamicScript(BeanDefinition bd) {
		if (!(bd instanceof RootBeanDefinition))
			return false;
		RootBeanDefinition rbd = (RootBeanDefinition) bd;
		return rbd.isSingleton() && "create".equals(rbd.getFactoryMethodName());
	}

	/**
	 * Find out what script this object was created from, if we created it
	 * @param o
	 * @return
	 */
	protected Script lookupScript(Object o) {
		return (Script) objectMap.get(o);
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException { 

		// Find the script managed by this class that created that bean.
		// If it's null, we didn't create it.
		Script script = lookupScript(bean);
		
		if (script != null) {
			ProxyFactory pf = new ProxyFactory();
	
			// Force the use of CGLIB
			pf.setProxyTargetClass(false);

			// Add Script interfaces
			for (int i = 0; i < script.getInterfaces().length; i++) {
				pf.addInterface(script.getInterfaces()[i]);
			}
			// Add interfaces implemented by Script
			// TODO optional
			Class[] intfs = AopUtils.getAllInterfaces(bean);
			for (int i = 0; i < intfs.length; i++) {
				pf.addInterface(intfs[i]);
			}
			
	
			// DynamicObjectInterceptor needs to know how to get this
			pf.setExposeProxy(true);
	
			pf.setOptimize(false);
	
			// Add the DynamicScript introduction
			DynamicScriptInterceptor dii = new DynamicScriptInterceptor(script);
			pf.addAdvisor(new DefaultIntroductionAdvisor(dii, DynamicScript.class));
	

			pf.setTargetSource(new DynamicBeanTargetSource(bean, beanFactory, beanName));
			if (expirySeconds == 0) {
				
			}
			else {
				AutoRefreshDynamicBeanTargetSource targetSource = new AutoRefreshDynamicBeanTargetSource(bean, beanFactory, beanName, script);
				targetSource.setExpirySeconds(this.expirySeconds);
				pf.setTargetSource(targetSource);
			}
	
			log.info("Installed refreshable TargetSource " + pf.getTargetSource() + " for bean '" + beanName + "'");
	
			Object wrapped = pf.getProxy();
			return wrapped;
		}
		else {
			return bean;
		}

	}
}