/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Convenient class for simulating attributes.
 * @author Rod Johnson
 * @version $Id: MapAttributes.java,v 1.1 2003-12-15 14:46:34 johnsonr Exp $
 */
public class MapAttributes extends AbstractAttributes {
	
	private Map attMap = new HashMap();
	
	
	public void register(Class clazz, Object[] atts) {
		attMap.put(clazz, atts);
	}
	
	public void register(Method method, Object[] atts) {
		attMap.put(method, atts);
	}

	private Collection getAllAttributes(Object o) {
		Object[] atts = (Object[]) attMap.get(o);
		return (atts == null) ?
			 new LinkedList() :
			 Arrays.asList(atts);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Field)
	 */
	public Collection getAttributes(Field targetField) {
		return getAllAttributes(targetField);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.Class)
	 */
	public Collection getAttributes(Class targetClass) {
		return getAllAttributes(targetClass);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Method)
	 */
	public Collection getAttributes(Method targetMethod) {
		return getAllAttributes(targetMethod);
	}


}
