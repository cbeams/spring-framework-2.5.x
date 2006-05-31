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

package org.springframework.instrument.classloading;

import org.springframework.util.ClassUtils;

/**
 * Abstract base implementation of the LoadTimeWeaver interface,
 * providing access to the current context class loader and
 * creating a simple throwaway class loader by default.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractLoadTimeWeaver implements LoadTimeWeaver {

	/**
	 * Return the current context class loader, falling back to a
	 * default class loader.
	 * @return the context class loader (never <code>null</code>)
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 */
	protected ClassLoader getContextClassLoader() {
		return ClassUtils.getDefaultClassLoader();
	}

	/**
	 * Creates a SimpleThrowawayClassLoader for the current context class loader.
	 * @see #getContextClassLoader()
	 * @see SimpleThrowawayClassLoader
	 */
	public ClassLoader getThrowawayClassLoader() {
		return new SimpleThrowawayClassLoader(getContextClassLoader());
	}

}
