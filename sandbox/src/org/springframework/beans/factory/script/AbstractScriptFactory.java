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

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.dynamic.AbstractDynamicObjectAutoProxyCreator;
import org.springframework.beans.factory.dynamic.AbstractRefreshableTargetSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Creates scripts
 * 
 * @author Rod Johnson
 */
public abstract class AbstractScriptFactory extends AbstractDynamicObjectAutoProxyCreator implements ScriptContext, ApplicationContextAware {

	private ResourceLoader resourceLoader;

	/** Location to Script */
	private Map scripts = new HashMap();

	/**
	 * Object to Script: need to bound TODO
	 */
	private Map objectMap = new HashMap();


	public void setApplicationContext(ApplicationContext ac) {
		this.resourceLoader = ac;
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
	 * Create an object
	 * @param className
	 * @param interfaceNames
	 * @return
	 * @throws BeansException
	 */
	public Object create(String className, String[] interfaceNames) throws BeansException {
		Script script = configuredScript(className, interfaceNames);
		Object o = script.createObject();
		objectMap.put(o, script);
		return o;
	}

	/**
	 * Ccreate without specifying any interfaces
	 * @param className
	 * @return
	 * @throws BeansException
	 */
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


	/**
	 * Find out what script this object was created from, if we created it
	 * @param o
	 * @return
	 */
	protected Script lookupScript(Object o) {
		return (Script) objectMap.get(o);
	}

	/**
	 * Will already have the TargetSource and introduction
	 * advisor in place
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	protected void customizeProxyFactory(Object bean, ProxyFactory pf) {
		if (pf.getTargetSource() instanceof DynamicScriptTargetSource) {
			// If we created it...
			DynamicScriptTargetSource ts = (DynamicScriptTargetSource) pf.getTargetSource();
			Script script = ts.getScript();
			
			// Add Script interfaces
			for (int i = 0; i < script.getInterfaces().length; i++) {
				pf.addInterface(script.getInterfaces()[i]);
			}
		}
	}
	
	public IntroductionAdvisor getIntroductionAdvisor() {
		return new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(this), Script.class);
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.AbstractDynamicObjectConverter#createRefreshableTargetSource(java.lang.Object, org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.lang.String)
	 */
	protected AbstractRefreshableTargetSource createRefreshableTargetSource(Object bean,
			ConfigurableListableBeanFactory beanFactory, String beanName) {
		Script script = lookupScript(bean);
		if (script == null) {
			return null;
		}
		return new DynamicScriptTargetSource(beanFactory, beanName, script);
	}
}