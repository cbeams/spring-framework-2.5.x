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

package org.springframework.beans.factory.wiring;

import org.springframework.util.Assert;

/**
 * Simple default implementation, which looks for a bean with the same name as
 * the fully-qualified class name. This is the default name of the bean in a
 * Spring XML file if the id attribute is not used.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ClassNameBeanWiringInfoResolver implements BeanWiringInfoResolver {

	public BeanWiringInfo resolveWiringInfo(Object beanInstance) {
		Assert.notNull(beanInstance, "Bean instance must not be null");
		return new BeanWiringInfo(beanInstance.getClass().getName());
	}

}
