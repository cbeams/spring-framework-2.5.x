/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.access;

import java.util.HashMap;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
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
 * <p><strong>Note: </strong>This class uses <strong>beanRefContext.xml</strong>
 * as the default name for the bean factory reference definition. It is not possible
 * nor legal to share definitions with SingletonBeanFactoryLocator, at the same time.
 * 
 * @version $Revision: 1.3 $
 * @author colin sampaleanu
 * 
 * @see org.springframework.context.access.LocatorFactory
 */
public class DefaultBeanFactoryLocator extends SingletonBeanFactoryLocator {

    // --- statics
	public static final String BEANS_REFS_XML_NAME = "beanRefContext.xml";
	
	// the keyed singleton instances
	private static DefaultBeanFactoryLocator _noSelectorInstance;

	private static HashMap instances = new HashMap();
	
	/**
	 * Returns an instance which uses the default "beanRefContext.xml", as the name
	 * of the definition file(s). All resources returned by the current thread's context
	 * classloader's getResources() method with this name will be combined to create a
	 * definition, which is just a BeanFactory.
	 *  
	 * @throws FatalBeanException
	 */
	public static BeanFactoryLocator getInstance() throws FatalBeanException {
		return getInstance(BEANS_REFS_XML_NAME);
	}

	/**
	 * Returns an instance which uses the the specified selector, as the name of the
	 * definition file(s). All resources returned by the current thread's context
	 * classloader's getResources() method with this name will be combined to create a
	 * definition, which is just a a BeanFactory.
	 *
	 * @param selector the name of the resource(s) which will be read and combine to
	 * form the definition for the KeyedSingletonBeanFactoryLocator instance
	 *  
	 * @throws FatalBeanException
	 */
	public static BeanFactoryLocator getInstance(String selector)
			throws FatalBeanException {

		synchronized (instances) {
			_log.debug("DefaultBeanFactoryLocator.getInstance(): DefaultBeanFactoryLocator.class="
				+ DefaultBeanFactoryLocator.class + "hash= " + DefaultBeanFactoryLocator.class.hashCode());
			_log.debug("DefaultBeanFactoryLocator.getInstance(): instances.hashCode=" + instances.hashCode() + ", instances=" + instances);
			
			BeanFactoryLocator bfl = (BeanFactoryLocator) instances.get(selector);
			if (bfl == null) {
				bfl = new DefaultBeanFactoryLocator(selector);
				instances.put(selector, bfl);
			}

			return bfl;
		}
	}

	//
	// Constructor which uses the default "bean-refs.xml", as the name of the
	// definition file(s). All resources returned by the definition classloader's
	// getResources() method with this name will be combined to create a definition
	// definition. 
	//
	protected DefaultBeanFactoryLocator() {
		super(BEANS_REFS_XML_NAME);
	}

	//
	// Constructor which uses the the specified name as the name of the
	// definition file(s). All resources returned by the definition classloader's
	// getResources() method with this name will be combined to create a definition
	// definition. 
	//
	protected DefaultBeanFactoryLocator(String resourceName) {
		super(resourceName);
	}
	
	/**
	 * Overrides default method to create definition oject as an ApplicationContext
	 * instead of the default BeanFactory. This does not affect what can actually
	 * be loaded by that definition.
	 *  
	 * @param resources
	 * @return
	 */
	protected BeanFactory createDefinition(String[] resources) throws FatalBeanException {
		FileSystemXmlApplicationContext groupContext = new FileSystemXmlApplicationContext(
				resources);
		return groupContext;
		
	}
	
}
