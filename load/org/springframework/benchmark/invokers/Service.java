/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

/**
 * 
 * @author Rod Johnson
 * @version $Id: Service.java,v 1.1 2003-12-02 22:31:06 johnsonr Exp $
 */
public interface Service {
	
	
	void takeUpToMillis(int i);
	
	void nop();
	
	void notAdvised();

}
