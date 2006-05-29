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

import org.springframework.instrument.classloading.AbstractLoadTimeWeaver;
import org.springframework.instrument.classloading.ClassLoaderWeaver;
import org.springframework.util.ClassUtils;

/**
 * Reflection-based LoadTimeWeaver. Internally it uses a reflection-based ClassLoaderWeaver,
 * to avoid class-loading problems because of the container classes not being accessible
 * inside a web application (by default).
 *
 * @author Costin Leau
 * @since 2.0
 */
public class ReflectiveLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private final ClassLoaderWeaver delegate;


	public ReflectiveLoadTimeWeaver() {
		this(ClassUtils.getDefaultClassLoader());
	}

	public ReflectiveLoadTimeWeaver(ClassLoader classLoader) {
		this.delegate = new ReflectiveClassLoaderWeaver(classLoader);
	}


	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		this.delegate.addClassFileTransformer(classFileTransformer);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.delegate.getInstrumentableClassLoader();
	}

}
