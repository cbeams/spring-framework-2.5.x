/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.util.HashMap;

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
 * and a public static forClass() method to obtain instances.
 *
 * @author Rod Johnson
 * @since 05 May 2001
*  @version $Id: CachedIntrospectionResults.java,v 1.6 2004-02-04 17:39:58 jhoeller Exp $
 */
final class CachedIntrospectionResults {

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/** Map keyed by class containing CachedIntrospectionResults */
	private static HashMap classCache = new HashMap();

	/**
	 * We might use this from the EJB tier, so we don't want to use synchronization.
	 * Object references are atomic, so we can live with doing the occasional
	 * unnecessary lookup at startup only.
	 */
	protected static CachedIntrospectionResults forClass(Class clazz) throws BeansException {
		Object results = classCache.get(clazz);
		if (results == null) {
			// can throw BeansException
			results = new CachedIntrospectionResults(clazz);
			classCache.put(clazz, results);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using cached introspection results for class " + clazz.getName());
			}
		}
		return (CachedIntrospectionResults) results;
	}


	private BeanInfo beanInfo;

	/** Property descriptors keyed by property name */
	private HashMap propertyDescriptorMap;

	/** Property descriptors keyed by property name */
	private HashMap methodDescriptorMap;

	/**
	 * Create new CachedIntrospectionResults instance fot the given class.
	 */
	private CachedIntrospectionResults(Class clazz) throws BeansException {
		try {
			logger.debug("Getting BeanInfo for class [" + clazz.getName() + "]");
			this.beanInfo = Introspector.getBeanInfo(clazz);

			logger.debug("Caching PropertyDescriptors for class [" + clazz.getName() + "]");
			this.propertyDescriptorMap = new HashMap();
			// This call is slow so we do it once
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				logger.debug("Found property '" + pds[i].getName() + "' of type [" + pds[i].getPropertyType() +
										 "]; editor=[" + pds[i].getPropertyEditorClass() + "]");
				this.propertyDescriptorMap.put(pds[i].getName(), pds[i]);
			}

			logger.debug("Caching MethodDescriptors for class [" + clazz.getName() + "]");
			this.methodDescriptorMap = new HashMap();
			// This call is slow so we do it once
			MethodDescriptor[] mds = this.beanInfo.getMethodDescriptors();
			for (int i = 0; i < mds.length; i++) {
				logger.debug("Found method '" + mds[i].getName() + "' of type [" + mds[i].getMethod().getReturnType() + "]");
				this.methodDescriptorMap.put(mds[i].getName(), mds[i]);
			}
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Cannot get BeanInfo for object of class [" + clazz.getName() + "]", ex);
		}
	}

	protected BeanInfo getBeanInfo() {
		return beanInfo;
	}

	protected Class getBeanClass() {
		return beanInfo.getBeanDescriptor().getBeanClass();
	}

	protected PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		PropertyDescriptor pd = (PropertyDescriptor) this.propertyDescriptorMap.get(propertyName);
		if (pd == null) {
			throw new FatalBeanException("No property '" + propertyName + "' in class [" + getBeanClass().getName() + "]", null);
		}
		return pd;
	}

	protected MethodDescriptor getMethodDescriptor(String methodName) throws BeansException {
		MethodDescriptor md = (MethodDescriptor) this.methodDescriptorMap.get(methodName);
		if (md == null) {
			throw new FatalBeanException("No method '" + methodName + "' in class [" + getBeanClass().getName() + "]", null);
		}
		return md;
	}

}
