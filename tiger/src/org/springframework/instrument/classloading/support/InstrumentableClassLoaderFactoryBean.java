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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Retrieves a class loader from the context class loader using the className property.
 * It will traverse the classloaders hierarchy and analyze the classloader class,
 * interfaces and superclasses.
 *
 * @author Costin Leau
 * @since 2.0
 */
public class InstrumentableClassLoaderFactoryBean implements FactoryBean, InitializingBean {

	private final Log logger = LogFactory.getLog(getClass());

	private String className;

	/** Loose reference to avoid class loading problems */
	private ClassLoader classLoader;


	public void setClassName(String className) {
		this.className = className;
	}


	public void afterPropertiesSet() {
		if (!StringUtils.hasText(this.className)) {
			throw new IllegalArgumentException("className is required");
		}

		ClassLoader loader = ClassUtils.getDefaultClassLoader();

		// parse class loading hierarchy
		for (Class clazz = loader.getClass(); loader != null && this.classLoader == null; loader = loader.getParent()) {
			// check class itself
			if (analyzeClasses(loader, clazz)) {
				return;
			}
			// check interfaces
			if (analyzeClasses(loader, clazz.getInterfaces())) {
				return;
			}
			// check superclasses
			for (Class superClass = clazz.getSuperclass(); superClass != Object.class && superClass != null; superClass = clazz.getSuperclass())
				if (analyzeClasses(loader, superClass)) {
					return;
				}
		}

		throw new IllegalArgumentException(className + " was not found in the current classloader hierarchy - "
				+ "see the JPA docs on how to use instrumented classloaders inside various containers");
	}

	protected boolean analyzeClasses(ClassLoader loader, Class... classes) {
		boolean debug = logger.isDebugEnabled();
		for (Class clazz : classes) {
			if (debug) {
				logger.debug("Analyzing class: " + clazz.getName());
			}
			if (this.className.equals(clazz.getName())) {
				if (debug) {
					logger.debug(this.className + " found! Using class loader: " + loader);
				}
				this.classLoader = loader;
				return true;
			}
		}
		return false;
	}


	public Object getObject() {
		return this.classLoader;
	}

	public Class getObjectType() {
		return ClassLoader.class;
	}

	public boolean isSingleton() {
		return false;
	}

}
