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

package org.springframework.core.type.asm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Caching implementation of the {@link ClassReaderFactory} interface,
 * caching a ClassReader per Spring Resource handle (i.e. per ".class" file).
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class CachingClassReaderFactory extends SimpleClassReaderFactory {

	private final Map<Resource, ClassReader> classReaderCache = new HashMap<Resource, ClassReader>();


	/**
	 * Create a new CachingClassReaderFactory for the default class loader.
	 */
	public CachingClassReaderFactory() {
		super();
	}

	/**
	 * Create a new CachingClassReaderFactory for the given resource loader.
	 * @param resourceLoader the Spring ResourceLoader to use
	 * (also determines the ClassLoader to use)
	 */
	public CachingClassReaderFactory(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	/**
	 * Create a new CachingClassReaderFactory for the given class loader.
	 * @param classLoader the ClassLoader to use
	 */
	public CachingClassReaderFactory(ClassLoader classLoader) {
		super(classLoader);
	}


	public ClassReader getClassReader(Resource resource) throws IOException {
		synchronized (this.classReaderCache) {
			ClassReader classReader = this.classReaderCache.get(resource);
			if (classReader == null) {
				classReader = super.getClassReader(resource);
				this.classReaderCache.put(resource, classReader);
			}
			return classReader;
		}
	}

}
