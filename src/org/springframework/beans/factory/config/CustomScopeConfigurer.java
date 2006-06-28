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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Simple {@link BeanFactoryPostProcessor} implementation that effects the
 * registration of custom {@link Scope Scope(s)} in a {@link ConfigurableBeanFactory}.
 * 
 * <p>Will register all of the supplied {@link #setScopes(java.util.Map) scopes}
 * with the {@link ConfigurableListableBeanFactory} that is passed to the
 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)} method.
 *
 * @author Rick Evans
 * @since 2.0
 */
public class CustomScopeConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	private int order = Ordered.LOWEST_PRECEDENCE;

	private Map scopes;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * The custom scopes that are to be registered.
	 * <p>The keys of the supplied {@link Map} <b>must</b> be {@link String Strings};
	 * the values can be either the fully qualified classname of the {@link Scope} type
	 * that is to be instantiated, the actual {@link Class} of the {@link Scope} type
	 * that is to be instantiated, or an actual custom {@link Scope} instance itself.
	 * @param scopes the custom scopes that are to be registered
	 */
	public void setScopes(Map scopes) {
		this.scopes = scopes;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.scopes != null && this.scopes.size() > 0) {
			for (Iterator it = this.scopes.keySet().iterator(); it.hasNext();) {
				String scopeName = resolveScopeName(it.next());
				Scope scope = resolveScope(this.scopes.get(scopeName));
				beanFactory.registerScope(scopeName, scope);
			}
		}
	}


	private String resolveScopeName(Object value) {
		if (value instanceof String) {
			String scopeName = (String) value;
			if (StringUtils.hasText(scopeName)) {
				return scopeName;
			}
		}
		throw new BeanInitializationException(
				"Invalid scope name [" + value + "] for custom scope registration - " +
						"needs to be a non-whitespace-only String");
	}

	private Scope resolveScope(Object value) {
		if (value instanceof String) {
			String className = (String) value;
			try {
				Class scopeType = ClassUtils.forName(className, this.beanClassLoader);
				return instantiateScope(scopeType);
			}
			catch (ClassNotFoundException ex) {
				throw new BeanInitializationException(
						"Could not load required type [" + className + "] for custom scope", ex);
			}
		} else if (value instanceof Class) {
			return instantiateScope((Class) value);
		} else if (value instanceof Scope) {
			return (Scope) value;
		} else {
			throw new BeanInitializationException(
					"Invalid scope value [" + value + "] for custom scope registration - " +
							"needs to be a fully qualified classname, Class, or Scope instance");
		}
	}


	private static Scope instantiateScope(Class scopeType) {
		if (Scope.class.isAssignableFrom(scopeType)) {
			return (Scope) BeanUtils.instantiateClass(scopeType);
		} else {
			throw new BeanInitializationException(
					"Invalid class type [" + scopeType + "] for custom scope : must be " +
							"assignable to the [" + ClassUtils.getQualifiedName(Scope.class) + "] type");
		}
	}

}
