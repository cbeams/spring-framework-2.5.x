/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

/**
 * 
 * @author Rod Johnson
 * @version $Id: PrototypeRandomWaitTest.java,v 1.1 2003-12-02 22:31:07 johnsonr Exp $
 */
public class PrototypeRandomWaitTest extends RandomWaitTest {

	/**
	 * @see org.springframework.benchmark.invokers.RandomWaitTest#getService()
	 */
	protected Service getService() {
		return (Service) bf.getBean(bean);
	}
}
