/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.benchmark.invokers;

import java.lang.reflect.Method;

import org.springframework.aop.Advisor;
import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.support.ControlFlowPointcut;
import org.springframework.load.AbortTestException;
import org.springframework.load.TestFailedException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: CflowTest.java,v 1.2 2004-02-23 15:51:05 dkopylenko Exp $
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

			//DK
			GlobalAdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
			//Created overloaded wrap() method to associate pointcut with Advisor
			Advisor beforeAdvisor = advisorAdapterRegistry.wrap(ba, cflow);

			//advised.addAdvisor(new DefaultBeforeAdvisor(cflow, ba));
			advised.addAdvisor(beforeAdvisor);

			System.out.println(advised.toProxyConfigString());
			assertEquals("right number of advisors", 2, advised.getAdvisors().length);
			System.out.println("Service bean class for group " + getGroup() + "=" + service.getClass());

			inited = true;
		}
	}

}
