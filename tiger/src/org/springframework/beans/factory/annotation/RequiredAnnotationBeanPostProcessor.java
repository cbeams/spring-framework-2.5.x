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

package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * A {@link BeanFactoryPostProcessor} implementation that ensures that JavaBean
 * properties on beans that are marked with an (arbitrary) {@link Annotation}
 * are actually (configured to be) dependency-injected with a value.
 *
 * <p>The motivation for the existence of this BeanPostProcessor is to allow
 * developers to annotate the setter properties of their own classes with an
 * arbitrary JDK 1.5 annotation to indicate that the container must check
 * for the configuration of a dependency injected value. This neatly pushes
 * responsibility for such checking onto the container (where it arguably belongs),
 * and obviates the need (<b>in part</b>) for a developer to code a method that
 * simply checks that all required properties have actually been set.
 *
 * <p>Please note that an 'init' method may still need to implemented (and may
 * still be desirable), because all that this class does is enforce that a
 * 'required' property has actually been configured with a value. It does
 * <b>not</b> check anything else... in particular, it does not check that a
 * configured value is not <code>null</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see Required
 */
public class RequiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

	private Class<? extends Annotation> requiredAnnotationType = Required.class;


	/**
	 * Set the 'required' annotation type.
	 * The default required annotation type is the {@link Required} annotation.
	 * <p>This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a property value
	 * is required.
	 * @param requiredAnnotationType the desired annotation type
	 */
	public void setRequiredAnnotationType(Class<? extends Annotation> requiredAnnotationType) {
		Assert.notNull(requiredAnnotationType, "requiredAnnotationType must not be null");
		this.requiredAnnotationType = requiredAnnotationType;
	}

	/**
	 * Return the 'required' annotation type.
	 */
	protected Class<? extends Annotation> getRequiredAnnotationType() {
		return requiredAnnotationType;
	}


	public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName)
			throws BeansException {

		Class targetClass = bean.getClass();
		PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(targetClass);
		List<String> invalidProperties = new ArrayList<String>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (isRequiredProperty(propertyDescriptor) && !pvs.contains(propertyDescriptor.getName())) {
				invalidProperties.add(propertyDescriptor.getName());
			}
		}
		if (!invalidProperties.isEmpty()) {
			throw new BeanInitializationException(buildExceptionMessage(invalidProperties, beanName));
		}
		return pvs;
	}

	/**
	 * Is the supplied property required to have a value (that is, to be dependency-injected)?
	 * <p>This implementation looks for the existence of a
	 * {@link #setRequiredAnnotationType "required" annotation}
	 * on the supplied {@link PropertyDescriptor property}.
	 * @param propertyDescriptor the target PropertyDescriptor (never <code>null</code>)
	 * @return <code>true</code> if the supplied property has been marked as being required;
	 * <code>false</code> if not, or if the supplied property does not have a setter method
	 */
	protected boolean isRequiredProperty(PropertyDescriptor propertyDescriptor) {
		Method setter = propertyDescriptor.getWriteMethod();
		return (setter != null && AnnotationUtils.getAnnotation(setter, getRequiredAnnotationType()) != null);
	}

	/**
	 * Build an exception message for the given list of invalid properties.
	 * @param invalidProperties the list of names of invalid properties
	 * @param beanName the name of the bean
	 * @return the exception message
	 */
	private String buildExceptionMessage(List<String> invalidProperties, String beanName) {
		StringBuilder sb = new StringBuilder();
		int size = invalidProperties.size();
		sb.append(size == 1 ? "Property" : "Properties");
		for (int i = 0; i < size; i++) {
			String propertyName = invalidProperties.get(i);
			if (i > 0) {
				if (i == (size - 1)) {
					sb.append(" and");
				}
				else {
					sb.append(",");
				}
			}
			sb.append(" '");
			sb.append(propertyName);
			sb.append('\'');
		}
		sb.append(size == 1 ? " is required" : " are required");
		sb.append(" for bean '");
		sb.append(beanName);
		sb.append("'.");
		return sb.toString();
	}

}
