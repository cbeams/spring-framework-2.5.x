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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract superclass for class loaders that do <i>not</i> always delegate to
 * their parent loader, as normal class loaders do. This enables, for example,
 * instrumentation to be forced, or a "throwaway" class loader behavior, where
 * selected classes are loaded by a child loader but not loaded by the parent.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractOverridingClassLoader extends ClassLoader {

	protected final Log logger = LogFactory.getLog(getClass());

	// Determined at startup since parsing the log hierarchy can be expensive.
	protected final boolean debug = logger.isDebugEnabled();

	private final Set<String> exclusions = new HashSet<String>();

	private final Set<String> inclusions = new HashSet<String>();

	private final Set<String> namesSeen = new HashSet<String>();


	protected AbstractOverridingClassLoader(ClassLoader parent) {
		super(parent);
	}


	public void addClassNameToExcludeFromUndelegation(String className) {
		this.exclusions.add(className);
	}

	public void addClassNameToExplicitlyInclude(String className) {
		this.inclusions.add(className);
	}

	protected boolean excludeFromUndelegation(String name) {
		return (this.exclusions.contains(name) || !this.inclusions.contains(name));
	}


	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!name.startsWith("java") && !this.namesSeen.contains(name) && !excludeFromUndelegation(name)) {
			this.namesSeen.add(name);
			String internalName = StringUtils.replace(name, ".", "/");
			InputStream is = getParent().getResourceAsStream(internalName + ".class");
			if (is == null) {
				throw new ClassNotFoundException(name);
			}
			try {
				// Load the raw bytes.
				byte[] bytes = FileCopyUtils.copyToByteArray(is);
				// Transform if necessary and use the potentially transformed bytes.
				byte[] transformed = transformIfNecessary(name, internalName, bytes);
				return defineClass(name, transformed, 0, transformed.length);
			}
			catch (IOException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Cannot load resource for class [" + name + "]", ex);
				}
				throw new ClassNotFoundException(name);
			}
		}
		else {
			// We don't override this class.
			return super.loadClass(name);
		}
	}

	/**
	 * Transformation hook to be implemented by subclasses.
	 * @param name FQN of class being transformed
	 * @param bytes class bytes
	 * @return transformed bytes. Return same as input bytes if
	 * the transformation produced no changes.
	 */
	protected abstract byte[] transformIfNecessary(String name, String internalName, byte[] bytes);

}
