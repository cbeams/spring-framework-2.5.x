/*
 * Copyright 2002-2004 the original author or authors.
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
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to cache PropertyDescriptor information for a Java class.
 * Package-visible; not for use by application code.
 *
 * <p>Necessary as Introspector.getBeanInfo() in JDK 1.3 will return a new
 * deep copy of the BeanInfo every time we ask for it. We take the opportunity
 * to hash property descriptors by method name for fast lookup.
 *
 * <p>Information is cached statically, so we don't need to create new
 * objects of this class for every JavaBean we manipulate. Thus this class
 * implements the factory design pattern, using a private constructor
 * and a static forClass method to obtain instances.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 05 May 2001
*  @version $Id: CachedIntrospectionResults.java,v 1.10 2004-05-24 09:52:22 jhoeller Exp $
 */
final class CachedIntrospectionResults {

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/**
	 * Map keyed by class containing CachedIntrospectionResults.
	 * Needs to be a WeakHashMap with WeakReferences as values
	 * to allow for proper garbage collection on shutdown!
	 */
	private static final Map classCache = new WeakHashMap();

	/**
	 * We might use this from the EJB tier, so we don't want to use synchronization.
	 * Object references are atomic, so we can live with doing the occasional
	 * unnecessary lookup at startup only.
	 */
	protected static synchronized CachedIntrospectionResults forClass(Class clazz) throws BeansException {
		WeakReference weakRef = (WeakReference) classCache.get(clazz);
		CachedIntrospectionResults results = (weakRef != null) ? (CachedIntrospectionResults) weakRef.get() : null;
		if (results == null) {
			// can throw BeansException
			results = new CachedIntrospectionResults(clazz);
			classCache.put(clazz, new WeakReference(results));
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using cached introspection results for class [" + clazz.getName() + "]");
			}
		}
		return results;
	}


	private final BeanInfo beanInfo;

	/** Property descriptors keyed by property name */
	private final Map propertyDescriptorCache;

	/**
	 * Create new CachedIntrospectionResults instance fot the given class.
	 */
	private CachedIntrospectionResults(Class clazz) throws BeansException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Getting BeanInfo for class [" + clazz.getName() + "]");
			}
			this.beanInfo = Introspector.getBeanInfo(clazz);

			if (logger.isDebugEnabled()) {
				logger.debug("Caching PropertyDescriptors for class [" + clazz.getName() + "]");
			}
			this.propertyDescriptorCache = new HashMap();

			// This call is slow so we do it once.
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found property '" + pds[i].getName() + "' of type [" + pds[i].getPropertyType() +
											 "]; editor=[" + pds[i].getPropertyEditorClass() + "]");
				}
				this.propertyDescriptorCache.put(pds[i].getName(), pds[i]);
			}
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Cannot get BeanInfo for object of class [" + clazz.getName() + "]", ex);
		}
	}

	protected BeanInfo getBeanInfo() {
		return this.beanInfo;
	}

	protected Class getBeanClass() {
		return this.beanInfo.getBeanDescriptor().getBeanClass();
	}

	protected PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		PropertyDescriptor pd = (PropertyDescriptor) this.propertyDescriptorCache.get(propertyName);
		if (pd == null) {
			throw new FatalBeanException("No property '" + propertyName + "' in class [" + getBeanClass().getName() + "]");
		}
		return pd;
	}

}
