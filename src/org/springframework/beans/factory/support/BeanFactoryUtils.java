/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Convenience methods operating on bean factories.
 * Use to get a collection of beans of a given type
 * (rather than bean names), and for methods that return 
 * bean instances, names or counts taking into account the
 * hierarchy a bean factory may participate in.
 * @author Rod Johnson
 * @since 04-Jul-2003
 * @version $Id: BeanFactoryUtils.java,v 1.3 2003-11-04 23:10:02 jhoeller Exp $
 */
public abstract class BeanFactoryUtils {
	
	/**
	 * Count all bean definitions in any hierarchy in which this
	 * factory participates. Includes counts of ancestor bean factories.
	 * Beans that are "overridden" (specified in a descendant factory
	 * with the same name) are counted only once.
	 * @param lbf
	 * @return int count of beans including those defined in ancestor
	 * factories
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
	 * Return all beans of the given type or subtypes, also picking
	 * up beans defined in ancestor bean factories if the current
	 * bean factory is a HierarchicalBeanFactory.
	 * Useful convenience method when we don't care about bean names.
	 * @param lbf the bean factory
	 * @param type type of bean to match. The return list will only
	 * contain beans of this type.
	 * @return the Map of bean instances, or an empty Map if none
	 */
	public static Map beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class type,
																									boolean includePrototypes, boolean includeFactoryBeans) {
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

}
