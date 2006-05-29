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
import java.lang.reflect.Method;

import org.springframework.instrument.classloading.ClassLoaderWeaver;
import org.springframework.util.ReflectionUtils;

/**
 * ClassLoaderWeaver that uses reflection to delegate methods to an internal
 * class loader. Useful when the delegating class loader is loaded in a
 * different class loader (such as the container server based class loader
 * that is not visible to the web application).
 *
 * @author Costin Leau
 * @since 2.0
 */
public class ReflectiveClassLoaderWeaver implements ClassLoaderWeaver {
	
	/** Keep a loose reference to avoid ClassCastExceptions */
	private ClassLoader classLoader;


	public ReflectiveClassLoaderWeaver(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	public void addClassFileTransformer(ClassFileTransformer cft) {
		invokeMethod("addClassFileTransformer", new Object[] { cft }, ClassFileTransformer.class);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.classLoader;
	}

	public Object invokeMethod(String methodName, Object[] args, Class... argumentTypes) {
		try {
			Method method = this.classLoader.getClass().getDeclaredMethod(methodName, argumentTypes);
			method.setAccessible(true);
			return ReflectionUtils.invokeMethod(method, this.classLoader, args);
		}
		catch (SecurityException ex) {
			ReflectionUtils.handleReflectionException(ex);
		}
		catch (NoSuchMethodException ex) {
			ReflectionUtils.handleReflectionException(ex);
		}
		throw new IllegalStateException("Invalid state reached during reflection handling");
	}

}
