/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

/**
 * 
 * @author Rod Johnson
 */
public class SynchIncrementer extends ServiceImpl {
	
	private int count;

	/**
	 * @see org.springframework.benchmark.invokers.Service#doStringComparisons(int)
	 */
	public void takeUpToMillis(int n) {
		//System.err.println("Synched in " + hashCode());
		super.takeUpToMillis(n);
		synchronized (this) {
			++count;
		}
	}


}
