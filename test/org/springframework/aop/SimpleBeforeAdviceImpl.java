package org.springframework.aop;

/**
 * 
 * @author Dmitriy Kopylenko
 * @version $Id: SimpleBeforeAdviceImpl.java,v 1.1 2004-02-27 14:28:16 dkopylenko Exp $
 */
public class SimpleBeforeAdviceImpl implements SimpleBeforeAdvice {
	
	private int invocationCounter;

	/**
	 * @see org.springframework.aop.SimpleBeforeAdvice#before()
	 */
	public void before() throws Throwable {
		System.out.println("before() method is called on " + getClass().getName());
		++invocationCounter;
	}

	public int getInvocationCounter() {
		return invocationCounter;
	}

}
