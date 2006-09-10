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
import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * LoadTimeWeaver that uses reflection to delegate to an underlying ClassLoader,
 * which needs to support the following weaving methods (as defined in the
 * LoadTimeWeaver interface):
 * <ul>
 * <li><code>addTransformer(java.lang.instrument.ClassFileTransformer)</code>:
 * to register the given ClassFileTransformer for this ClassLoader
 * <li><code>ClassLoader getThrowawayClassLoader()</code>:
 * to obtain a throwaway class loader for this ClassLoader
 * (optional; ReflectiveLoadTimeWeaver will fall back to a
 * SimpleThrowawayClassLoader if that method isn't available)
 * </ul>
 *
 * <p>Useful when the underlying class loader implementation is loaded in a
 * different class loader (such as the application server's class loader
 * which is not visible to the web application).
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 * @see #addTransformer(java.lang.instrument.ClassFileTransformer)
 * @see #getThrowawayClassLoader()
 * @see SimpleThrowawayClassLoader
 */
public class ReflectiveLoadTimeWeaver implements LoadTimeWeaver {

	private static final String ADD_TRANSFORMER_METHOD_NAME = "addTransformer";

	private static final String GET_THROWAWAY_CLASS_LOADER_METHOD_NAME = "getThrowawayClassLoader";


	/** Keep a loose reference - we cannot rely on seeing the implementation class */
	private final ClassLoader classLoader;

	private final Method addTransformerMethod;

	private final Method getThrowawayClassLoaderMethod;


	/**
	 * Create a new ReflectiveLoadTimeWeaver for the current context class loader,
	 * which needs to support the required weaving methods.
	 */
	public ReflectiveLoadTimeWeaver() {
		this(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Create a new ReflectiveLoadTimeWeaver for the given class loader.
	 * @param classLoader the ClassLoader to delegate to for weaving
	 * (needs to support the required weaving methods)
	 */
	public ReflectiveLoadTimeWeaver(ClassLoader classLoader) {
		Assert.notNull(classLoader, "ClassLoader must not be null");
		this.classLoader = classLoader;
		this.addTransformerMethod = ClassUtils.getMethodIfAvailable(
				this.classLoader.getClass(), ADD_TRANSFORMER_METHOD_NAME,
				new Class [] {ClassFileTransformer.class});
		if (this.addTransformerMethod == null) {
			throw new IllegalStateException(
					"ClassLoader [" + classLoader + "|" + classLoader.getClass() + "] does not have an addTransformer(ClassFileTransformer) method");
		}
		this.getThrowawayClassLoaderMethod = ClassUtils.getMethodIfAvailable(
				this.classLoader.getClass(), GET_THROWAWAY_CLASS_LOADER_METHOD_NAME,
				new Class[0]);
		// getThrowawayClassLoader method is optional
	}


	public void addTransformer(ClassFileTransformer transformer) {
		Assert.notNull(transformer, "Transformer must not be null");
		ReflectionUtils.invokeMethod(this.addTransformerMethod, this.classLoader, new Object[] {transformer});
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.classLoader;
	}

	public ClassLoader getThrowawayClassLoader() {
		if (this.getThrowawayClassLoaderMethod != null) {
			return (ClassLoader) ReflectionUtils.invokeMethod(
					this.getThrowawayClassLoaderMethod, this.classLoader, new Object[0]);
		}
		else {
			return new SimpleThrowawayClassLoader(this.classLoader);
		}
	}

}
