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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Convenience methods operating on bean factories, returning bean instances,
 * names or counts taking into account the nesting hierarchy of a bean factory.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 04-Jul-2003
 */
public abstract class BeanFactoryUtils {
	
	/**
	 * Count all bean definitions in any hierarchy in which this
	 * factory participates. Includes counts of ancestor bean factories.
	 * Beans that are "overridden" (specified in a descendant factory
	 * with the same name) are counted only once.
	 * @param lbf
	 * @return int count of beans including those defined in ancestor factories
	 */
	public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesIncludingAncestors(lbf).length;
	}
	
	/**
	 * Return all bean names in the factory, including ancestor factories.
	 * @param lbf the bean factory
	 * @return the array of bean names, or an empty array if none
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
		Set result = new HashSet();
		result.addAll(Arrays.asList(lbf.getBeanDefinitionNames()));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() != null && hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesIncludingAncestors((ListableBeanFactory) hbf.getParentBeanFactory());
				result.addAll(Arrays.asList(parentResult));
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}
	
	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * @param lbf ListableBeanFactory. If this isn't also a HierarchicalBeanFactory,
	 * this method will return the same as it's own getBeanDefinitionNames() method.
	 * @param type the type that beans must match
	 * @return the array of bean names, or an empty array if none
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf, Class type) {
		Set result = new HashSet();
		result.addAll(Arrays.asList(lbf.getBeanDefinitionNames(type)));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() != null && hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesIncludingAncestors((ListableBeanFactory) hbf.getParentBeanFactory(), type);
				result.addAll(Arrays.asList(parentResult));
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The return list will only contain beans of this type.
	 * Useful convenience method when we don't care about bean names.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include FactoryBeans too or just normal beans
	 * @return the Map of bean instances, or an empty Map if none
	 * @throws org.springframework.beans.BeansException if the beans could not be created
	 */
	public static Map beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class type,
																									boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException {
		Map result = new HashMap();
		result.putAll(lbf.getBeansOfType(type, includePrototypes, includeFactoryBeans));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() != null && hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				Map parentResult = beansOfTypeIncludingAncestors((ListableBeanFactory) hbf.getParentBeanFactory(),
																												 type, includePrototypes, includeFactoryBeans);
				for (Iterator it = parentResult.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					if (!result.containsKey(key)) {
						result.put(key, parentResult.get((key)));
					}
				}
			}
		}
		return result;
	}

	/**
	 * Return a single bean of the given type or subtypes, also picking up beans defined
	 * in ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * Useful convenience method when we expect a single bean and don't care about the bean name.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include FactoryBeans too or just normal beans
	 * @return the Map of bean instances, or an empty Map if none
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.BeansException if the bean could not be created
	 */
	public static Object beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class type,
	                                                  boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException {
		Map beansOfType = beansOfTypeIncludingAncestors(lbf, type, includePrototypes, includeFactoryBeans);
		if (beansOfType.size() == 1) {
			return beansOfType.values().iterator().next();
		}
		else {
			throw new NoSuchBeanDefinitionException(type, "Expected single bean but found " + beansOfType.size());
		}
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor factories.
	 * Useful convenience method when we expect a single bean and don't care about the bean name.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include FactoryBeans too or just normal beans
	 * @return the Map of bean instances, or an empty Map if none
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.BeansException if the bean could not be created
	 */
	public static Object beanOfType(ListableBeanFactory lbf, Class type,
	                                boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException {
		Map beansOfType = lbf.getBeansOfType(type, includePrototypes, includeFactoryBeans);
		if (beansOfType.size() == 1) {
			return beansOfType.values().iterator().next();
		}
		else {
			throw new NoSuchBeanDefinitionException(type, "Expected single bean but found " + beansOfType.size());
		}
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor factories.
	 * Useful convenience method when we expect a single bean and don't care about the bean name.
	 * This version of beanOfType automatically includes prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the Map of bean instances, or an empty Map if none
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.BeansException if the bean could not be created
	 */
	public static Object beanOfType(ListableBeanFactory lbf, Class type) throws BeansException {
		return beanOfType(lbf, type, true, true);
	}

}
