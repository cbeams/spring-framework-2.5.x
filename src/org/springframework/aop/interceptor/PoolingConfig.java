/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

/**
 * Config interface for a pooling invoker.
 * @see org.springframework.aop.interceptor.AbstractPoolingInvokerInterceptor
 * @author Rod Johnson
 * @version $Id: PoolingConfig.java,v 1.1 2003-11-24 11:27:17 johnsonr Exp $
 */
public interface PoolingConfig {
	
	int getMaxSize();
	
	int getActive() throws UnsupportedOperationException;
	
	int getFree() throws UnsupportedOperationException;
	
	/**
	 * @return total number of invocations on pooled invoker
	 */
	int getInvocations();

}
