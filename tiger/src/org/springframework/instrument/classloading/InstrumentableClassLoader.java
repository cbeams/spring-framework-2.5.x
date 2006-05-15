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
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.springframework.util.ClassUtils;

/**
 * Simplistic implementation. Usable in tests and standalone.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class InstrumentableClassLoader extends AbstractOverridingClassLoader implements InstrumentedClassLoader {

	private List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();

	private boolean enableAspectJ;

	public InstrumentableClassLoader(ClassLoader parent) {
		super(parent);
	}

	public InstrumentableClassLoader() {
		super(ClassUtils.getDefaultClassLoader());
	}

	public void setAspectJWeavingEnabled(boolean flag) {
		if (flag && !enableAspectJ) {
			enableAspectJ = true;
			transformers.add(new ClassPreProcessorAgentAdapter());
		}
	}

	/**
	 * @return the enableAspectJ
	 */
	public boolean isAspectJWeavingEnabled() {
		return this.enableAspectJ;
	}

	public void addTransformer(ClassFileTransformer cft) {
		this.transformers.add(cft);
	}

	// TODO could have exclusions built on classes known to be entities?

	@Override
	public byte[] transformIfNecessary(String name, String internalName, byte[] bytes) {
		for (ClassFileTransformer cft : transformers) {
			try {
				byte[] transformed = cft.transform(this, internalName, null, null, bytes);
				if (transformed == null) {
					if (debug)
						log.debug(name + " is already weaved by transformer " + cft);
				}
				else {
					if (debug)
						log.debug("Weaving: " + name + " with transformer " + cft);
					bytes = transformed;
				}
			}
			catch (IllegalClassFormatException ex) {
				throw new RuntimeException("Cannot transform", ex);
			}
		}
		return bytes;
	}
}