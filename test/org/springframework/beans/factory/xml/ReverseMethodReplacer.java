/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.beans.factory.support.MethodReplacer;

/**
 * 
 * @author Rod Johnson
 */
public class ReverseMethodReplacer implements MethodReplacer, Serializable {

	/**
	 * @see org.springframework.beans.factory.support.MethodReplacer#reimplement(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object reimplement(Object o, Method m, Object[] args) throws Throwable {
		String s = (String) args[0];
		return new StringBuffer(s).reverse().toString();
	}

}
