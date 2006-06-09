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
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ClassFileTransformer based weaving, allowing for a list of transformers to be
 * applied on a class byte array. Normally used inside class loaders.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @since 2.0
 */
public class WeavingTransformer {

	protected final Log logger = LogFactory.getLog(getClass());

	// Determined at startup since parsing the log hierarchy can be expensive.
	protected final boolean trace = logger.isTraceEnabled();

	private final ClassLoader classLoader;

	private List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();

	/**
	 * See ClassUtils. We don't depend on that to avoid pulling in more of
	 * Spring
	 * 
	 * @return
	 */
	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = WeavingTransformer.class.getClassLoader();
		}
		return cl;
	}

	public WeavingTransformer() {
		this(null);
	}

	public WeavingTransformer(ClassLoader classLoader) {
		this.classLoader = (classLoader != null ? classLoader : getDefaultClassLoader());
	}

	public void addTransformer(ClassFileTransformer cft) {
		if (logger.isDebugEnabled())
			logger.debug("adding transformer " + cft);
		this.transformers.add(cft);
	}

	public byte[] transformIfNecessary(String className, String internalName, byte[] bytes, ProtectionDomain pd) {
		for (ClassFileTransformer cft : transformers) {
			try {
				byte[] transformed = cft.transform(classLoader, internalName, null, pd, bytes);
				if (transformed == null) {
					if (trace)
						logger.trace("Not Weaving: " + className + " w/ transformer " + cft);
				}
				else {
					if (trace)
						logger.trace("Weaving: " + className + " w/ transformer " + cft);
					bytes = transformed;
				}
			}
			catch (IllegalClassFormatException ex) {
				throw new RuntimeException("Cannot transform", ex);
			}
		}
		return bytes;
	}

	public byte[] transformIfNecessary(String className, byte[] bytes, ProtectionDomain pd) {
		String internalName = className.replace(".", "/");
		return transformIfNecessary(className, internalName, bytes, pd);
	}

}
