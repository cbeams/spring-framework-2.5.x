package org.springframework.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * General utility methods for working with annotations.
 *
 * @author Rob Harrop
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

		for (int i = 0; i < annotationProperties.length; i++) {
			Method annotationProperty = annotationProperties[i];
			String propertyName = annotationProperty.getName();

			if((!excluded.contains(propertyName)) && bw.isWritableProperty(propertyName)) {
				Object value = ReflectionUtils.invokeMethod(annotationProperty, ann);
				bw.setPropertyValue(propertyName, value);
			}
		}
	}
}
