
 
package org.springframework.benchmark;

import org.springframework.beans.TestBean;
import org.springframework.load.AbortTestException;
import org.springframework.load.TestFailedException;

/**
 * 
 * @author Rod Johnson
 */
public class SingletonTest extends AbstractBeansTest {
	
		

	/**
	 * @see org.springframework.load.AbstractTest#runPass(int)
	 */
	protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
		TestBean prototype = (TestBean) bf.getBean("testSingleton");
	}


}
