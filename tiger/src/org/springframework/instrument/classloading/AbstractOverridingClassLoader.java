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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract superclass for class loaders that do <i>not</i> always delegate to
 * their parent loader, as normal class loaders do. This enables, for example,
 * instrumentation to be forced, or a "throwaway" class loader behaviour,
 * where selected classes are loaded by a child loader but not
 * loaded by the parent.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractOverridingClassLoader extends ClassLoader {

	protected final Log log = LogFactory.getLog(getClass());

	// determined at startup since parsing the log hierarchy can be expensive
	protected final boolean debug = log.isDebugEnabled();

	private Set<String> namesSeen = new HashSet<String>();

	private Set<String> exclusions = new HashSet<String>();

	private Set<String> inclusions;

	private final ResourceLoader loader;

	protected AbstractOverridingClassLoader(ClassLoader parent, ResourceLoader loader) {
		super(parent);
		this.loader = loader;
	}

	protected AbstractOverridingClassLoader(ClassLoader parent) {
		this(parent, new DefaultResourceLoader(parent));
	}

	public void addClassNameToExcludeFromUndelegation(String className) {
		exclusions.add(className);
	}

	public void setExplicitInclusions(Collection<String> explicitClassNames) {
		this.inclusions = new HashSet<String>();
		inclusions.addAll(explicitClassNames);
	}

	protected boolean excludeFromUndelegation(String name) {
		return exclusions.contains(name) || (inclusions != null && !inclusions.contains(name));
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!name.startsWith("java") && !namesSeen.contains(name) && !excludeFromUndelegation(name)) {
			namesSeen.add(name);

			String internalName = StringUtils.replace(name, ".", "/");
			
			StringBuilder path = new StringBuilder("classpath:");
			path.append(internalName);
			path.append(".class");

			Resource classRes = loader.getResource(path.toString());
			try {
				// Load the raw bytes
				byte[] bytes = FileCopyUtils.copyToByteArray(classRes.getInputStream());
	
				// Transform if necessary and use the potentially transformed bytes
				byte[] transformed = transformIfNecessary(name, internalName, bytes);
				return defineClass(name, transformed, 0, transformed.length);
			}
			catch (IOException ex) {
				log.warn("Cannot load resource for file: " + classRes, ex);
				throw new ClassNotFoundException(name);
			}
		}
		else {
			// We don't override this class
			return super.loadClass(name);
		}
	}

	/**
	 * Transformation hook to be implemented by subclasses
	 * @param name FQN of class being transformed
	 * @param bytes class tyes
	 * @return transformed bytes. Return same as input bytes if
	 * the transformation produced no changes.
	 */
	public abstract byte[] transformIfNecessary(String name, String internalName, byte[] bytes);

}
