/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AopProxy.java,v 1.16 2003-12-01 15:40:46 johnsonr Exp $
 */
public interface AopProxy {
	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the thread context class loader.
	 */
	public abstract Object getProxy();
	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the given class loader.
	 */
	public abstract Object getProxy(ClassLoader cl);
}