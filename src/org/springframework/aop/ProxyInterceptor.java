/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import org.aopalliance.intercept.Interceptor;

/**
 * Interface to be implemented by interceptors that have
 * a proxy target.
 * @author Rod Johnson
 * @since 14-Mar-2003
 * @version $Revision: 1.1 $
 */
public interface ProxyInterceptor extends Interceptor {
	
	Object getTarget();

}
