/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Set of method overrides, determining which, if any,
 * methods on a managed object the Spring IoC container
 * will override at runtime.
 * @author Rod Johnson
 * @version $Id: MethodOverrides.java,v 1.1 2004-06-23 21:13:35 johnsonr Exp $
 */
public class MethodOverrides {

	private List overrides = new LinkedList();

	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}

	public List getOverrides() {
		return overrides;
	}
	
	public boolean isEmpty() {
		return overrides.isEmpty();
	}
	
	/**
	 * Return null if no override for the given method.
	 * @param method method to check for overrides for
	 * @return
	 */
	public MethodOverride getOverride(Method method) {
		for (int i = 0; i < overrides.size(); i++) {
			MethodOverride methodOverride = (MethodOverride) overrides.get(i);
			if (methodOverride.matches(method)) {
				return methodOverride;
			}			
		}
		return null;
	}

}
