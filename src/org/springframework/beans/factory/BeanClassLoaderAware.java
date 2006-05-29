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

package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that wish to be aware of the bean class
 * loader, that is, the class loader uses by the present bean factory to load
 * bean classes.
 *
 * <p>This is mainly intended to be implemented by framework classes which
 * have to pick up application classes by name despite themselves potentially
 * being loaded from a shared class loader.
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
public interface BeanClassLoaderAware {

	/**
	 * Callback that supplies the bean class loader to a bean instance.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's <code>afterPropertiesSet</code> or a
	 * custom init-method.
	 * @param classLoader owning class loader (may be <code>null</code> in
	 * case of the caller class loader to be used)
	 */
	void setBeanClassLoader(ClassLoader classLoader);

}
