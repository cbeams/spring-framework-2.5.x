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

package org.springframework.beans.factory;

import java.util.Map;

import org.springframework.beans.BeansException;

/**
 * Extension of the BeanFactory interface to be implemented by bean factories
 * that can enumerate all their bean instances, rather than attempting bean lookup
 * by name one by one as requested by clients. BeanFactory implementations that
 * preload all their beans (for example, DOM-based XML factories) may implement
 * this interface. This interface is discussed in "Expert One-on-One J2EE Design
 * and Development", by Rod Johnson.
 *
 * <p>If this is a HierarchicalBeanFactory, the return values will <i>not</i> take
 * any BeanFactory hierarchy into account, but will relate only to the beans defined
 * in the current factory. Use the BeanFactoryUtils helper class to get all beans.
 * 
 * <p>The methods in this interface will just respect bean definitions of this factory.
 * They will ignore any singleton beans that have been registered by other means like
 * ConfigurableBeanFactory's registerSingleton. Use BeanFactory's methods to access such
 * beans. In typical scenarios, all beans will be defined by bean definitions anyway.
 *
 * <p>With the exception of getBeanDefinitionCount(), the methods in this interface
 * are not designed for frequent invocation. Implementations may be slow.
 *
 * @author Rod Johnson
 * @since 16 April 2001
 * @see BeanFactoryUtils
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerSingleton
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * Return the number of beans defined in the factory.
	 * Does not consider any hierarchy this factory may participate in.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @return the number of beans defined in the factory
	 */
	int getBeanDefinitionCount();

	/**
	 * Return the names of all beans defined in this factory.
	 * Does not consider any hierarchy this factory may participate in.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @return the names of all beans defined in this factory,
	 * or an empty array if none defined
	 */
	String[] getBeanDefinitionNames();
	
	/**
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from the bean definitions. Will <i>not</i> consider FactoryBeans,
	 * as the type of their created objects is not known before instantiation.
	 * Does not consider any hierarchy this factory may participate in.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @param type class or interface to match, or null for all bean names
	 * @return the names of beans matching the given object type 
	 * (including subclasses), or an empty array if none
	 */
	String[] getBeanDefinitionNames(Class type);

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * Does not consider any hierarchy this factory may participate in.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @param name the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 */
	boolean containsBeanDefinition(String name);

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * getObjectType() in the case of FactoryBeans.
	 * Does not consider any hierarchy this factory may participate in.
	 * <p>If FactoryBean's getObjectType() returns null and the bean is a
	 * singleton, the type of the actually created objects should be evaluated.
	 * Prototypes without explicit object type specification should be ignored.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * @param type class or interface to match
	 * @param includePrototypes whether to include prototype beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include FactoryBeans too
	 * or just normal beans
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * @throws BeansException if the beans could not be created
	 * @see FactoryBean#getObjectType
	 */
	Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException;

}
