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

package org.springframework.beans.factory;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 *
 * @author Rod Johnson
 */
public interface Definition {

	BeanDefinition getBeanDefinition();

	// Definition recordable() throws BeansException;

	String getBeanDefinitionName();

	Definition singleton(boolean singleton);

	Definition noAutowire();

	Definition autowireByType();

	Definition autowireByName();

	Definition autowireConstructor();

	Definition prop(String name, Object value);

	//Definition carg(String name, Object value); --REF?

	Definition carg(Object o);

	Definition ref(String name, String bean);

	Definition factoryMethod(String factoryMethod);

	Definition factoryBean(String factoryBean, String factoryMethod);

	/**
	 * @param string
	 * @return
	 */
	Definition destroyMethodName(String string);

	// hotswap

	//advice(Definition d)

	// lookup method

}
