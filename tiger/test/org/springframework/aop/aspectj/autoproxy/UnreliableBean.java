package org.springframework.aop.aspectj.autoproxy;

/**
 * @author Rob Harrop
 */
public class UnreliableBean {

	private int calls;

	public int unreliable() {
		this.calls++;
		 if(this.calls % 2 != 0) {
			 throw new RetryableException("foo");
		 }

		return this.calls;
	}

}
