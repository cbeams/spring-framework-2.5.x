/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.load.AbstractTest;

/**
 * 
 * @author Rod Johnson
 */
public abstract class AbstractBenchmark extends AbstractTest  {
	
	public final static int USERS = 99; // indexed from 1 to 100
	
	public final static int ITEMS = 999;
	
	protected BenchmarkFactory factory;
	

	/**
	 * We share the benchmark with all other tests
	 * @see org.springframework.load.ConfigurableTest#setFixture(java.lang.Object)
	 */
	public void setFixture(Object context) {
		try {
			this.factory = (BenchmarkFactory) context;
		} 
		catch (Exception ex) {
			throw new RuntimeException("Can't create test: " + ex);
		}
	}
	
	public String toString() {
		return getClass().getName() + ": factory=" + factory;
	}
}
