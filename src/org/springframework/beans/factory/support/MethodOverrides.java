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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Set of method overrides, determining which, if any, methods on a
 * managed object the Spring IoC container will override at runtime.
 * @author Rod Johnson
 * @version $Id: MethodOverrides.java,v 1.2 2004-06-24 08:45:59 jhoeller Exp $
 */
public class MethodOverrides {

	private final List overrides = new LinkedList();

	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}

	public List getOverrides() {
		return overrides;
	}
	
	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}
	
	/**
	 * Return the override for the given method, if any.
	 * @param method method to check for overrides for
	 * @return the method override, or null if none
	 */
	public MethodOverride getOverride(Method method) {
		for (int i = 0; i < this.overrides.size(); i++) {
			MethodOverride methodOverride = (MethodOverride) this.overrides.get(i);
			if (methodOverride.matches(method)) {
				return methodOverride;
			}			
		}
		return null;
	}

}
