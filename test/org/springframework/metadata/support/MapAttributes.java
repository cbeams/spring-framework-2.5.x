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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Convenient class for simulating attributes.
 * @author Rod Johnson
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
