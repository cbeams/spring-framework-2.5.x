/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * 
 * @author Rod Johnson
 * @version $Id: TargetSource.java,v 1.1 2003-11-30 17:17:34 johnsonr Exp $
 */
public interface TargetSource {
	
	Class getTargetClass();
	
	//boolean isDynamic();
	
	Object getTarget() throws Exception;
	
	void releaseTarget(Object target) throws Exception;

}
