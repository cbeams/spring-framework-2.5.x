/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.target;

/**
 * Statistics for a ThreadLocal TargetSource.
 * @author Rod Johnson
 * @version $Id: ThreadLocalTargetSourceStats.java,v 1.1 2003-11-30 17:17:34 johnsonr Exp $
 */
public interface ThreadLocalTargetSourceStats {
	
	/**
	 * @return all clients given one of us
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
