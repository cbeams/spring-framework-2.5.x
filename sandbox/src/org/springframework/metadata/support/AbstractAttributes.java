/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.metadata.Attributes;

/**
 * Convenient superclass for Attributes implementations.
 * Implements filtering and saves attribute packages.
 * TODO could implement caching here for efficiency,
 * or add a caching decorator (probably a better idea)
 * @author Rod Johnson
 * @version $Id: AbstractAttributes.java,v 1.1 2003-11-22 09:05:44 johnsonr Exp $
 */
public abstract class AbstractAttributes implements Attributes {
	
	private String[] attributePackages = new String[0];

	
	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.Class, java.lang.Class)
	 */
	public final List getAttributes(Class targetClass, Class filter) {
		return filter(getAttributes(targetClass), filter);
	}

	/**
	 * Filter these attributes to those matching the filter type
	 * @param l
	 * @param filter
	 * @return
	 */
	private List filter(List l, Class filter) {
		if (filter == null)
			return l;
			
		List matches = new LinkedList();
		for (int i = 0; i < l.size(); i++) {
			if (filter.isInstance(l.get(i))) {
				matches.add(l.get(i));
			}
		}
		return matches;
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Method, java.lang.Class)
	 */
	public final List getAttributes(Method targetMethod, Class filter) {
		return filter(getAttributes(targetMethod), filter);
	}


	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Field, java.lang.Class)
	 */
	public final List getAttributes(Field targetField, Class filter) {
		return filter(getAttributes(targetField), filter);
	}

	/**
	 * @see org.springframework.metadata.Attributes#setAttributePackages(java.lang.String[])
	 */
	public void setAttributePackages(String[] packages) {
		this.attributePackages = packages;
	}
	
	protected String[] getAttributePackages() {
		return this.attributePackages;
	}

}
