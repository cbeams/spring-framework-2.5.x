/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * A TargetSource is used to obtain the current "target" of 
 * an AOP invocation, which will be invoked via reflection if no
 * around advice chooses to end the interceptor chain itself. 
 * <br>If a TargetSource is "static", it will always
 * return the same target, allowing optimizations in the AOP framework.
 * Dynamic target sources can support pooling, hot swapping etc.
 * <br>Application developers don't usually need to work with TargetSources
 * directly: this is an AOP framework interface.
 * @author Rod Johnson
 * @version $Id: TargetSource.java,v 1.3 2004-02-22 09:59:59 johnsonr Exp $
 */
public interface TargetSource {
	
	Class getTargetClass();
	
	boolean isStatic();
	
	Object getTarget() throws Exception;
	
	void releaseTarget(Object target) throws Exception;

}
