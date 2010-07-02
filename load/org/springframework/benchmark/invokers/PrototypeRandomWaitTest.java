/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

/**
 * 
 * @author Rod Johnson
 */
public class PrototypeRandomWaitTest extends RandomWaitTest {

	/**
	 * @see org.springframework.benchmark.invokers.RandomWaitTest#getService()
	 */
	protected Service getService() {
		return (Service) bf.getBean(bean);
	}
}
