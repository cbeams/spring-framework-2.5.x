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
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.instrument.classloading.InstrumentationRegistry;
import org.springframework.util.ClassUtils;

/**
 * ClassFileTransformer based weaving, allowing for a list of transformers to be
 * applied on class byte array. Normally used inside classloaders.
 * 
 * @author Costin Leau
 * 
 */
public class WeavingTransformer implements InstrumentationRegistry {

	protected final Log logger = LogFactory.getLog(getClass());

	// Determined at startup since parsing the log hierarchy can be expensive.
	protected final boolean debug = logger.isDebugEnabled();

	private List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();

	private ClassLoader classLoader;

	public WeavingTransformer() {
		classLoader = ClassUtils.getDefaultClassLoader();
	}

	public WeavingTransformer(ClassLoader loader) {
		this.classLoader = loader;
	}

	public void addClassFileTransformer(ClassFileTransformer cft) {
		this.transformers.add(cft);
	}

	public byte[] transformIfNecessary(String name, String internalName, byte[] bytes) {
		for (ClassFileTransformer cft : transformers) {
			try {
				byte[] transformed = cft.transform(classLoader, internalName, null, null, bytes);
				if (transformed == null) {
					if (debug)
						logger.debug(name + " is already weaved by transformer " + cft);
				}
				else {
					if (debug)
						logger.debug("Weaving: " + name + " with transformer " + cft);
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
