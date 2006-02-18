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

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to cache PropertyDescriptor information for a Java class.
 * Package-visible; not for use by application code.
 *
 * <p>Necessary as <code>Introspector.getBeanInfo()</code> in JDK 1.3 will return a
 * new deep copy of the BeanInfo every time we ask for it. We take the opportunity
 * to cache property descriptors by method name for fast lookup. Furthermore,
 * we do our own caching of descriptors here, rather than rely on the JDK's
 * system-wide BeanInfo cache (to avoid leaks on class loader shutdown).
 *
 * <p>Information is cached statically, so we don't need to create new
 * objects of this class for every JavaBean we manipulate. Hence, this class
 * implements the factory design pattern, using a private constructor
 * and a static <code>forClass</code> method to obtain instances.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 05 May 2001
 * @see #forClass(Class)
 */
final class CachedIntrospectionResults {

	//---------------------------------------------------------------------
	// Static section
	//---------------------------------------------------------------------

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/**
	 * Map keyed by class containing CachedIntrospectionResults.
	 * Needs to be a WeakHashMap with WeakReferences as values to allow
	 * for proper garbage collection in case of multiple class loaders.
	 */
	private static final Map classCache = Collections.synchronizedMap(new WeakHashMap());


	/**
	 * Create CachedIntrospectionResults for the given bean class.
	 * <P>We don't want to use synchronization here. Object references are atomic,
	 * so we can live with doing the occasional unnecessary lookup at startup only.
	 * @param beanClass the bean class to analyze
	 */
	public static CachedIntrospectionResults forClass(Class beanClass) throws BeansException {
		CachedIntrospectionResults results = null;
		Object value = classCache.get(beanClass);
		if (value instanceof Reference) {
			Reference ref = (Reference) value;
			results = (CachedIntrospectionResults) ref.get();
		}
		else {
			results = (CachedIntrospectionResults) value;
		}
		if (results == null) {
			// can throw BeansException
			results = new CachedIntrospectionResults(beanClass);
			boolean cacheSafe = isCacheSafe(beanClass);
			if (logger.isDebugEnabled()) {
				logger.debug("Class [" + beanClass.getName() + "] is " + (!cacheSafe ? "not " : "") + "cache-safe");
			}
			if (cacheSafe) {
				classCache.put(beanClass, results);
			}
			else {
				classCache.put(beanClass, new WeakReference(results));
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using cached introspection results for class [" + beanClass.getName() + "]");
			}
		}
		return results;
	}

	/**
	 * Check whether the given class is cache-safe,
	 * i.e. whether it is loaded by the same class loader as the
	 * CachedIntrospectionResults class or a parent of it.
	 * <p>Many thanks to Guillaume Poirier for pointing out the
	 * garbage collection issues and for suggesting this solution.
	 * @param clazz the class to analyze
	 * @return whether the given class is thread-safe
	 */
	private static boolean isCacheSafe(Class clazz) {
		ClassLoader cur = CachedIntrospectionResults.class.getClassLoader();
		ClassLoader target = clazz.getClassLoader();
		if (target == null || cur == target) {
			return true;
		}
		while (cur != null) {
			cur = cur.getParent();
			if (cur == target) {
				return true;
			}
		}
		return false;
	}


	//---------------------------------------------------------------------
	// Instance section
	//---------------------------------------------------------------------

	private final BeanInfo beanInfo;

	/** Property descriptors keyed by property name */
	private final Map propertyDescriptorCache;


	/**
	 * Create a new CachedIntrospectionResults instance for the given class.
	 */
	private CachedIntrospectionResults(Class clazz) throws BeansException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Getting BeanInfo for class [" + clazz.getName() + "]");
			}
			this.beanInfo = Introspector.getBeanInfo(clazz);

			// Immediately remove class from Introspector cache, to allow for proper
			// garbage collection on class loader shutdown - we cache it here anyway,
			// in a GC-friendly manner. In contrast to CachedIntrospectionResults,
			// Introspector does not use WeakReferences as values of its WeakHashMap!
			Class classToFlush = clazz;
			do {
				Introspector.flushFromCaches(classToFlush);
				classToFlush = classToFlush.getSuperclass();
			}
			while (classToFlush != null);

			if (logger.isDebugEnabled()) {
				logger.debug("Caching PropertyDescriptors for class [" + clazz.getName() + "]");
			}
			this.propertyDescriptorCache = new HashMap();

			// This call is slow so we do it once.
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found property '" + pds[i].getName() + "'" +
							(pds[i].getPropertyType() != null ?
							" of type [" + pds[i].getPropertyType().getName() + "]" : "") +
							(pds[i].getPropertyEditorClass() != null ?
							"; editor [" + pds[i].getPropertyEditorClass().getName() + "]" : ""));
				}

				this.propertyDescriptorCache.put(pds[i].getName(), pds[i]);
			}
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Cannot get BeanInfo for object of class [" + clazz.getName() + "]", ex);
		}
	}

	public BeanInfo getBeanInfo() {
		return this.beanInfo;
	}

	public Class getBeanClass() {
		return this.beanInfo.getBeanDescriptor().getBeanClass();
	}

	public PropertyDescriptor getPropertyDescriptor(String propertyName) {
		return (PropertyDescriptor) this.propertyDescriptorCache.get(propertyName);
	}

}
