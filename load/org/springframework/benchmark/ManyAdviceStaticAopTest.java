/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ManyAdviceStaticAopTest.java,v 1.1 2003-11-17 08:58:25 johnsonr Exp $
 */
public class ManyAdviceStaticAopTest extends StaticAopTest {

	private int advices;

	/**
	 * @see org.springframework.benchmark.StaticAopTest#addFurtherAdvice(org.springframework.aop.framework.ProxyFactory)
	 */
	protected void addFurtherAdvice(ProxyFactory pf) {
		
		System.err.println("Adding " + advices + " further advices");
		
		for (int i = 0; i < advices; i++) {
			MethodInterceptor mi = new Advices.NopInterceptor();
			pf.addInterceptor(mi);
		}
		if (pf.getAdvisors().length <= this.advices) 
			throw new RuntimeException("Too few advices");
	}

	/**
	 * @param i
	 */
	public void setAdvices(int advices) {
		this.advices = advices;
	}

}
