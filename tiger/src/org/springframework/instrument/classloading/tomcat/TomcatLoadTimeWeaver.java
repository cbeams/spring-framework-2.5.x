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

package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;

import org.springframework.instrument.classloading.AbstractLoadTimeWeaver;
import org.springframework.instrument.classloading.DelegatedInstrumentedClassLoader;
import org.springframework.instrument.classloading.support.ReflectionClassLoaderHandler;

/**
 * Tomcat specific LoadTimeWeaver. It uses indirection mechanism and interfaces to work with the inner classloader to avoid
 * class loading problems as the container classes are not accessible inside a web application.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class TomcatLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private DelegatedInstrumentedClassLoader classLoader;

	public ClassLoader getInstrumentableClassLoader() {
		return classLoader.getDelegatedClassLoader();
	}

	

	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		classLoader.addTransformer(classFileTransformer);
	}

	public TomcatLoadTimeWeaver() {
		setInstrumentedClassLoader(getContextClassLoader());
	}
	
	public TomcatLoadTimeWeaver(ClassLoader classLoader) {
		setInstrumentedClassLoader(classLoader);
	}

	/**
	 * @param classLoader
	 *            The classLoader to set.
	 */
	public void setInstrumentedClassLoader(ClassLoader classLoader) {
		this.classLoader = new ReflectionClassLoaderHandler(classLoader);		
	}

}
