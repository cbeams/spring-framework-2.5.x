/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

/**
 * Statistics for a ThreadLocal invoker.
 * @author Rod Johnson
 * @see org.springframework.aop.interceptor.ThreadLocalInvokerInterceptor
 * @version $Id: ThreadLocalInvokerStats.java,v 1.1 2003-11-24 20:43:43 johnsonr Exp $
 */
public interface ThreadLocalInvokerStats {
	
	/**
	 * @return all invocations against the apartment invoker
	 */
	int getInvocations();

	/**
	 * @return hits that were satisfied by a thread bound object
	 */
	int getHits();

	/**
	 * @return thread bound objects created
	 */
	int getObjects();

}
