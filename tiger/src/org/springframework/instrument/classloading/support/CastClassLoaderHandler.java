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

package org.springframework.instrument.classloading.support;

import java.lang.instrument.ClassFileTransformer;

import org.springframework.instrument.classloading.InstrumentableClassLoader;
import org.springframework.instrument.classloading.ClassLoaderWeaver;

/**
 * InstrumentableClassLoader that holds a narrow reference to the internal
 * classloader delegate. Such class is useful when the container classloader
 * allows the interface to be loaded by the same classloader (the web
 * application has access to the classes loaded by the parent). This class
 * should be always used if possible, instead of ReflectionClassLoaderHandler
 * since it avoids the reflection mechanism.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class CastClassLoaderHandler implements ClassLoaderWeaver {

	private InstrumentableClassLoader classLoader;

	public CastClassLoaderHandler(ClassLoader loader) {
		if (!(loader instanceof InstrumentableClassLoader))
			throw new IllegalArgumentException(loader.getClass().getName() + " is not an implementation of "
					+ InstrumentableClassLoader.class);

		setClassLoader((InstrumentableClassLoader) loader);
	}

	public CastClassLoaderHandler() {
	}

	public void addClassNameToExcludeFromUndelegation(String className) {
		classLoader.addClassNameToExcludeFromUndelegation(className);
	}

	public void addClassFileTransformer(ClassFileTransformer cft) {
		classLoader.addTransformer(cft);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return classLoader;
	}

	/**
	 * @return Returns the classLoader.
	 */
	public InstrumentableClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @param classLoader The classLoader to set.
	 */
	public void setClassLoader(InstrumentableClassLoader loader) {
		this.classLoader = loader;
	}
}
