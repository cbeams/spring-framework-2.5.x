/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Extension of MethodOverride that represents an arbitrary
 * override of a method by the IoC container.<br>
 * Any non-final method can be overridden, irrespective of its
 * parameters and return types.
 * @author Rod Johnson
 * @version $Id: ReplaceOverride.java,v 1.1 2004-06-28 11:44:18 johnsonr Exp $
 */
public class ReplaceOverride extends MethodOverride {
	
	private final String callback;
	
	/** 
	 * List of String. Identifying signatures.
	 */
	private List typeIdentifiers = new LinkedList();

	public ReplaceOverride(String methodName, String callback) {
		super(methodName);
		this.callback = callback;
	}
	
	/**
	 * Add a fragment of a class string, like "Exception"
	 * or "java.lang.Exc", to identify a parameter type
	 * @param s substring of class FQN
	 */
	public void addTypeIdentifier(String s) {
		this.typeIdentifiers.add(s);
	}
	
	
	/**
	 * @see org.springframework.beans.factory.support.MethodOverride#matches(java.lang.reflect.Method)
	 */
	public boolean matches(Method method, MethodOverrides overrides) {
		// TODO could cache result for efficiency
		if (!method.getName().equals(getMethodName())) {
			// It can't match
			return false;
		}
		
		if (!overrides.isOverloadedMethodName(method.getName())) {
			// No overloaded: don't worry about arg type matching
			return true;
		}
		
		// If we get to here, we need to insist on precise argument matching
		if (typeIdentifiers.size() != method.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < typeIdentifiers.size(); i++) {
			String identifier = (String) typeIdentifiers.get(i);
			if (method.getParameterTypes()[i].getName().indexOf(identifier) == -1) {
				// This parameter can't match
				return false;
			}
		}
		return true;			
	}
	
	/**
	 * @return the name of the bean implementing MethodReplacer
	 */
	public String getMethodReplacerBeanName() {
		return callback;
	}


	public String toString() {
		return "Replace override for method '" + getMethodName() + "; will callback bean '" + callback + "'";
	}
}
