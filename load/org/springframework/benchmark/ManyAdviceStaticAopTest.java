/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark;

import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Rod Johnson
 */
public class ManyAdviceStaticAopTest extends StaticAopTest {

	private int advices;

	public void setAdvices(int advices) {
		this.advices = advices;
	}

	protected void addFurtherAdvice(ProxyFactory pf) {
		System.err.println("Adding " + advices + " further advices");
		
		for (int i = 0; i < advices; i++) {
			MethodInterceptor mi = new Advices.NopInterceptor();
			pf.addAdvice(mi);
		}
		if (pf.getAdvisors().length <= this.advices) {
			throw new RuntimeException("Too few advices");
		}
	}

}
