/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.metadata.support.AbstractAttributes;

/**
 * Convenient class for simulating attributes.
 * @author Rod Johnson
 * @version $Id: MapAttributes.java,v 1.1 2003-11-22 09:05:40 johnsonr Exp $
 */
public class MapAttributes extends AbstractAttributes {
	
	private Map attMap = new HashMap();
	
	
	public void register(Class clazz, Object[] atts) {
		attMap.put(clazz, atts);
	}
	
	public void register(Method method, Object[] atts) {
		attMap.put(method, atts);
	}

	private List getAllAttributes(Object o) {
		Object[] atts = (Object[]) attMap.get(o);
		return (atts == null) ?
			 new LinkedList() :
			 Arrays.asList(atts);
	}
	
	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.Class)
	 */
	public List getAttributes(Class targetClass) {
		return getAllAttributes(targetClass);
	}
	
	private List getMatchingAttributes(Object o, Class filter) {
		List l = getAllAttributes(o);
		if (filter == null)
			return l;
		LinkedList matches = new LinkedList();
		for (int i = 0;	i < l.size(); i++) {
			if (filter.isInstance(l.get(i))) {
				matches.add(l.get(i));
			}
		}
		return matches;
	}

	

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Method)
	 */
	public List getAttributes(Method targetMethod) {
		return getAllAttributes(targetMethod);
	}

	

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Field)
	 */
	public List getAttributes(Field targetField) {
		return getAllAttributes(targetField);
	}


}
