/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;


/**
 * Singleton to publish a shared DefaultAdvisorAdapterRegistry.
 * @author Rod Johnson
 * @version $Id: GlobalAdvisorAdapterRegistry.java,v 1.2 2003-12-11 16:41:44 johnsonr Exp $
 */
public class GlobalAdvisorAdapterRegistry extends DefaultAdvisorAdapterRegistry {
	
	private static GlobalAdvisorAdapterRegistry instance = new GlobalAdvisorAdapterRegistry();
	
	/**
	 * @return the per-VM AdapterRegistry instance.
	 */
	public static GlobalAdvisorAdapterRegistry getInstance() {
		return instance;
	}
	
	/**
	 * Constructor to enforce the Singleton pattern.
	 */
	private GlobalAdvisorAdapterRegistry() {
	}
	
}
