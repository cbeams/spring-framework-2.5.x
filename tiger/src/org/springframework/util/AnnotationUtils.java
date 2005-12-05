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

package org.springframework.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * General utility methods for working with annotations.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 */
public abstract class AnnotationUtils {

	/**
	 * Copies the properties of the supplied {@link Annotation} to the supplied target bean. Any
	 * properties defined in <code>excludedProperties</code> will not be copied.
	 * @see BeanWrapperImpl
	 */
	public static void copyPropertiesToBean(Annotation ann, Object bean, String... excludedProperties) {
		Set<String> excluded =  new HashSet<String>(Arrays.asList(excludedProperties));
		Method[] annotationProperties = ann.annotationType().getDeclaredMethods();

		BeanWrapper bw = new BeanWrapperImpl(bean);
		for (Method annotationProperty : annotationProperties) {
			String propertyName = annotationProperty.getName();

			if ((!excluded.contains(propertyName)) && bw.isWritableProperty(propertyName)) {
				Object value = ReflectionUtils.invokeMethod(annotationProperty, ann);
				bw.setPropertyValue(propertyName, value);
			}
		}
	}
	
	/**
	 * Annotations on methods are not inherited by default,
	 * so we need to handle this explicitly
	 * @param m method to look for annotations on
	 * @param c clazz to start with in the hierarchy. Will
	 * go up inheritance hierarchy looking for annotations
	 * on superclasses.
	 * @return the annotation of the given type found,
	 * or null
	 */
	public static <A extends Annotation> A findMethodAnnotation(Class<A> annotationClass, Method m, Class c) {
		if (!annotationClass.isAnnotation()) {
			throw new IllegalArgumentException(annotationClass + " is not an annotation");
		}
		A annotation = m.getAnnotation(annotationClass);

		while (annotation == null) {
			c = c.getSuperclass();
			if (c == null || c.equals(Object.class)) {
				break;
			}
			try {
				m = c.getDeclaredMethod(m.getName(), m.getParameterTypes());
				annotation = m.getAnnotation(annotationClass);
			}
			catch (NoSuchMethodException ex) {
				// We're done
			}
		}
		return annotation;
	}
}
