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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;

/**
 * Editor for java.lang.Class, to directly feed a Class property
 * instead of using a String class name property.
 *
 * <p>Also supports "java.lang.String[]"-style array class names,
 * in contrast to the standard Class.forName method.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see java.lang.Class
 * @see java.lang.Class#forName
 */
public class ClassEditor extends PropertyEditorSupport {

	public static final String ARRAY_SUFFIX = "[]";

	public void setAsText(String text) throws IllegalArgumentException {
		Class clazz = null;
		try {
			if (text.endsWith(ARRAY_SUFFIX)) {
				// special handling for array class names
				String elementClassName = text.substring(0, text.length() - ARRAY_SUFFIX.length());
				Class elementClass = Class.forName(elementClassName, true, Thread.currentThread().getContextClassLoader());
				clazz = Array.newInstance(elementClass, 0).getClass();
			}
			else {
				clazz = Class.forName(text, true, Thread.currentThread().getContextClassLoader());
			}
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Invalid class name: " + ex.getMessage());
		}
		setValue(clazz);
	}

	public String getAsText() {
		Class clazz = (Class) getValue();
		return (clazz.isArray() ? clazz.getComponentType().getName() + ARRAY_SUFFIX : clazz.getName());
	}

}
