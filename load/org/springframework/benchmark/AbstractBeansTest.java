package org.springframework.benchmark;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.load.AbortTestException;
import org.springframework.load.AbstractTest;
import org.springframework.load.TestFailedException;

/**
 * Test against a shared factory
 * @author Rod Johnson
 */
public abstract class AbstractBeansTest extends AbstractTest {

	protected BeanFactory beanFactory;
	
	protected int gets = 1;
	
	/**
	 * The shared fixture is a bean factory.
	 * All test threads will run against it.
	 * @see org.springframework.load.ConfigurableTest#setFixture(java.lang.Object)
	 */
	public void setFixture(Object o) {
		this.beanFactory = (BeanFactory) o;
	}
	
	public void setGets(int gets) {
		this.gets = gets;
	}
	
	protected final void runPass(int i) throws TestFailedException, AbortTestException, Exception {
		for (int j = 0; j < gets; j++) {
			runPass(i, j);
		}		
	}
	
	protected abstract void runPass(int i, int j) throws TestFailedException, AbortTestException, Exception;

}
