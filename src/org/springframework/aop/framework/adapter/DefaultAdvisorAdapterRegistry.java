/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import java.util.LinkedList;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.support.DefaultInterceptionAroundAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DefaultAdvisorAdapterRegistry.java,v 1.1 2003-12-11 14:51:37 johnsonr Exp $
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry {
	
	private List adapters = new LinkedList();
	
	public DefaultAdvisorAdapterRegistry() {
		// Register well-known adapters
		registerAdvisorAdapter(new BeforeAdviceAdapter());
		registerAdvisorAdapter(new ThrowsAdviceAdapter());
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapterRegistry#wrap(java.lang.Object)
	 */
	public Advisor wrap(Object advice) throws UnknownAdviceTypeException {
		if (advice instanceof Advisor)
			return (Advisor) advice;
		if (advice instanceof Interceptor)
			return new DefaultInterceptionAroundAdvisor((Interceptor) advice);
		
		for (int i = 0; i < adapters.size(); i++) {
			AdvisorAdapter adapter = (AdvisorAdapter) adapters.get(i);
			if (adapter.supportsAdvice(advice)) {
				return adapter.wrap(advice);
			}
		}
		throw new UnknownAdviceTypeException(advice);
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapterRegistry#createInterceptionAroundAdvisor(org.springframework.aop.Advisor)
	 */
	public Interceptor getInterceptor(Advisor advisor) throws UnknownAdviceTypeException {
		if (advisor instanceof InterceptionAroundAdvisor)
			return ((InterceptionAroundAdvisor) advisor).getInterceptor();
		
		for (int i = 0; i < adapters.size(); i++) {
			AdvisorAdapter adapter = (AdvisorAdapter) adapters.get(i);
			if (adapter.supportsAdvisor(advisor)) {
				return adapter.getInterceptor(advisor);
			}
		}
		throw new UnknownAdviceTypeException(advisor);
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapterRegistry#registerAdvisorAdapter(org.springframework.aop.framework.adapter.AdvisorAdapter)
	 */
	public void registerAdvisorAdapter(AdvisorAdapter adapter) {
		adapters.add(adapter);
	}

}
