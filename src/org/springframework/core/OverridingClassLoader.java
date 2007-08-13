/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * <code>ClassLoader</code> that does <i>not</i> always delegate to the
 * parent loader, as normal class loaders do. This enables, for example,
 * instrumentation to be forced in the overriding ClassLoader, or a
 * "throwaway" class loading behavior, where selected classes are
 * temporarily loaded in the overriding ClassLoader, in order to load
 * an instrumented version of the class in the parent ClassLoader later on.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0.1
 */
public class OverridingClassLoader extends ClassLoader {

	/** Packages that are excluded by default */
	public static final String[] DEFAULT_EXCLUDED_PACKAGES = new String[] {"java.", "javax.", "sun."};


	private static final String CLASS_FILE_SUFFIX = ".class";


	private final Set excludedPackages = Collections.synchronizedSet(new HashSet());

	private final Set excludedClasses = Collections.synchronizedSet(new HashSet());


	/**
	 * Create a new OverridingClassLoader for the given class loader.
	 * @param parent the ClassLoader to build an overriding ClassLoader for
	 */
	public OverridingClassLoader(ClassLoader parent) {
		super(parent);
		for (int i = 0; i < DEFAULT_EXCLUDED_PACKAGES.length; i++) {
			this.excludedPackages.add(DEFAULT_EXCLUDED_PACKAGES[i]);
		}
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


	protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class result = null;

		if (isEligibleForOverriding(name)) {
			result = findLoadedClass(name);
			if (result == null) {
				InputStream is = openStreamForClass(name);
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
	 * Determine whether the specified class is eligible for overriding
	 * by this class loader.
	 * <p>The default implementation checks against excluded packages and classes.
	 * @param className the class name to check
	 * @return whether the specified class is eligible
	 * @see #excludePackage
	 * @see #excludeClass
	 */
	protected boolean isEligibleForOverriding(String className) {
		if (this.excludedClasses.contains(className)) {
			return false;
		}
		for (Iterator it = this.excludedPackages.iterator(); it.hasNext();) {
			String packageName = (String) it.next();
			if (className.startsWith(packageName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Open an InputStream for the specified class.
	 * <p>The default implementation loads a standard class file through
	 * the parent ClassLoader's <code>getResourceAsStream</code> method.
	 * @param name the name of the class
	 * @return the InputStream containing the byte code for the specified class
	 */
	protected InputStream openStreamForClass(String name) {
		String internalName = name.replace('.', '/') + CLASS_FILE_SUFFIX;
		return getParent().getResourceAsStream(internalName);
	}


	/**
	 * Transformation hook to be implemented by subclasses.
	 * <p>The default implementation simply returns the given bytes as-is.
	 * @param name the fully-qualified name of the class being transformed
	 * @param bytes the raw bytes of the class
	 * @return the transformed bytes (never <code>null</code>;
	 * same as the input bytes if the transformation produced no changes)
	 */
	protected byte[] transformIfNecessary(String name, byte[] bytes) {
		return bytes;
	}

}
