/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.access;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * <p>Small variant of SingletonBeanFactoryLocator which creates its internal 
 * definition object as an ApplicationContext instead of BeanFactory. It would be
 * preferred over its superclass if you need to use features (such as
 * BeanPostProcessors) in the definition itself, which are only available in an
 * ApplicationContext. Note that for most users it this is irrelevant, as even its
 * superclass can still, within its definition object, which is a BeanFactory, create
 * and return ApplicationContext objects.
 * 
 * @version $Revision: 1.1 $
 * @author colin sampaleanu
 * 
 * @see org.springframework.context.access.LocatorFactory
 */
public class DefaultBeanFactoryLocator extends SingletonBeanFactoryLocator {

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
	
	// used only for unit tests
	protected DefaultBeanFactoryLocator() {
		super();
	}

	// used only for unit tests
	protected DefaultBeanFactoryLocator(String resourceName) {
		super(resourceName);
	}
}
