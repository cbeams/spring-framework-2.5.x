/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

/**
 * Sub-interface implemented by bean factories that can be part
 * of a hierarchy.
 *
 * <p>The corresponding setParentBeanFactory method for bean
 * factories that allow setting the parent in a configurable
 * fashion can be found in the ConfigurableBeanFactory interface.
 *
 * @author Rod Johnson
 * @since 07-Jul-2003
 * @version $Id: HierarchicalBeanFactory.java,v 1.2 2004-02-22 21:52:31 jhoeller Exp $
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
 */
public interface HierarchicalBeanFactory extends BeanFactory {
	
	/**
	 * Return the parent bean factory, or null if there is none.
	 */
	BeanFactory getParentBeanFactory();

}
