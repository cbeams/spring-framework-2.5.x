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

package org.springframework.metadata.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.metadata.Attributes;

/**
 * Convenient superclass for Attributes implementations.
 * Implements filtering and saves attribute packages.
 * TODO could implement caching here for efficiency,
 * or add a caching decorator (probably a better idea)
 * @author Rod Johnson
 */
public abstract class AbstractAttributes implements Attributes {
	
	//private String[] attributePackages = new String[0];

	
	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.Class, java.lang.Class)
	 */
	public final Collection getAttributes(Class targetClass, Class filter) {
		return filter(getAttributes(targetClass), filter);
	}

	/**
	 * Filter these attributes to those matching the filter type
	 * @param l
	 * @param filter
	 * @return
	 */
	private Collection filter(Collection c, Class filter) {
		if (filter == null)
			return c;
			
		List matches = new LinkedList();
		for (Iterator itr = c.iterator(); itr.hasNext(); ) {
			Object next = itr.next();
			if (filter.isInstance(next)) {
				matches.add(next);
			}
		}
		return matches;
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Method, java.lang.Class)
	 */
	public final Collection getAttributes(Method targetMethod, Class filter) {
		return filter(getAttributes(targetMethod), filter);
	}


	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Field, java.lang.Class)
	 */
	public final Collection getAttributes(Field targetField, Class filter) {
		return filter(getAttributes(targetField), filter);
	}

	/**
	 * @see org.springframework.metadata.Attributes#setAttributePackages(java.lang.String[])
	 */
	//public void setAttributePackages(String[] packages) {
	//	this.attributePackages = packages;
	//}
	
	//protected String[] getAttributePackages() {
	//	return this.attributePackages;
	//}

}
