/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.access;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.access.BeanFactoryLocator;

/**
 * A factory class to get a BeanFactoryLocator instance.<br/> By default, an
 * instance of type DefaultBeanFactoryLocator is returned.
 * 
 * @version $Revision: 1.1 $
 * @author colin sampaleanu
 * 
 * @see org.springframework.context.access.DefaultBeanFactoryLocator
 */
public class LocatorFactory {

	/**
	 * Return an instance object implementing BeanFactoryLocator. This will normally
	 * be a singleton instance of the specific DefaultBeanFactoryLocator class,
	 * using the default resource selector.
	 *  
	 * @return BeanFactoryLocator
	 * @throws FatalBeanException
	 */
	public static BeanFactoryLocator getInstance() throws FatalBeanException {
		return DefaultBeanFactoryLocator.getInstance();
	}

	/**
	 * Return an instance object implementing BeanFactoryLocator. This will normally
	 * be a singleton instance of the specific DefaultBeanFactoryLocator class,
	 * using the specified resource selector.
	 * 
	 * @param selector a selector variable which provides a hint to the factory as to
	 * which instance to return.
	 * 
	 * @return BeanFactoryLocator
	 * @throws FatalBeanException
	 */
	public static BeanFactoryLocator getInstance(String selector)
			throws FatalBeanException {
		return DefaultBeanFactoryLocator.getInstance(selector);
	}
}
