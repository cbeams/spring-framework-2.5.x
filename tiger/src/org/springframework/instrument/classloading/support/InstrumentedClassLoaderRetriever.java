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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Retrieves a classloader from the context classloader using the className property.
 * It will traverse the classloaders hierarchy and analyze the classloader class, interfaces and superclasses.
 * 
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class InstrumentedClassLoaderRetriever implements InitializingBean, DisposableBean, FactoryBean {
	
	private static final Log log = LogFactory.getLog(InstrumentedClassLoaderRetriever.class);

	private String className;

	// loose reference to avoid class loading problems
	private ClassLoader loader;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() {
		return loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return ClassLoader.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return false;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		loader = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		if (!StringUtils.hasText(className))
			throw new IllegalArgumentException("className is required");

		ClassLoader loader = ClassUtils.getDefaultClassLoader();

		// parse class loading hierarchy
		for (Class clazz = loader.getClass(); loader != null && this.loader == null; loader = loader.getParent()) {
			// check class itself
			if (analyzeClasses(loader, clazz))
				return;
			// check interfaces
			if (analyzeClasses(loader, clazz.getInterfaces()))
				return;
			// check superclasses
			for (Class superClass = clazz.getSuperclass(); superClass != Object.class && superClass != null; superClass = clazz.getSuperclass())
				if (analyzeClasses(loader, superClass))
					return;
		}

		throw new IllegalArgumentException(className + " was not found in the current classloader hierarchy - "
				+ "see the JPA docs on how to use instrumented classloaders inside various containers");
	}

	protected boolean analyzeClasses(ClassLoader loader, Class... classes) {
		boolean debug = log.isDebugEnabled();
		for (Class clazz : classes) {
			if (debug)
				log.debug("analyzing class " + clazz.getName());
			if (className.equals(clazz.getName())) {
				if (debug)
					log.debug(className + " found! Using classloader " + loader);
				this.loader = loader;
				return true;
			}
		}
		return false;
	}

}
