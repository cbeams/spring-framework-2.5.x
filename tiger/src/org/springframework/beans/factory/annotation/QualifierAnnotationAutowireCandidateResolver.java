/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.AutowireCandidateQualifier;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Autowire candidate resolver that matches bean definition qualifiers
 * against qualifier annotations on the field or parameter to be autowired.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 * @see Qualifier
 */
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver {

	private final Set<Class<? extends Annotation>> qualifierTypes;


	public QualifierAnnotationAutowireCandidateResolver() {
		this(Qualifier.class);
	}

	public QualifierAnnotationAutowireCandidateResolver(Class<? extends Annotation> qualifierType) {
		this.qualifierTypes = new HashSet();
		this.qualifierTypes.add(qualifierType);
	}

	public QualifierAnnotationAutowireCandidateResolver(Set<Class<? extends Annotation>> qualifierTypes) {
		Assert.notNull(qualifierTypes, "qualifierTypes must not be null");
		this.qualifierTypes = qualifierTypes;
	}


	/**
	 * Register the given type to be used as a qualifier when autowiring.
	 * @param qualifierType the annotation type to register
	 */
	public void addQualifierType(Class qualifierType) {
		if (!Annotation.class.isAssignableFrom(qualifierType)) {
			throw new IllegalArgumentException("qualifierType must be an annotation type");
		}
		this.qualifierTypes.add(qualifierType);
	}

	/**
	 * Determine if the provided bean definition is an autowire candidate.
	 * <p>To be considered a candidate the bean's <em>autowire-candidate</em>
	 * attribute must not have been set to 'false'. Also any annotations
	 * on the field or parameter to be autowired that are recognized by
	 * this bean factory as <em>qualifiers</em> must also be present as
	 * qualifiers on the bean definition. If the annotation further specifies
	 * any attributes (including a simple "value"), then the qualifier on the
	 * bean definition must also match all specified attribute values.</p>
	 */
	public boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
			DependencyDescriptor descriptor, TypeConverter typeConverter) {

		if (!mbd.isAutowireCandidate()) {
			// if explicitly set to false, then do not proceed with qualifier check
			return false;
		}
		if (descriptor == null || descriptor.getAnnotations() == null) {
			// isAutowireCandidate must have been true and no qualification necessary
			return true;
		}
		Annotation[] annotations = (Annotation[]) descriptor.getAnnotations();
		for (Annotation annotation : annotations) {
			Class<? extends Annotation> type = annotation.annotationType();
			// check if this annotation is a recognized qualifier type
			if (isQualifier(type)) {
				AutowireCandidateQualifier qualifier = mbd.getQualifier(type.getName());
				Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
				if (qualifier == null) {
					qualifier = mbd.getQualifier(ClassUtils.getShortName(type));
					if (qualifier == null) {
						// no qualifier present, fall back on beanName match
						if (attributes.size() == 1 && beanNameResolves(beanName, annotation)) {
							continue;
						}
						return false;
					}
				}
				for (Iterator<Map.Entry<String, Object>> it = attributes.entrySet().iterator(); it.hasNext();) {
					Map.Entry<String, Object> entry = it.next();
					String attributeName = entry.getKey();
					Object expectedValue = entry.getValue();
					Object actualValue = qualifier.getAttribute(attributeName);
					if (actualValue == null) {
						// if relying on default value, then the qualifier need not be specified
						Object defaultValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
						if (defaultValue == null || ((defaultValue instanceof String) && ((String) defaultValue).length() == 0)) {
							if (attributeName.equals("value") && beanNameResolves(beanName, annotation)) {
								continue;
							}
						}
						actualValue = defaultValue;
					}
					else {
						actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
					}
					if (!actualValue.equals(expectedValue)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks if an annotation type is a recognized qualifier type
	 */
	private boolean isQualifier(Class<? extends Annotation> annotationType) {
		for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
			if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
				return true;
			}
		}
		return false;
	}


	private static boolean beanNameResolves(String beanName, Annotation annotation) {
		Object value = AnnotationUtils.getValue(annotation, "value");
		if (value == null || !(value instanceof String)) {
			return false;
		}
		return (value.equals(beanName));
	}

}
