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

package org.springframework.context.annotation;

import java.beans.Introspector;
import java.util.Map;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.support.BeanNameGenerator} implementation
 * for bean classes annotated with the @Component annotation.
 *
 * <p>If the annotation's value doesn't indicate a bean name, an appropriate name
 * will be built based on the short name of the class (with the first letter
 * lower-cased). For example:
 *
 * <p><code>com.xyz.FooServiceImpl -&gt; fooServiceImpl</code>
 *
 * <p>Note that generated bean names have to be unique, hence the actual
 * runtime names may have a "#1", "#2", etc suffix appended if the raw
 * names were not unique already.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 * @see org.springframework.stereotype.Component#value()
 */
public class ComponentBeanNameGenerator implements BeanNameGenerator {

	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		String rawBeanName = null;
		if (definition instanceof AnnotatedBeanDefinition) {
			AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
			Map<String, Object> attributes =
					annDef.getMetadata().getAnnotationAttributes(Component.class.getName());
			if (attributes != null) {
				rawBeanName = (String) attributes.get("value");
			}
		}
		if (!StringUtils.hasLength(rawBeanName)) {
			String shortClassName = ClassUtils.getShortName(definition.getBeanClassName());
			rawBeanName = Introspector.decapitalize(shortClassName);
		}
		String id = rawBeanName;
		int counter = 0;
		while (registry.containsBeanDefinition(id)) {
			counter++;
			id = rawBeanName + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + counter;
		}
		return id;
	}

}
