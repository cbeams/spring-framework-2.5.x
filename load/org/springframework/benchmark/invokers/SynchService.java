/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

/**
 * 
 * @author Rod Johnson
 * @version $Id: SynchService.java,v 1.1 2003-12-02 22:31:06 johnsonr Exp $
 */
public class SynchService extends ServiceImpl {

	/**
	 * @see org.springframework.benchmark.invokers.Service#doStringComparisons(int)
	 */
	public synchronized void takeUpToMillis(int n) {
		//System.err.println("Synched in " + hashCode());
		super.takeUpToMillis(n);
	}


}
