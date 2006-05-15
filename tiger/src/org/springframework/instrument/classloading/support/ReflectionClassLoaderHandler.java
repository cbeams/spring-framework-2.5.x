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

import org.springframework.instrument.classloading.DelegatedInstrumentedClassLoader;
import org.springframework.util.ReflectionUtils;

/**
 * InstrumentedClassLoader that uses reflection to delegate methods to an
 * internal classloader. Useful when the delegating classloader is loaded in a
 * different classloader (such as the container server based classloader that is
 * not visible to the web application).
 * 
 * @author Costin Leau
 * 
 */
public class ReflectionClassLoaderHandler implements DelegatedInstrumentedClassLoader {
	
	// keep a common reference to avoid ClassCastExceptions
	private ClassLoader loader;

	public ReflectionClassLoaderHandler(ClassLoader loader) {
		this.loader = loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentedClassLoader#addClassNameToExcludeFromUndelegation(java.lang.String)
	 */
	public void addClassNameToExcludeFromUndelegation(String className) {
		invokeMethod("addClassNameToExcludeFromUndelegation", new Object[] { className }, String.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentedClassLoader#addTransformer(java.lang.instrument.ClassFileTransformer)
	 */
	public void addTransformer(ClassFileTransformer cft) {
		invokeMethod("addTransformer", new Object[] { cft }, ClassFileTransformer.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentedClassLoader#isAspectJWeavingEnabled()
	 */
	public boolean isAspectJWeavingEnabled() {
		return ((Boolean) invokeMethod("isAspectJWeavingEnabled", null, (Class[]) null)).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentedClassLoader#setAspectJWeavingEnabled(boolean)
	 */
	public void setAspectJWeavingEnabled(boolean flag) {
		invokeMethod("setAspectJWeavingEnabled", new Object[] { Boolean.valueOf(flag) }, boolean.class);
	}

	protected Object invokeMethod(String methodName, Object[] args, Class... argumentTypes) {
		try {
			Method method = loader.getClass().getDeclaredMethod(methodName, argumentTypes);
			return ReflectionUtils.invokeMethod(method, loader, args);
		}
		catch (SecurityException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		catch (NoSuchMethodException e) {
			ReflectionUtils.handleReflectionException(e);
		}

		// should not reach this line
		throw new UnsupportedOperationException("this line should not be reached");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.support.DelegatedInstrumentedClassLoader#getDelegatedClassLoader()
	 */
	public ClassLoader getDelegatedClassLoader() {
		return loader;
	}
}
