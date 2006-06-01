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
package org.springframework.instrument.classloading.resin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.instrument.classloading.support.AbstractLoadTimeWeaver;
import org.springframework.util.ReflectionUtils;

import com.caucho.loader.DynamicClassLoader;
import com.caucho.loader.EnvironmentClassLoader;

/**
 * Resin specific LoadTimeWeaver.
 * 
 * @author Costin Leau
 * 
 */
public class ResinLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private static final Log log = LogFactory.getLog(ResinLoadTimeWeaver.class);

	private static boolean debug = log.isDebugEnabled();

	private EnvironmentClassLoader classLoader;

	public ResinLoadTimeWeaver() {
		this.classLoader = fetchResinClassLoader(getContextClassLoader());
	}

	public ResinLoadTimeWeaver(ClassLoader loader) {
		this.classLoader = fetchResinClassLoader(loader);
	}

	protected EnvironmentClassLoader fetchResinClassLoader(ClassLoader cl) {
		ClassLoader loader = getContextClassLoader();
		if (loader instanceof EnvironmentClassLoader)
			return (EnvironmentClassLoader) loader;

		throw new IllegalArgumentException("the contextClassLoader is not instance of "
				+ EnvironmentClassLoader.class.getName() + "; maybe a different LoadTimeWeaver should be used?");
	}

	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		ResinByteCodeEnhancerAdapter enhancer = new ResinByteCodeEnhancerAdapter(classFileTransformer);

		// TODO: add (not remove) previous enhancer if possible
		classLoader.setByteCodeEnhancer(enhancer);
		System.out.println("adding classFileTransformer " + enhancer);
		if (debug)
			log.debug("adding classFileTransformer " + enhancer);

	}

	public ClassLoader getInstrumentableClassLoader() {
		return classLoader;
	}

	@Override
	public ClassLoader getThrowawayClassLoader() {
		EnvironmentClassLoader temp = new EnvironmentClassLoader();
		invokeMethod(temp, DynamicClassLoader.class, "replace", new Object[] { classLoader },
				new Class[] { DynamicClassLoader.class });
		//return temp;
		return classLoader;
	}

	protected Object invokeMethod(Object target, Class methodClass, String methodName, Object[] args,
			Class... argumentTypes) {
		try {
			Method method = methodClass.getDeclaredMethod(methodName, argumentTypes);
			method.setAccessible(true);
			return ReflectionUtils.invokeMethod(method, target, args);
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

}
