/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Convenience methods operating on bean factories, in particular on the
 * ListableBeanFactory interface.
 *
 * <p>Returns bean counts, bean names or bean instances,
 * taking into account the nesting hierarchy of a bean factory
 * (which the methods defined on the ListableBeanFactory interface don't,
 * in contrast to the methods defined on the BeanFactory interface).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 04.07.2003
 */
public abstract class BeanFactoryUtils {

	/**
	 * Return whether the given name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public static boolean isFactoryDereference(String name) {
		return name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX);
	}

	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary.
	 */
	public static String transformedBeanName(String name) {
		Assert.notNull(name, "Name must not be null");
		String beanName = name;
		if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanName;
	}


	/**
	 * Count all bean definitions in any hierarchy in which this factory
	 * participates. Includes counts of ancestor bean factories.
	 * <p>Beans that are "overridden" (specified in a descendant factory
	 * with the same name) are only counted once.
	 * @param lbf the bean factory
	 * @return count of beans including those defined in ancestor factories
	 */
	public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesIncludingAncestors(lbf).length;
	}
	
	/**
	 * Return all bean names in the factory, including ancestor factories.
	 * @param lbf the bean factory
	 * @return the array of matching bean names, or an empty array if none
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
		Set result = new HashSet();
		result.addAll(Arrays.asList(lbf.getBeanDefinitionNames()));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory());
				result.addAll(Arrays.asList(parentResult));
			}
		}
		return StringUtils.toStringArray(result);
	}
	
	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does <i>not</i> consider objects created by FactoryBeans but rather the
	 * FactoryBean classes themselves, avoiding instantiation of any beans. Use
	 * <code>beanNamesForTypeIncludingAncestors</code> to match objects created by
	 * FactoryBeans.
	 * @param lbf the bean factory
	 * @param type the type that beans must match
	 * @return the array of matching bean names, or an empty array if none
	 * @deprecated in favor of beanNamesForTypeIncludingAncestors.
	 * This method will be removed as of Spring 2.0.
	 * @see #beanNamesForTypeIncludingAncestors
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf, Class type) {
		Set result = new HashSet();
		result.addAll(Arrays.asList(lbf.getBeanDefinitionNames(type)));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
				result.addAll(Arrays.asList(parentResult));
			}
		}
		return StringUtils.toStringArray(result);
	}


	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of <code>beanNamesForTypeIncludingAncestors</code> automatically
	 * includes prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type the type that beans must match
	 * @return the array of matching bean names, or an empty array if none
	 */
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class type) {
		Set result = new HashSet();
		result.addAll(Arrays.asList(lbf.getBeanNamesForType(type)));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
				result.addAll(Arrays.asList(parentResult));
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans if the "includeFactoryBeans"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "includeFactoryBeans" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include <i>objects created by
	 * FactoryBeans</i> (or by factory methods with a "factory-bean" reference)
	 * too, or just conventional beans. Note that FactoryBeans need to be
	 * initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans (and "factory-bean" references).
	 * @param type the type that beans must match
	 * @return the array of matching bean names, or an empty array if none
	 */
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, Class type, boolean includePrototypes, boolean includeFactoryBeans) {

		Set result = new HashSet();
		result.addAll(Arrays.asList(lbf.getBeanNamesForType(type, includePrototypes, includeFactoryBeans)));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type, includePrototypes, includeFactoryBeans);
				result.addAll(Arrays.asList(parentResult));
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The returned Map will only contain beans of this type.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the Map of matching bean instances, or an empty Map if none
	 * @throws BeansException if a bean could not be created
	 */
	public static Map beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class type)
	    throws BeansException {

		Map result = new HashMap();
		result.putAll(lbf.getBeansOfType(type));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				Map parentResult = beansOfTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
				for (Iterator it = parentResult.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (!result.containsKey(entry.getKey())) {
						result.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The returned Map will only contain beans of this type.
	 * <p>Does consider objects created by FactoryBeans if the "includeFactoryBeans"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "includeFactoryBeans" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include <i>objects created by
	 * FactoryBeans</i> (or by factory methods with a "factory-bean" reference)
	 * too, or just conventional beans. Note that FactoryBeans need to be
	 * initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans (and "factory-bean" references).
	 * @return the Map of matching bean instances, or an empty Map if none
	 * @throws BeansException if a bean could not be created
	 */
	public static Map beansOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class type, boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException {

		Map result = new HashMap();
		result.putAll(lbf.getBeansOfType(type, includePrototypes, includeFactoryBeans));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				Map parentResult = beansOfTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type, includePrototypes, includeFactoryBeans);
				for (Iterator it = parentResult.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (!result.containsKey(entry.getKey())) {
						result.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return result;
	}


	/**
	 * Return a single bean of the given type or subtypes, also picking up beans
	 * defined in ancestor bean factories if the current bean factory is a
	 * HierarchicalBeanFactory. Useful convenience method when we expect a
	 * single bean and don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of <code>beanOfTypeIncludingAncestors</code> automatically includes
	 * prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the matching bean instance
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if no single bean could be found for the given type
	 * @throws BeansException if the bean could not be created
	 */
	public static Object beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class type)
			throws BeansException {

		Map beansOfType = beansOfTypeIncludingAncestors(lbf, type);
		if (beansOfType.size() == 1) {
			return beansOfType.values().iterator().next();
		}
		else {
			throw new NoSuchBeanDefinitionException(type, "expected single bean but found " + beansOfType.size());
		}
	}

	/**
	 * Return a single bean of the given type or subtypes, also picking up beans
	 * defined in ancestor bean factories if the current bean factory is a
	 * HierarchicalBeanFactory. Useful convenience method when we expect a
	 * single bean and don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans if the "includeFactoryBeans"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "includeFactoryBeans" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include <i>objects created by
	 * FactoryBeans</i> (or by factory methods with a "factory-bean" reference)
	 * too, or just conventional beans. Note that FactoryBeans need to be
	 * initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans (and "factory-bean" references).
	 * @return the matching bean instance
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if no single bean could be found for the given type
	 * @throws BeansException if the bean could not be created
	 */
	public static Object beanOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class type, boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException {

		Map beansOfType = beansOfTypeIncludingAncestors(lbf, type, includePrototypes, includeFactoryBeans);
		if (beansOfType.size() == 1) {
			return beansOfType.values().iterator().next();
		}
		else {
			throw new NoSuchBeanDefinitionException(type, "expected single bean but found " + beansOfType.size());
		}
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor
	 * factories. Useful convenience method when we expect a single bean and
	 * don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of <code>beanOfType</code> automatically includes
	 * prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the matching bean instance
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if no single bean could be found for the given type
	 * @throws BeansException if the bean could not be created
	 */
	public static Object beanOfType(ListableBeanFactory lbf, Class type) throws BeansException {
		Map beansOfType = lbf.getBeansOfType(type);
		if (beansOfType.size() == 1) {
			return beansOfType.values().iterator().next();
		}
		else {
			throw new NoSuchBeanDefinitionException(type, "expected single bean but found " + beansOfType.size());
		}
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor
	 * factories. Useful convenience method when we expect a single bean and
	 * don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans if the "includeFactoryBeans"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "includeFactoryBeans" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includePrototypes whether to include prototype beans too or just singletons
	 * (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include <i>objects created by
	 * FactoryBeans</i> (or by factory methods with a "factory-bean" reference)
	 * too, or just conventional beans. Note that FactoryBeans need to be
	 * initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans (and "factory-bean" references).
	 * @return the matching bean instance
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if 0 or more than 1 beans of the given type were found
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if no single bean could be found for the given type
	 * @throws BeansException if the bean could not be created
	 */
	public static Object beanOfType(
			ListableBeanFactory lbf, Class type, boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException {

		Map beansOfType = lbf.getBeansOfType(type, includePrototypes, includeFactoryBeans);
		if (beansOfType.size() == 1) {
			return beansOfType.values().iterator().next();
		}
		else {
			throw new NoSuchBeanDefinitionException(type, "expected single bean but found " + beansOfType.size());
		}
	}

}
