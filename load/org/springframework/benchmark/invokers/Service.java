/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

/**
 * 
 * @author Rod Johnson
 */
public interface Service {
	
	
	void takeUpToMillis(int i);
	
	void nop();
	
	void notAdvised();

}
