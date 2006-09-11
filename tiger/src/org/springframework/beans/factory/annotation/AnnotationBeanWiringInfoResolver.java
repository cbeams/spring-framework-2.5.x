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

import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.beans.factory.wiring.BeanWiringInfoResolver;
import org.springframework.util.Assert;

/**
 * BeanWiringInfoResolver that uses the Configurable annotation to identify
 * which classes need autowiring. The bean name to look up will be taken from
 * the Configurable annotation if specified; otherwise the default will be
 * the fully-qualified name of the class being configured.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see Configurable
 * @see org.springframework.beans.factory.wiring.ClassNameBeanWiringInfoResolver
 */
public class AnnotationBeanWiringInfoResolver implements BeanWiringInfoResolver {

	private static final String CGLIB_MAGIC = "EnhancerByCGLIB";

	public BeanWiringInfo resolveWiringInfo(Object beanInstance) {
		Assert.notNull(beanInstance, "Bean instance must not be null");
		Class<?> clazz = beanInstance.getClass();
		Configurable configurableAnnotation = clazz.getAnnotation(Configurable.class);
		if (configurableAnnotation != null) {
			if (!Autowire.NO.equals(configurableAnnotation.autowire())) {
				return new BeanWiringInfo(
						configurableAnnotation.autowire().value(), configurableAnnotation.dependencyCheck());
			}
			else {
				// Bean name may be explicit or defaulted to FQN.
				String beanName =
						("".equals(configurableAnnotation.value())) ? getUserClassName(clazz) : configurableAnnotation.value();
				return new BeanWiringInfo(beanName);
			}
		}
		return null;
	}
	
	/**
	 * Copes with CGLIB generated subclasses and ensures that the user's class name
	 * is returned instead.
	 */
	private String getUserClassName(Class beanClass) {
		String className = beanClass.getName();
		if (className.indexOf(CGLIB_MAGIC) != -1) {
			className = beanClass.getSuperclass().getName();
		}
		return className;
	}

}
