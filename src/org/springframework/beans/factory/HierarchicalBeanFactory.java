/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

/**
 * Subinterface implemented by bean factories that can be part
 * of a hierarchy.
 * @author Rod Johnson
 * @since 07-Jul-2003
 * @version $Id: HierarchicalBeanFactory.java,v 1.1.1.1 2003-08-14 16:20:18 trisberg Exp $
 */
public interface HierarchicalBeanFactory extends BeanFactory {
	
	/**
	 * Returns the parent bean factory, or null if there is none.
	 * @return the parent bean factory, or null if there is no parent
	 */
	BeanFactory getParentBeanFactory();

}
