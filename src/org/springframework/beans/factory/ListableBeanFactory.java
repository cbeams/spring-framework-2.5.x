/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory;

import java.util.Map;

import org.springframework.beans.BeansException;

/**
 * Extension of the BeanFactory interface to be implemented by bean factories
 * that can enumerate all their bean instances, rather than attempting bean
 * lookup by name one by one as requested by clients.
 *
 * <p>If this is a HierarchicalBeanFactory, the return values will not take any
 * BeanFactory hierarchy into account, but will relate only to the beans defined
 * in the current factory. Use the BeanFactoryUtils helper class to get all.
 * 
 * <p>With the exception of getBeanDefinitionCount(), the methods in this interface
 * are not designed for frequent invocation. Implementations may be slow.
 *
 * <p>BeanFactory implementations that preload all their beans (for example,
 * DOM-based XML factories) may implement this interface.
 *
 * <p>This interface is discussed in "Expert One-on-One J2EE", by Rod Johnson.
 *
 * @author Rod Johnson
 * @since 16 April 2001
 * @version $Id: ListableBeanFactory.java,v 1.6 2003-12-11 18:20:44 jhoeller Exp $
 * @see org.springframework.beans.factory.support.BeanFactoryUtils
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * Return the number of beans defined in the factory.
	 * Does not consider any hierarchy this factory may participate in.
	 * @return the number of beans defined in the factory
	 */
	int getBeanDefinitionCount();

	/**
	 * Return the names of all beans defined in this factory
	 * Does not consider any hierarchy this factory may participate in.
	 * @return the names of all beans defined in this factory,
	 * or an empty array if none defined
	 */
	String[] getBeanDefinitionNames();
	
	/**
	 * Return the names of beans matching the given object type (including
	 * subclasses), judging from the bean definitions. Will <i>not</i>
	 * consider FactoryBeans as the type of their created objects is not
	 * known before instantiation.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * @param type class or interface to match, or null for all bean names
	 * @return the names of beans matching the given object type 
	 * (including subclasses), or an empty array if none
	 */
	String[] getBeanDefinitionNames(Class type);

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * Does not consider any hierarchy this factory may participate in.
	 * @param name the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 */
	boolean containsBeanDefinition(String name);

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * getObjectType() in the case of FactoryBeans.
	 * <p>If FactoryBean's getObjectType() returns null and the bean is a
	 * singleton, the type of the actually created objects should be evaluated.
	 * Prototypes without explicit object type specification should be ignored.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * @param type class or interface to match
	 * @param includePrototypes whether to include prototype beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include FactoryBeans too
	 * or just normal beans
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * @throws BeansException if the beans could not be created
	 */
	Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException;

}
