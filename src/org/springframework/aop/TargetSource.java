/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * 
 * @author Rod Johnson
 * @version $Id: TargetSource.java,v 1.2 2003-11-30 18:10:53 johnsonr Exp $
 */
public interface TargetSource {
	
	Class getTargetClass();
	
	boolean isStatic();
	
	Object getTarget() throws Exception;
	
	void releaseTarget(Object target) throws Exception;

}
