/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

import java.lang.reflect.Method;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.ControlFlowPointcut;
import org.springframework.aop.support.DefaultBeforeAdvisor;
import org.springframework.load.AbortTestException;
import org.springframework.load.TestFailedException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: CflowTest.java,v 1.1 2003-12-07 18:56:59 johnsonr Exp $
 */
public class CflowTest extends RandomWaitTest {
	
	protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
		Service myService = getService();
		myService.takeUpToMillis(maxMillis);
	
		for (int j = 0; j < notAdvised; j++) {
			myService.notAdvised();
		}
	}

	static boolean inited;

	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void afterPropertiesSet() throws Exception {
		service = (Service) bf.getBean(bean);
		if (!inited) {
			// avoiding adding cflow twice
			Advised advised = (Advised) service;
			BeforeAdvice ba = new MethodBeforeAdvice() {
				public void before(Method m, Object[] args, Object target) throws Throwable {
					//System.err.println(m);
				}
			};
			ControlFlowPointcut cflow = new ControlFlowPointcut(getClass(), "runPass");
			advised.addAdvisor(new DefaultBeforeAdvisor(cflow, ba));
			
			System.out.println(advised.toProxyConfigString());
			assertEquals("right number of advisors", 2, advised.getAdvisors().length);
			System.out.println("Service bean class for group " + getGroup() + "=" + service.getClass());
			
			inited = true;
		}
	}
	
	
}
