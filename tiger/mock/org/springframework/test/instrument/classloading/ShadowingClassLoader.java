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

package org.springframework.test.instrument.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * @author Rob Harrop
 */
public class ShadowingClassLoader extends ClassLoader {

	private final ClassLoader enclosingClassLoader;

	private final List<ClassFileTransformer> classFilterTransformers = new ArrayList<ClassFileTransformer>();

	private final Map<String, Class> classCache = new HashMap<String, Class>();

	public ShadowingClassLoader(ClassLoader enclosingClassLoader) {
		Assert.notNull(enclosingClassLoader, "'enclosingClassLoader' cannot be null.");
		this.enclosingClassLoader = enclosingClassLoader;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (shouldShadow(name)) {
			Class cls = classCache.get(name);
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
		if (isExcluded(name)) {
			return false;
		}
		else {
			return true;
		}
	}
	

	private boolean isExcluded(String name) {
		return name.equals(getClass().getName()) || name.startsWith("java.") ||
						name.startsWith("javax.") ||
						name.startsWith("org.apache.commons.logging") ||
						name.startsWith("org.xml.sax") ||
						name.startsWith("org.w3c") ||
						name.startsWith("sun");
	}

	private Class doLoadClass(String name) throws ClassNotFoundException {
		String path = name.replaceAll("\\.", "/") + ".class";
		ClassPathResource cpr = new ClassPathResource(path);
		InputStream inputStream = null;
		try {
			inputStream = cpr.getInputStream();
			byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
			bytes = applyTransformers(name, bytes);
			Class cls = defineClass(name, bytes, 0, bytes.length);
			classCache.put(name, cls);
			return cls;
		}
		catch (IOException e) {
			throw new ClassNotFoundException("Class '" + name + "' cannot be found.");
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private byte[] applyTransformers(String name, byte[] bytes) {
		String internalName = name.replaceAll("\\.", "/");
		try {
			for (ClassFileTransformer transformer : this.classFilterTransformers) {
				byte[] transformed = transformer.transform(this, internalName, null, null, bytes);
				bytes = (transformed != null ? transformed : bytes);
			}
			return bytes;
		}
		catch (IllegalClassFormatException e) {
			throw new RuntimeException(e);
		}
	}

	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		this.classFilterTransformers.add(classFileTransformer);
	}
}
