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
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0
 */
public class ShadowingClassLoader extends ClassLoader {

	private final ClassLoader enclosingClassLoader;

	private final List<ClassFileTransformer> classFileTransformers = new ArrayList<ClassFileTransformer>();

	private final Map<String, Class> classCache = new HashMap<String, Class>();


	public ShadowingClassLoader(ClassLoader enclosingClassLoader) {
		Assert.notNull(enclosingClassLoader, "Enclosing ClassLoader must not be null");
		this.enclosingClassLoader = enclosingClassLoader;
	}


	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (shouldShadow(name)) {
			Class cls = this.classCache.get(name);
			if (cls != null) {
				return cls;
			}
			return doLoadClass(name);
		}
		else {
			return this.enclosingClassLoader.loadClass(name);
		}
	}

	private boolean shouldShadow(String name) {
		return !isExcluded(name);
	}
	
	private boolean isExcluded(String name) {
		return name.equals(getClass().getName()) || 
						name.endsWith("ShadowingClassLoader") ||
						name.startsWith("org.dom4j") ||
			 			name.startsWith("org.aspectj") ||
			 			name.startsWith("org.apache.xerces") ||
						name.startsWith("java.") ||
						name.startsWith("javax.") ||
						name.startsWith("org.apache.commons.logging") ||
						name.startsWith("org.xml.sax") ||
						name.startsWith("org.w3c") ||
						name.startsWith("sun") ||
						isClassNameExcludedFromShadowing(name);
	}
	
	/**
	 * Subclasses can override this method to specify whether or not
	 * particular classes are excluded from shadowing
	 * @param className class name to test
	 */
	protected boolean isClassNameExcludedFromShadowing(String className) {
		return false;
	}

	private Class doLoadClass(String name) throws ClassNotFoundException {
		String internalName = StringUtils.replace(name, ".", "/") + ".class";
		InputStream is = getParent().getResourceAsStream(internalName);
		if (is == null) {
			throw new ClassNotFoundException(name);
		}
		try {
			byte[] bytes = FileCopyUtils.copyToByteArray(is);
			bytes = applyTransformers(name, bytes);
			Class cls = defineClass(name, bytes, 0, bytes.length);
			this.classCache.put(name, cls);
			return cls;
		}
		catch (IOException ex) {
			throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
		}
	}
	
	public void addTransformers(ShadowingClassLoader other) {
		classFileTransformers.addAll(other.classFileTransformers);
	}

	private byte[] applyTransformers(String name, byte[] bytes) {
		String internalName = StringUtils.replace(name, ".", "/");
		try {
			for (ClassFileTransformer transformer : this.classFileTransformers) {
				byte[] transformed = transformer.transform(this, internalName, null, null, bytes);
				bytes = (transformed != null ? transformed : bytes);
			}
			return bytes;
		}
		catch (IllegalClassFormatException ex) {
			throw new IllegalStateException(ex);
		}
	}


	public void addTransformer(ClassFileTransformer transformer) {
		this.classFileTransformers.add(transformer);
	}

}
