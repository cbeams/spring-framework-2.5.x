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

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

/**
 * Extension of the {@link org.springframework.beans.factory.support.RootBeanDefinition}
 * class, adding support for annotation metadata exposed through the
 * {@link AnnotatedBeanDefinition} interface.
 *
 * <p>This RootBeanDefinition variant is mainly useful for testing code that expects
 * to operate on an AnnotatedBeanDefinition, for example strategy implementations
 * in Spring's component scanning support (where the default definition class is
 * {@link org.springframework.context.annotation.ScannedRootBeanDefinition},
 * which also implements the AnnotatedBeanDefinition interface).
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see AnnotatedBeanDefinition#getMetadata()
 * @see org.springframework.core.type.StandardAnnotationMetadata
 */
public class AnnotatedRootBeanDefinition extends RootBeanDefinition implements AnnotatedBeanDefinition {

	private final AnnotationMetadata annotationMetadata;


	/**
	 * Create a new AnnotatedRootBeanDefinition for the given bean class.
	 * @param beanClass the loaded bean class
	 */
	public AnnotatedRootBeanDefinition(Class beanClass) {
		super(beanClass);
		this.annotationMetadata = new StandardAnnotationMetadata(beanClass);
	}


	public final AnnotationMetadata getMetadata() {
		 return this.annotationMetadata;
	}

}
