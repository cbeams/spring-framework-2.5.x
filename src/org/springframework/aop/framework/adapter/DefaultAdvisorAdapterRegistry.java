/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import java.util.LinkedList;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * @author Rod Johnson
 * @version $Id: DefaultAdvisorAdapterRegistry.java,v 1.8 2004-02-23 18:21:08 dkopylenko Exp $
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry {
	
	private List adapters = new LinkedList();
	
	public DefaultAdvisorAdapterRegistry() {
		// register well-known adapters
		registerAdvisorAdapter(new BeforeAdviceAdapter());
		registerAdvisorAdapter(new AfterReturningAdviceAdapter());
		registerAdvisorAdapter(new ThrowsAdviceAdapter());
	}

	public Advisor wrap(Object advice) throws UnknownAdviceTypeException {
		if (advice instanceof Advisor) {
			return (Advisor) advice;
		}
		if (advice instanceof Interceptor) {
			// So well-known it doesn't even need an adapter
			return new DefaultPointcutAdvisor(advice);
		}
		for (int i = 0; i < this.adapters.size(); i++) {
			// Check that it is supported
			AdvisorAdapter adapter = (AdvisorAdapter) this.adapters.get(i);
			if (adapter.supportsAdvice(advice)) {
				return new DefaultPointcutAdvisor(advice);
			}
		}
		throw new UnknownAdviceTypeException(advice);
	}

	public Interceptor getInterceptor(Advisor advisor) throws UnknownAdviceTypeException {
		Object advice = advisor.getAdvice();
		if (advice instanceof Interceptor) {
			return (Interceptor) advice;
		}
		for (int i = 0; i < this.adapters.size(); i++) {
			AdvisorAdapter adapter = (AdvisorAdapter) this.adapters.get(i);
			if (adapter.supportsAdvice(advice)) {
				return adapter.getInterceptor(advisor);
			}
		}
		throw new UnknownAdviceTypeException(advisor.getAdvice());
	}

	public void registerAdvisorAdapter(AdvisorAdapter adapter) {
		this.adapters.add(adapter);
	}

}
