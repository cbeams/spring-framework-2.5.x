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

package org.springframework.aop.framework.adapter;


/**
 * Singleton to publish a shared DefaultAdvisorAdapterRegistry.
 * @author Rod Johnson
 * @version $Id: GlobalAdvisorAdapterRegistry.java,v 1.3 2004-03-18 02:46:10 trisberg Exp $
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
