/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.access;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.access.BeanFactoryLocator;

/**
 * A factory class to get a default ContextSingletonBeanFactoryLocator instance.
 * @author Colin Sampaleanu
 * @version $Revision: 1.1 $
 * @see org.springframework.context.access.ContextSingletonBeanFactoryLocator
 */
public class DefaultLocatorFactory {

	/**
	 * Return an instance object implementing BeanFactoryLocator. This will normally
	 * be a singleton instance of the specific ContextSingletonBeanFactoryLocator class,
	 * using the default resource selector.
	 */
	public static BeanFactoryLocator getInstance() throws FatalBeanException {
		return ContextSingletonBeanFactoryLocator.getInstance();
	}

	/**
	 * Return an instance object implementing BeanFactoryLocator. This will normally
	 * be a singleton instance of the specific ContextSingletonBeanFactoryLocator class,
	 * using the specified resource selector.
	 * @param selector a selector variable which provides a hint to the factory as to
	 * which instance to return.
	 */
	public static BeanFactoryLocator getInstance(String selector) throws FatalBeanException {
		return ContextSingletonBeanFactoryLocator.getInstance(selector);
	}
}
