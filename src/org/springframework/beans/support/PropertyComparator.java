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

package org.springframework.beans.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

/**
 * PropertyComparator performs a comparison of two beans,
 * using the specified bean property via a BeanWrapper.
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 19.05.2003
 */
public class PropertyComparator implements Comparator {

	protected final Log logger = LogFactory.getLog(getClass());

	private final SortDefinition sortDefinition;

	private final Map cachedBeanWrappers = new HashMap();


	public PropertyComparator(SortDefinition sortDefinition) {
		this.sortDefinition = sortDefinition;
	}

	public int compare(Object o1, Object o2) {
		Object v1 = getPropertyValue(o1);
		Object v2 = getPropertyValue(o2);
		if (this.sortDefinition.isIgnoreCase() && (v1 instanceof String) && (v2 instanceof String)) {
			v1 = ((String) v1).toLowerCase();
			v2 = ((String) v2).toLowerCase();
		}
		int result;
		
		// Put a null property at the end of the sort.
		try {
			if (v1 != null) {
				if (v2 != null) {
					result = ((Comparable) v1).compareTo(v2);
				}
				else {
					result = -1;
				}
			}
			else {
				if (v2 != null) {
					result = 1;
				}
				else {
					result = 0;
				}
			}
		}
		catch (RuntimeException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not sort objects [" + o1 + "] and [" + o2 + "]", ex);
			}
			return 0;
		}
		return (this.sortDefinition.isAscending() ? result : -result);
	}

	/**
	 * Get the SortDefinition's property value for the given object.
	 * @param obj the object to get the property value for
	 * @return the property value
	 */
	private Object getPropertyValue(Object obj) {
		BeanWrapper bw = (BeanWrapper) this.cachedBeanWrappers.get(obj);
		if (bw == null) {
			bw = new BeanWrapperImpl(obj);
			this.cachedBeanWrappers.put(obj, bw);
		}

		// If a nested property cannot be read, simply return null
		// (similar to JSTL EL). If the property doesn't exist in the
		// first place, let the exception through.
		try {
			return bw.getPropertyValue(this.sortDefinition.getProperty());
		}
		catch (BeansException ex) {
			logger.info("PropertyComparator could not access property - treating as null for sorting", ex);
			return null;
		}
	}


	/**
	 * Sort the given List according to the given sort definition.
	 * <p>Note: Contained objects have to provide the given property
	 * in the form of a bean property, i.e. a getXXX method.
	 * @param source the input List
	 * @param sortDefinition the parameters to sort by
	 * @throws java.lang.IllegalArgumentException in case of a missing propertyName
	 */
	public static void sort(List source, SortDefinition sortDefinition) throws BeansException {
		Collections.sort(source, new PropertyComparator(sortDefinition));
	}

	/**
	 * Sort the given source according to the given sort definition.
	 * <p>Note: Contained objects have to provide the given property
	 * in the form of a bean property, i.e. a getXXX method.
	 * @param source input source
	 * @param sortDefinition the parameters to sort by
	 * @throws java.lang.IllegalArgumentException in case of a missing propertyName
	 */
	public static void sort(Object[] source, SortDefinition sortDefinition) throws BeansException {
		Arrays.sort(source, new PropertyComparator(sortDefinition));
	}

}
