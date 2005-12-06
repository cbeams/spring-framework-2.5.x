/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ParameterNameDiscoverer implementation that tries several ParameterNameDiscoverers
 * in succession. Those added first in the addDiscoverer() method have highest priority.
 * If one returns null, the next will be tried.
 * The default behaviour is always to return null if no discoverer matches.
 * @author Rod Johnson
 * @since 2.0
 */
public class PrioritizedParameterNameDiscoverer implements ParameterNameDiscoverer {
	
	private static ParameterNameDiscoverer RETURNS_NULL = new ParameterNameDiscoverer() {
		public String[] getParameterNames(Method m, Class clazz) {
			return null;
		}
		
		public String[] getParameterNames(Constructor ctor) {
			return null;
		}
	};
	
	private List parameterNameDiscoverers = new LinkedList();
	
	public PrioritizedParameterNameDiscoverer() {
		parameterNameDiscoverers.add(RETURNS_NULL);
	}
	
	public void addDiscoverer(ParameterNameDiscoverer pnd) {
		// Add just before returnsNull
		this.parameterNameDiscoverers.add(parameterNameDiscoverers.size() - 1, pnd);
	}

	public String[] getParameterNames(Method m, Class clazz) {
		for (Iterator it = parameterNameDiscoverers.iterator(); it.hasNext(); ) {
			ParameterNameDiscoverer pmd = (ParameterNameDiscoverer) it.next();
			String[] result = pmd.getParameterNames(m, clazz);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public String[] getParameterNames(Constructor ctor) {
		for (Iterator it = parameterNameDiscoverers.iterator(); it.hasNext(); ) {
			ParameterNameDiscoverer pmd = (ParameterNameDiscoverer) it.next();
			String[] result = pmd.getParameterNames(ctor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
