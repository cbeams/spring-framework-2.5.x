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
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
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
 *  This superclass has the following responsibilities:
 * <ol>
 * 	<li>Provide create() methods that can be used as factory methods to create scripts.
 *  <li>Act as an auto proxy creator to automatically proxy these beans, making the
 * resulting proxies implement the DynamicScript interface.
 * </ol>
 * <p>
 * Use: Define a concrete subclass as a bean in a context. Invoke create() methods via factory-bean/factory-method
 * bean definitions in the same context.
 * @author Rod Johnson
 * @since 1.2
 */
public abstract class AbstractScriptFactory extends AbstractDynamicObjectAutoProxyCreator 
	implements ScriptContext, ApplicationContextAware, BeanFactoryPostProcessor, BeanNameAware {

	/**
	 * Owning bean factory. We need this to look up bean definitions.
	 */
	private ConfigurableListableBeanFactory beanFactory;
	
	/**
	 * The bean name this factory is defined with
	 */
	private String ourBeanName;
	
	/**
	 * ResourceLoader to pass to scripts.
	 */
	private ResourceLoader resourceLoader;

	/**
	 * Map of object created by a create() method to the Script that created each object.
	 * This is used for internal communication between the create() method
	 * and the createRefreshableTargetSource() method.
	 */
	private Map createBeanToScriptMap = new HashMap();

	/**
	 * We need to know our bean name to post process bean definitions that call its
	 * create methods
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String beanName) {
		ourBeanName = beanName;
	}
	
	/**
	 * We implement this method to obtain a ResourceLoader to make available to Scripts.
	 * The setResourceLoader() method can enable this class to work outside an application context.
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext ac) {
		this.resourceLoader = ac;
	}

	/**
	 * Alternative to ApplicationContextAware callback
	 * @param resourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * @see org.springframework.beans.factory.script.ScriptContext#getResourceLoader()
	 */
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}


	/**
	 * Public method for use as a factory-bean factory-method.
	 * Create a Scripted object
	 * @param className location of the script
	 * @param interfaceNames interfaces to be implemented by the scripts. (FQNs.)
	 * @return an object created from the specified script location
	 * @throws BeansException if the script cannot be created
	 */
	public Object create(String className, String[] interfaceNames) throws BeansException {
		if (requiresConfigInterface()) {
			throw new IllegalStateException("Script bean usage incorrect: must specify one or more interfaces if config interface is required");
		}
		return create(className, interfaceNames, null);
	}
	
	/**
	 * For private usage. This class adds the beanName argument (which is only known at runtime
	 * in a container) by modified the bean definition.
	 * All other create() methods delegate to this method.
	 * @param className
	 * @param interfaceNames
	 * @param bd
	 * @return
	 * @throws BeansException
	 */
	public Object create(String className, String[] interfaceNames, String beanName) throws BeansException {
		Script script = configuredScript(className, interfaceNames);
		if (requiresConfigInterface()) {
			if (beanName == null) {
				throw new IllegalArgumentException("Bean name must not be null");
			}
			logger.info("Create script bean with name '" + beanName + "'; will create config interface");
			// Use the bean name to find the bean definition, which contains the properties
			// that will be needed to the configuration interface
			script.addInterface(createConfigInterface(beanFactory.getBeanDefinition(beanName)));
		}
		
		// Now we have a script, create an object
		Object o = script.createObject();
		// Put the script in a Map, keyed by the created object
		createBeanToScriptMap.put(o, script);
		return o;
	}
	
	/**
	 * Post process bean definitions using the create methods on this class to make bean name
	 * available, causing the 3-arg create method to be invoked. 
	 * This is necessary to work out the config interface that may need to be implemented.
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
					// Add beanName as third argument, causing the 3-arg create method to be invoked
					logger.info("Handling create() method bean name '" + names[i] + "'");
					rbd.getConstructorArgumentValues().addGenericArgumentValue(names[i]);
				}
			}
		}
	}

	/**
	 * Create a script object without specifying any interfaces.
	 * Only usable by subclasses that don't require a configuration interface and,
	 * like Groovy, can define a full Java class.
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
		//Script script = (Script) scripts.get(location);
		
		Script script = createScript(location);

		// Add interfaces. This will not include any config interface.
		try {
			Class[] interfaces = AopUtils.toInterfaceArray(interfaceNames);
			for (int i = 0; i < interfaces.length; i++) {
				script.addInterface(interfaces[i]);
			}
			return script;
		}
		catch (ClassNotFoundException ex) {
			throw new ScriptInterfaceException(ex);
		}
	}

	/**
	 * Subclasses must implement this, with knowledge about specific Script
	 * classes to instantiate.
	 */
	protected abstract Script createScript(String location) throws BeansException;


	/**
	 * Find out what script this object was created from, if we created it
	 * @param o
	 * @return
	 */
	protected Script lookupScript(Object o) {
		return (Script) createBeanToScriptMap.get(o);
	}

	/**
	 * Will already have the TargetSource and introduction
	 * advisor in place. We need to add to the ProxyFactory
	 * all interfaces implemented by the script.
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
	

	/**
	 * @see org.springframework.beans.factory.dynamic.AbstractDynamicObjectConverter#createRefreshableTargetSource(java.lang.Object, org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.lang.String)
	 */
	protected AbstractRefreshableTargetSource createRefreshableTargetSource(Object bean,
			ConfigurableListableBeanFactory beanFactory, String beanName) {
		// If we created this bean, create a refreshable TargetSource
		// for it
		Script script = lookupScript(bean);
		if (script == null) {
			// This bean was not created by this object: leave it alone
			return null;
		}
		return new DynamicScriptTargetSource(beanFactory, beanName, script);
	}
	
	/**
	 * Create a config interface based on the setter methods the BeanDefinition will require.
	 * This interface can then be implemented by Beanshell and other scripts that require Java
	 * interfaces to target configuration.
	 * <br>The config interface will include a setter method taking Object for each 
	 * property specified in the bean definition
	 * @param bd BeanDefinition driving the current script
	 * @return a configuration interface including the necessary setter methods
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
	
	/**
	 * Subclasses should implement this to indicate whether they require
	 * a configuration interface to be constructed. The configuration interface
	 * will include all necessary setters.
	 * @return
	 */
	protected abstract boolean requiresConfigInterface();
}