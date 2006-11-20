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

import java.lang.instrument.ClassFileTransformer;

import org.springframework.core.OverridingClassLoader;

/**
 * Simplistic implementation of an instrumentable ClassLoader.
 * Usable in tests and standalone environments.
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @since 2.0
 */
public class SimpleInstrumentableClassLoader extends OverridingClassLoader  {

	private final WeavingTransformer weavingTransformer;


	/**
	 * Create a new SimpleLoadTimeWeaver for the given class loader.
	 * @param parent the ClassLoader to build an simple instrumentable
	 * ClassLoader for
	 */
	public SimpleInstrumentableClassLoader(ClassLoader parent) {
		super(parent);
		this.weavingTransformer = new WeavingTransformer(parent);
	}


	/**
	 * Add a class file transformer to be applied by this ClassLoader.
	 * @param transformer the class file transformer to register
	 */
	public void addTransformer(ClassFileTransformer transformer) {
		this.weavingTransformer.addTransformer(transformer);
	}


	@Override
	protected byte[] transformIfNecessary(String name, byte[] bytes) {
		return this.weavingTransformer.transformIfNecessary(name, bytes);
	}

}
