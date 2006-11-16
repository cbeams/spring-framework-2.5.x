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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * Abstract superclass for <code>ClassLoaders</code> that do <i>not</i>
 * always delegate to their parent loader, as normal class loaders do.
 * This enables, for example, instrumentation to be forced, or a "throwaway"
 * class loading behavior, where selected classes are loaded by a child loader
 * but not loaded by the parent.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractOverridingClassLoader extends ClassLoader {

	private static final String CLASS_FILE_SUFFIX = ".class";

	private final Set<String> excludedPackages = Collections.synchronizedSet(new HashSet<String>());

	private final Set<String> excludedClasses = Collections.synchronizedSet(new HashSet<String>());


	/**
	 * Create a new AbstractOverridingClassLoader for the given class loader.
	 * @param parent the ClassLoader to build an overriding ClassLoader for
	 */
	protected AbstractOverridingClassLoader(ClassLoader parent) {
		super(parent);
		this.excludedPackages.add("java.");
		this.excludedPackages.add("javax.");
	}


	/**
	 * Add a package name to exclude from overriding.
	 * <p>Any class whose fully-qualified name starts with the name registered
	 * here will be handled by the parent ClassLoader in the usual fashion.
	 * @param packageName the package name to exclude
	 */
	public void excludePackage(String packageName) {
		Assert.notNull(packageName, "Package name must not be null");
		this.excludedPackages.add(packageName);
	}

	/**
	 * Add a class name to exclude from overriding.
	 * <p>Any class name registered here will be handled by
	 * the parent ClassLoader in the usual fashion.
	 * @param className the class name to exclude
	 */
	public void excludeClass(String className) {
		Assert.notNull(className, "Class name must not be null");
		this.excludedClasses.add(className);
	}


	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> result = null;

		if (isEligibleForOverriding(name)) {
			result = findLoadedClass(name);
			if (result == null) {
				String internalName = name.replace('.', '/') + CLASS_FILE_SUFFIX;
				InputStream is = getParent().getResourceAsStream(internalName);
				if (is != null) {
					try {
						// Load the raw bytes.
						byte[] bytes = FileCopyUtils.copyToByteArray(is);
						// Transform if necessary and use the potentially transformed bytes.
						byte[] transformed = transformIfNecessary(name, bytes);
						result = defineClass(name, transformed, 0, transformed.length);
					}
					catch (IOException ex) {
						throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
					}
				}
			}
		}

		if (result != null) {
			if (resolve) {
				resolveClass(result);
			}
			return result;
		}
		else {
			return super.loadClass(name, resolve);
		}
	}

	/**
	 * Determine whether the given class name is eligible for overriding
	 * by this class loader.
	 * <p>The default implementation excludes all specified packages
	 * and classes.
	 * @param className the class name to check
	 * @see #excludePackage
	 * @see #excludeClass
	 */
	protected boolean isEligibleForOverriding(String className) {
		if (this.excludedClasses.contains(className)) {
			return false;
		}
		for (Iterator<String> it = this.excludedPackages.iterator(); it.hasNext();) {
			String packageName = it.next();
			if (className.startsWith(packageName)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Transformation hook to be implemented by subclasses.
	 * @param name FQN of class being transformed
	 * @param bytes class bytes
	 * @return transformed bytes. Return same as input bytes if
	 * the transformation produced no changes.
	 */
	protected abstract byte[] transformIfNecessary(String name, byte[] bytes);

}
