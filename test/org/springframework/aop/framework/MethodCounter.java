/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;


/**
 * Useful abstract superclass for counting advices etc.
 * 
 * @author Rod Johnson
 * @version $Id: MethodCounter.java,v 1.1 2003-12-08 11:23:51 johnsonr Exp $
 */
public class MethodCounter {
	
	/** Method name --> count, does not understand overloading */
	private HashMap map = new HashMap();
	
	private int allCount;
	
	protected void count(Method m) {
		count(m.getName());
	}
	
	protected void count(String methodName) {
		Integer I = (Integer) map.get(methodName);
		I = (I != null) ? new Integer(I.intValue() + 1) : new Integer(1);
		map.put(methodName, I);
		++allCount;
	}
	
	public int getCalls(String methodName) {
		Integer I = (Integer) map.get(methodName);
		return (I != null) ? I.intValue() : 0;
	}
	
	public int getCalls() {
		return allCount;
	}
}