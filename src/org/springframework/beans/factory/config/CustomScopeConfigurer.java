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

package org.springframework.beans.factory.config;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;

/**
 * Simple {@link BeanFactoryPostProcessor} implementation that registers
 * custom {@link Scope Scope(s)} with the containing {@link ConfigurableBeanFactory}.
 *
 * <p>Will register all of the supplied {@link #setScopes(java.util.Map) scopes}
 * with the {@link ConfigurableListableBeanFactory} that is passed to the
 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)} method.
 *
 * <p>This class allows for <i>declarative</i> registration of custom scopes.
 * Alternatively, consider implementing a custom {@link BeanFactoryPostProcessor}
 * that calls {@link ConfigurableBeanFactory#registerScope} programmatically.
 *
 * @author Rick Evans
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 */
public class CustomScopeConfigurer implements BeanFactoryPostProcessor, Ordered {

	private int order = Ordered.LOWEST_PRECEDENCE;

	private Map scopes;


	/**
	 * Specify the custom scopes that are to be registered.
	 * <p>The keys indicate the scope names (of type String); each value
	 * is expected to be the corresponding custom {@link Scope} instance.
	 */
	public void setScopes(Map scopes) {
		this.scopes = scopes;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.scopes != null) {
			for (Iterator it = this.scopes.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				Object key = entry.getKey();
				if (!(key instanceof String)) {
					throw new IllegalArgumentException(
							"Invalid scope key [" + key + "]: only Strings allowed");
				}
				Object value = entry.getValue();
				if (!(value instanceof Scope)) {
					throw new IllegalArgumentException("Mapped value [" + value + "] for scope key [" +
							key + "] is not of required type [" + Scope.class.getName() + "]");
				}
				beanFactory.registerScope((String) key, (Scope) value);
			}
		}
	}

}
