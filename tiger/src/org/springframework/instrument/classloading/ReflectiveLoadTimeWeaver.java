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

import org.springframework.util.ReflectionUtils;

/**
 * ClassLoaderWeaver that uses reflection to delegate methods to an internal
 * class loader. Useful when the delegating class loader is loaded in a
 * different class loader (such as the container server based class loader that
 * is not visible to the web application).
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class ReflectiveLoadTimeWeaver extends AbstractLoadTimeWeaver {

	protected String METHOD_NAME_ADD_TRANSFORMERS = "addTransformer";

	protected String METHOD_NAME_THROWAWAY_CLASSLOADER = "getThrowawayClassLoader";


	/** Keep a loose reference to avoid ClassCastExceptions */
	private final ClassLoader classLoader;


	public ReflectiveLoadTimeWeaver() {
		this.classLoader = getContextClassLoader();
	}

	public ReflectiveLoadTimeWeaver(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	public void addTransformer(ClassFileTransformer transformer) {
		invokeMethod(METHOD_NAME_ADD_TRANSFORMERS, new Object[] { transformer }, ClassFileTransformer.class);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.classLoader;
	}

	public ClassLoader getThrowawayClassLoader() {
		return (ClassLoader) invokeMethod(METHOD_NAME_THROWAWAY_CLASSLOADER, null, (Class[]) null);
	}

	private Object invokeMethod(String methodName, Object[] args, Class... argumentTypes) {
		return ReflectionUtils.invokeMethod(methodName, this.classLoader.getClass(), this.classLoader, args, argumentTypes);
	}

}
