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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Subinterface of BeanPostProcessor that adds a before-destruction callback.
 *
 * <p>The typical usage will be to invoke custom destruction callbacks on
 * specific bean types, matching corresponding initialization callbacks.
 *
 * @author Juergen Hoeller
 * @since 09.04.2004
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance before
	 * its destruction. Can invoke custom destruction callbacks.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeforeDestruction(Object bean, String name) throws BeansException;

}
