package org.springframework.load;

import java.util.Random;

/**
 * Trivial Test implementation to demonstrate how to implement the
 * Test interface by extending AbstractTest. Configurable via
 * bean properties to allow delay behavior to be set, so that the
 * test suite can test the correctness of captured statistics.
 * 
 * @author Rod Johnson
 */
public class DemoTest extends AbstractTest {

	private Random rand;
	
	private int methodExecutionTime;
	
	private boolean useRandom;

	public DemoTest() {
		rand = new Random(hashCode());
		this.methodExecutionTime = 500;
	}

	/**
	 * @see AbstractTest#runPass(int)
	 */
	protected void runPass(int i) throws TestFailedException {
		try {
			//System.out.println(getName() + " run pass " + i);
			if (this.useRandom)
				Thread.sleep(rand.nextInt(this.methodExecutionTime));
			else
				Thread.sleep(this.methodExecutionTime);
		}
		catch (InterruptedException ex) {
		}
	}


	/**
	 * Returns the useRandom.
	 * @return boolean
	 */
	public boolean isUseRandom() {
		return useRandom;
	}

	
	/**
	 * Sets the useRandom.
	 * @param useRandom The useRandom to set
	 */
	public void setUseRandom(boolean useRandom) {
		this.useRandom = useRandom;
	}

	/**
	 * Returns the methodExecutionTime.
	 * @return boolean
	 */
	public int getMethodExecutionTime() {
		return methodExecutionTime;
	}

	/**
	 * Sets the methodExecutionTime.
	 * @param methodExecutionTime The methodExecutionTime to set
	 */
	public void setMethodExecutionTime(int methodExecutionTime) {
		this.methodExecutionTime = methodExecutionTime;
	}

}
