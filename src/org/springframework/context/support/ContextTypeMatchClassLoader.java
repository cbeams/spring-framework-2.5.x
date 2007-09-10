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

package org.springframework.context.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.OverridingClassLoader;

/**
 * Special variant of an overriding ClassLoader, used for temporary
 * type matching in AbstractApplicationContext. Redefines classes
 * from a cached byte array for every <code>loadClass</code> call
 * in order to pick up recently loaded types in the parent ClassLoader.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AbstractApplicationContext
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setTempClassLoader
 */
class ContextTypeMatchClassLoader extends OverridingClassLoader {

	/** Cache for byte array per class name */
	private final Map bytesCache = new HashMap();


	public ContextTypeMatchClassLoader(ClassLoader parent) {
		super(parent);
		excludePackage("org.springframework");
	}


	protected Class loadClassForOverriding(String name) throws ClassNotFoundException {
		byte[] bytes = (byte[]) this.bytesCache.get(name);
		if (bytes == null) {
			bytes = loadBytesForClass(name);
			if (bytes != null) {
				this.bytesCache.put(name, bytes);
			}
			else {
				return null;
			}
		}
		return defineClass(name, bytes, 0, bytes.length);
	}

}
