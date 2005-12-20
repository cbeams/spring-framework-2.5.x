/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.orm.jpa.aspectj;

import javax.persistence.Entity;

/**
 * Concrete aspect that uses the Java Persistence API Entity annotation to identify
 * which classes need autowiring. The bean name to look up will be taken from the
 * Configurable annotation's "name" attribute if specified, otherwise the default
 * will be the fully-qualified name of the class being configured.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see javax.persistence.Entity
 */
public aspect EntityBeanConfigurer extends AbstractBeanConfigurer {
	
	public EntityBeanConfigurer() {
		// Set a custom BeanWiringInfoResolver that is aware of 
		// the Entity annotation
		setBeanWiringInfoResolver(new BeanWiringInfoResolver() {
			public BeanWiringInfo resolve(Object instance) {
				Class<?> clazz = instance.getClass();
				Entity entityAnnotation = clazz.getAnnotation(Entity.class);
				if (entityAnnotation != null) {
					String beanName = entityAnnotation.name();
					if ("".equals(beanName)) {
						beanName = clazz.getName();
					}
					return new BeanWiringInfo(beanName);
				}
				return null;
			}
		});
	}

	/**
	 * The creation of a new bean (an object with the @Entity annotation)
	 */
	protected pointcut beanCreation(Object beanInstance) :
		initialization((@Entity *).new(..)) && this(beanInstance);

}
