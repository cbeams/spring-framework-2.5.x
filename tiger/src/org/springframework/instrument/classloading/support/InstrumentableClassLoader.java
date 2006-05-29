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

import org.springframework.instrument.classloading.AbstractOverridingClassLoader;
import org.springframework.util.ClassUtils;

/**
 * Simplistic implementation. Usable in tests and standalone.
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @since 2.0
 */
public class InstrumentableClassLoader extends AbstractOverridingClassLoader {

	private final AspectJWeavingTransformer weavingTransformer;


	public InstrumentableClassLoader(ClassLoader parent) {
		super(parent);
		this.weavingTransformer = new AspectJWeavingTransformer(parent);
	}

	public InstrumentableClassLoader() {
		super(ClassUtils.getDefaultClassLoader());
		this.weavingTransformer = new AspectJWeavingTransformer(ClassUtils.getDefaultClassLoader());
	}


	public void addTransformer(ClassFileTransformer cft) {
		this.weavingTransformer.addClassFileTransformer(cft);
	}

	// TODO could have exclusions built on classes known to be entities?

	public AspectJWeavingTransformer getWeavingTransformer() {
		return weavingTransformer;
	}

	@Override
	public byte[] transformIfNecessary(String name, String internalName, byte[] bytes) {
		return weavingTransformer.transformIfNecessary(name, bytes, null);
	}

}
