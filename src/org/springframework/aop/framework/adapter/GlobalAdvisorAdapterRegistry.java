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

import java.lang.ref.WeakReference;

/**
 * Singleton to publish a shared DefaultAdvisorAdapterRegistry.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: GlobalAdvisorAdapterRegistry.java,v 1.4 2004-05-23 20:13:03 jhoeller Exp $
 * @see DefaultAdvisorAdapterRegistry
 */
public abstract class GlobalAdvisorAdapterRegistry {

	/**
	 * Keep track of a single instance so we can return it to classes that request it.
	 * Needs to be a WeakReference to allow for proper garbage collection on shutdown!
	 */
	private static WeakReference instance;
	
	/**
	 * Return the per-VM AdvisorAdapterRegistry instance.
	 */
	public static synchronized AdvisorAdapterRegistry getInstance() {
		AdvisorAdapterRegistry registry = null;
		if (instance != null) {
			registry = (AdvisorAdapterRegistry) instance.get();
		}
		if (registry == null) {
			registry = new DefaultAdvisorAdapterRegistry();
			instance = new WeakReference(registry);
		}
		return registry;
	}

}
