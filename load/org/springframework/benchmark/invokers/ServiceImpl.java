/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

import java.util.Random;

/**
 * 
 * @author Rod Johnson
 */
public class ServiceImpl implements Service {
	
	static Random rand = new Random();
	
	public ServiceImpl() {
	//	System.err.println("NEW " + getClass().getName());
	}

	/**
	 * @see org.springframework.benchmark.invokers.Service#doStringComparisons(int)
	 */
	public void takeUpToMillis(int millis) {
		//int i;
		//for (int i = 0; i < n; i++) {
		//	"fooooowpeowipeoripwoerp3woeir".compareTo("barwieruwporijopqgnoqwenoiqwrje");
		//}
		
		if (millis <= 0)
			return;
		
		int ms = rand.nextInt(millis);
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @see org.springframework.benchmark.invokers.Service#nop()
	 */
	public void nop() {
		throw new UnsupportedOperationException();
	}
	
	public void notAdvised() {
	}

}
