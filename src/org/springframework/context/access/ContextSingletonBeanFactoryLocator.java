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

package org.springframework.context.access;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * <p>Variant of SingletonBeanFactoryLocator which creates its internal bean
 * factory reference definition as an ApplicationContext instead of
 * SingletonBeanFactoryLocator's BeanFactory. For almost all usage scenarios, this
 * will not make a difference, since withing that ApplicationContext or BeanFactory
 * you are still free to create either BeanFactories or ApplicationContexts. The
 * main reason one would need to use this class is if BeanPostProcessing (or other
 * ApplicationContext specific features are needed in the bean reference definition
 * itself.</p>
 *
 * <p><strong>Note: </strong>This class uses <strong>beanRefContext.xml</strong>
 * as the default name for the bean factory reference definition. It is not possible
 * nor legal to share definitions with SingletonBeanFactoryLocator at the same time.
 * 
 * @author Colin Sampaleanu
 * @see org.springframework.context.access.DefaultLocatorFactory
 */
public class ContextSingletonBeanFactoryLocator extends SingletonBeanFactoryLocator {

	public static final String BEANS_REFS_XML_NAME = "beanRefContext.xml";
	
	// the keyed singleton instances
	private static Map instances = new HashMap();


	/**
	 * Returns an instance which uses the default "beanRefContext.xml", as the name
	 * of the definition file(s). All resources returned by the current thread's context
	 * classloader's getResources() method with this name will be combined to create a
	 * definition, which is just a BeanFactory.
	 */
	public static BeanFactoryLocator getInstance() throws BeansException {
		return getInstance(BEANS_REFS_XML_NAME);
	}

	/**
	 * Returns an instance which uses the the specified selector, as the name of the
	 * definition file(s). All resources returned by the current thread's context
	 * classloader's getResources() method with this name will be combined to create a
	 * definition, which is just a a BeanFactory.
	 * @param selector the name of the resource(s) which will be read and combine to
	 * form the definition for the SingletonBeanFactoryLocator instance
	 */
	public static BeanFactoryLocator getInstance(String selector) throws BeansException {
		synchronized (instances) {
			if (logger.isDebugEnabled()) {
				logger.debug("ContextSingletonBeanFactoryLocator.getInstance(): instances.hashCode=" +
				             instances.hashCode() + ", instances=" + instances);
			}
			BeanFactoryLocator bfl = (BeanFactoryLocator) instances.get(selector);
			if (bfl == null) {
				bfl = new ContextSingletonBeanFactoryLocator(selector);
				instances.put(selector, bfl);
			}
			return bfl;
		}
	}


	/**
	 * Constructor which uses the default "bean-refs.xml", as the name of the
	 * definition file(s). All resources returned by the definition classloader's
	 * getResources() method with this name will be combined to create a definition.
	 */
	protected ContextSingletonBeanFactoryLocator() {
		super(BEANS_REFS_XML_NAME);
	}

	/**
	 * Constructor which uses the the specified name as the name of the
	 * definition file(s). All resources returned by the definition classloader's
	 * getResources() method with this name will be combined to create a definition.
	 */
	protected ContextSingletonBeanFactoryLocator(String resourceName) {
		super(resourceName);
	}
	
	/**
	 * Overrides default method to create definition object as an ApplicationContext
	 * instead of the default BeanFactory. This does not affect what can actually
	 * be loaded by that definition.
	 */
	protected BeanFactory createDefinition(String[] resources) throws BeansException {
		return new FileSystemXmlApplicationContext(resources);
	}
	
    /**
     * Overrides default method to work with ApplicationContext
     */
	protected void destroyDefinition(BeanFactory groupDef, String resourceName) throws BeansException {

		if (groupDef instanceof ConfigurableApplicationContext) {
			// debugging trace only
			if (logger.isDebugEnabled()) {
				logger.debug("ContextSingletonBeanFactoryLocator group with resourceName '"
						+ resourceName
						+ "' being released, as no more references.");
			}
			((ConfigurableApplicationContext) groupDef).close();
		}
	}
}
