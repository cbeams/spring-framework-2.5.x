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
package org.springframework.instrument.classloading.weblogic;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Reflective wrapper around a WebLogic classloader. Used to encapsulate the
 * classloader specific methods (discovered and called through reflection) from
 * the loadtime weaver.
 * 
 * @author Costin Leau
 * 
 */
class WebLogicClassLoader {

	private static final String WL_CLASSLOADER_CLASSNAME = "weblogic.utils.classloaders.GenericClassLoader";

	private static final String PREPROCESSOR_CLASSNAME = "weblogic.utils.classloaders.ClassPreProcessor";

	private static final String GET_FINDER_METHOD = "getClassFinder";

	private static final String GET_PARENT_METHOD = "getParent";

	private static final String ADD_PREPROCESSOR_METHOD = "addInstanceClassPreProcessor";

	private static final String EXCEPTION_MSG = "cannot wrap WebLogic 10 classloader ";

	private final ClassLoader loader;

	private final Class<?> wlGenericClassLoaderClass;

	public WebLogicClassLoader(ClassLoader classLoader) {
		Assert.notNull(classLoader);

		try {
			wlGenericClassLoaderClass = classLoader.loadClass(WL_CLASSLOADER_CLASSNAME);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(EXCEPTION_MSG + classLoader, ex);
		}

		Assert.isInstanceOf(wlGenericClassLoaderClass, classLoader, "ClassLoader must be instanceof "
				+ wlGenericClassLoaderClass.getName());

		this.loader = classLoader;
	}

	public ClassLoader getThrowawayClassLoader() {
		Method getClassFinderMethod = ReflectionUtils.findMethod(loader.getClass(), GET_FINDER_METHOD);
		Object classFinder = ReflectionUtils.invokeMethod(getClassFinderMethod, loader);

		Method getParentMethod = ReflectionUtils.findMethod(loader.getClass(), GET_PARENT_METHOD);
		Object parent = ReflectionUtils.invokeMethod(getParentMethod, loader);

		try {
			// arguments for 'clone'-like method
			Class[] constructorArgs = new Class[] { getClassFinderMethod.getReturnType(), ClassLoader.class };
			Constructor<?> constructor = wlGenericClassLoaderClass.getConstructor(constructorArgs);

			return (ClassLoader) constructor.newInstance(new Object[] { classFinder, parent });
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException(EXCEPTION_MSG + loader, ex);
		}

		catch (IllegalAccessException ex) {
			throw new IllegalArgumentException(EXCEPTION_MSG + loader, ex);
		}

		catch (InstantiationException ex) {
			throw new IllegalArgumentException(EXCEPTION_MSG + loader, ex);
		}

		catch (InvocationTargetException ex) {
			throw new IllegalArgumentException(EXCEPTION_MSG + loader, ex.getCause());
		}

	}

	public void addTransformer(ClassFileTransformer transformer) {
		Class wlPreProcessorClass = null;
		try {
			// create new ClassPreProcessor wrapper
			wlPreProcessorClass = loader.loadClass(PREPROCESSOR_CLASSNAME);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(EXCEPTION_MSG + loader, ex);
		}

		InvocationHandler adapter = new WebLogicClassPreProcessorAdapter(transformer, loader);
		Object adapterInstance = Proxy.newProxyInstance(wlPreProcessorClass.getClassLoader(),
			new Class[] { wlPreProcessorClass }, adapter);

		Method addPreProcessor = ReflectionUtils.findMethod(loader.getClass(), ADD_PREPROCESSOR_METHOD,
			new Class[] { wlPreProcessorClass });
		
		ReflectionUtils.invokeMethod(addPreProcessor, loader, new Object[] { adapterInstance });
	}

	public ClassLoader getInternalClassLoader() {
		return loader;
	}
}
