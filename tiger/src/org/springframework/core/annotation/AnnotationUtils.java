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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * General utility methods for working with annotations.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AnnotationUtils {

	/**
	 * Annotations on methods are not inherited by default,
	 * so we need to handle this explicitly.
	 * @param annotationClass the annotation class to look for
	 * @param method the method to look for annotations on
	 * @param clazz the class to start with in the hierarchy. Will go up
	 * inheritance hierarchy looking for annotations on superclasses.
	 * @return the annotation of the given type found, or <code>null</code>
	 */
	public static <A extends Annotation> A findMethodAnnotation(
			Class<A> annotationClass, Method method, Class clazz) {

		if (!annotationClass.isAnnotation()) {
			throw new IllegalArgumentException(annotationClass + " is not an annotation");
		}
		A annotation = method.getAnnotation(annotationClass);

		Class cl = clazz;
		while (annotation == null) {
			cl = cl.getSuperclass();
			if (cl == null || cl.equals(Object.class)) {
				break;
			}
			try {
				method = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
				annotation = method.getAnnotation(annotationClass);
			}
			catch (NoSuchMethodException ex) {
				// We're done
			}
		}
		return annotation;
	}

}
