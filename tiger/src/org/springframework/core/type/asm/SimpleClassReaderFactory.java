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
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

/**
 * Simple implementation of the {@link ClassReaderFactory} interface,
 * creating a new ClassReader for every request.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class SimpleClassReaderFactory implements ClassReaderFactory {

	private final ResourceLoader resourceLoader;


	/**
	 * Create a new SimpleClassReaderFactory for the default class loader.
	 */
	public SimpleClassReaderFactory() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new SimpleClassReaderFactory for the given resource loader.
	 * @param resourceLoader the Spring ResourceLoader to use
	 * (also determines the ClassLoader to use)
	 */
	public SimpleClassReaderFactory(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}

	/**
	 * Create a new SimpleClassReaderFactory for the given class loader.
	 * @param classLoader the ClassLoader to use
	 */
	public SimpleClassReaderFactory(ClassLoader classLoader) {
		this.resourceLoader =
				(classLoader != null ? new DefaultResourceLoader(classLoader) : new DefaultResourceLoader());
	}


	public ClassReader getClassReader(String className) throws IOException {
		String resourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath(className) + ClassUtils.CLASS_FILE_SUFFIX;
		return getClassReader(this.resourceLoader.getResource(resourcePath));
	}

	public ClassReader getClassReader(Resource resource) throws IOException {
		InputStream is = resource.getInputStream();
		try {
			return new ClassReader(is);
		}
		finally {
			is.close();
		}
	}

}
