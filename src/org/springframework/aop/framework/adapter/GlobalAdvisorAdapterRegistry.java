/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;


/**
 * 
 * @author Rod Johnson
 * @version $Id: GlobalAdvisorAdapterRegistry.java,v 1.1 2003-12-11 14:51:37 johnsonr Exp $
 */
public class GlobalAdvisorAdapterRegistry extends DefaultAdvisorAdapterRegistry {
	
	private static GlobalAdvisorAdapterRegistry instance = new GlobalAdvisorAdapterRegistry();
	
	public static GlobalAdvisorAdapterRegistry getInstance() {
		return instance;
	}
	
	private GlobalAdvisorAdapterRegistry() {
		
	}

	
}
