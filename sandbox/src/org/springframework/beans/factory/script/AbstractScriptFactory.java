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

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.InterfaceMaker;

import org.objectweb.asm.Type;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.dynamic.AbstractDynamicObjectAutoProxyCreator;
import org.springframework.beans.factory.dynamic.AbstractRefreshableTargetSource;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Creates scripts
 * 
 * @author Rod Johnson
 */
public abstract class AbstractScriptFactory extends AbstractDynamicObjectAutoProxyCreator 
	implements ScriptContext, ApplicationContextAware, BeanFactoryPostProcessor, BeanNameAware {

	private ConfigurableListableBeanFactory beanFactory;
	
	private String ourBeanName;
	
	private ResourceLoader resourceLoader;

	/** Location to Script */
	private Map scripts = new HashMap();

	/**
	 * Object to Script: need to bound TODO
	 */
	private Map objectMap = new HashMap();

	/**
	 * We need to know our bean name to post process bean definitions that call its
	 * create methods
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String beanName) {
		ourBeanName = beanName;
	}
	
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
	 * Public method for users.
	 * Create an object
	 * @param className
	 * @param interfaceNames
	 * @return
	 * @throws BeansException
	 * TODO need to know config interface here
	 */
	public Object create(String className, String[] interfaceNames) throws BeansException {
		// TODO should this throw an exception?
		return create(className, interfaceNames, null);
	}
	
	/**
	 * For private usage
	 * @param className
	 * @param interfaceNames
	 * @param bd
	 * @return
	 * @throws BeansException
	 */
	public Object create(String className, String[] interfaceNames, String beanName) throws BeansException {
		logger.info("Create bean with name '" + beanName + "'");
		Script script = configuredScript(className, interfaceNames);
		if (requiresConfigInterface()) {
			if (beanName == null) {
				throw new IllegalArgumentException("Bean name must not be null");
			}
			// Use the bean name to find the bean definition, which contains the properties
			// that will be needed to the configuration interface
			script.addInterface(createConfigInterface(beanFactory.getBeanDefinition(beanName)));
		}
		Object o = script.createObject();
		objectMap.put(o, script);
		return o;
	}
	
	/**
	 * Post process bean definitions using the create methods on this class to make bean name
	 * available. This is necessary to work out the config interface that may need to be implemented.
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		String[] names = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < names.length; i++) {
			BeanDefinition bd = beanFactory.getBeanDefinition(names[i]);
			if (bd instanceof RootBeanDefinition) {
				RootBeanDefinition rbd = (RootBeanDefinition) bd;
				if (ourBeanName.equals(rbd.getFactoryBeanName()) && 
						"create".equals(rbd.getFactoryMethodName()) && 
						rbd.getConstructorArgumentValues().getArgumentCount() == 2) {
					// Add beanName as third argument
					logger.info("Handling create() method bean name '" + names[i] + "'");
					rbd.getConstructorArgumentValues().addGenericArgumentValue(names[i]);
				}
			}
		}
	}

	/**
	 * Create without specifying any interfaces
	 * @param className
	 * @return
	 * @throws BeansException
	 */
	public Object create(String className) throws BeansException {
		if (requiresConfigInterface()) {
			throw new IllegalArgumentException("Must specify interfaces if ScriptFactory requires config interface");
		}
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
	
	/**
	 * Create a config interface based on the setter methods the BeanDefinition will require.
	 * This interface can then be implemented by Beanshell and other scripts that require Java
	 * interfaces to target configuration.
	 * @param bd BeanDefinition driving the current script
	 * @return
	 */
	protected Class createConfigInterface(BeanDefinition bd) {
		InterfaceMaker imaker = new InterfaceMaker();
		for (int i = 0; i < bd.getPropertyValues().getPropertyValues().length; i++) {
			String propertyName = bd.getPropertyValues().getPropertyValues()[i].getName();
			String setterName = "set"  + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			Signature sig = new Signature(setterName, Type.VOID_TYPE, new Type[] { Type.getType(Object.class) });
			imaker.add(sig, new Type[0]);
		}
		Class intf = imaker.create();
		for (int i = 0; i < intf.getMethods().length; i++) {
			logger.info("Adding configuration method: " + intf.getMethods()[i]);
		}
		return intf;
	}
	
	protected abstract boolean requiresConfigInterface();
}