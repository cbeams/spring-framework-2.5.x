/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.support;

import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanFactoryUtils;

/**
 * BeanPostProcessor implementation that creates AOP proxies based on all candidate
 * advices in the current BeanFactory. This class is completely generic; it contains
 * no special code to handle any particular aspects, such as pooling aspects.
 * <br>
 * Note that all beans required to support the auto-proxying infrastructure, such as
 * advisors and all beans they reference, must have names beginning with the prefix
 * auto_. This allows us to avoid circular references, which might otherwise arise when
 * a PostProcessor attempted to use an advisor which referenced another bean that
 * was post processed by this post processor.
 * @author Rod Johnson
 * @version $Id: AdvisorAutoProxyCreator.java,v 1.3 2003-12-09 12:59:11 johnsonr Exp $
 */
public class AdvisorAutoProxyCreator extends AbstractAutoProxyCreator implements BeanFactoryAware {

	public final static int UNORDERED = 100;

	/**
	 * Prefix for candidate Advice bean names
	 */	
	public final static String AUTO_ADVICE_PREFIX = "auto_";
	
	public final static String AUTO_PROXY_INFRASTRUCTURE_PREFIX = "auto_";
	
	/**
	 * BeanFactory that owns this post processor
	 */
	private ListableBeanFactory owningFactory;

	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalStateException("Cannot use AdviceAutoProxyCreator without a ListableBeanFactory");
		}
		this.owningFactory = (ListableBeanFactory) beanFactory;
	}
	
	/**
	 * Return the owning ListableBeanFactory
	 */
	protected final ListableBeanFactory getBeanFactory() {
		return this.owningFactory;
	}

	/**
	 * Find all candidate advices to use in auto proxying.
	 * @return list of Advice
	 */
	protected List findCandidateAdvisors() {
		String[] adviceNames = BeanFactoryUtils.beanNamesIncludingAncestors(this.owningFactory, Advisor.class);
		List candidateAdvice = new LinkedList();
		for (int i = 0; i < adviceNames.length; i++) {
			String name = adviceNames[i];
			if (name.startsWith(AUTO_ADVICE_PREFIX)) {
				Advisor advice = (Advisor) this.owningFactory.getBean(name);
				candidateAdvice.add(advice);
			}
		}
		return candidateAdvice;
	}

	/**
	 * Find all eligible advices and for autoproxying this class.
	 * @return the empty list, not null, if there are no pointcuts or interceptors
	 */
	protected List findEligibleAdvisors(Class clazz) {
		List candidateAdvice = findCandidateAdvisors();
		List eligibleAdvice = new LinkedList();
		
		for (int i = 0; i < candidateAdvice.size(); i++) {
			// Sun, give me generics, please!
			Advisor candidate = (Advisor) candidateAdvice.get(i);
			if (AopUtils.canApply(candidate, clazz, null)) {
				eligibleAdvice.add(candidate);
				logger.info("Candidate Advice [" + candidate + "] ACCEPTED for class '" + clazz.getName() + "'");
			}
			else {
				logger.info("Candidate Advice [" + candidate + "] REJECTED for class '" + clazz.getName() + "'");
			}
		}
		
		return eligibleAdvice;
	}

	protected Object[] getInterceptorsAndAdvisorsForBean(Object bean, String name) {
		if (logger.isDebugEnabled())
			logger.debug("getInterceptorsAndAdvicesForBean with name '" + name + "'; singleton=" + this.owningFactory.isSingleton(name));
		List advices = findEligibleAdvisors(bean.getClass());
		advices = sortAdvisors(advices);
		if (advices.size() == 0 && !hasCustomInvoker(bean, name)) {
			return DO_NOT_PROXY;
		}
		return advices.toArray();
	}

	/**
	 * Sort based on ordering.
	 */
	protected List sortAdvisors(List l) {
		// TODO implement this
		//		Ordering:
		// PERFMON?
		// 1. tx interceptor - or weightings for pointcuts?
		// 2. tx 
		// 3. custom

		return l;
	}

	/**
	 * Does this bean need a custom invoker interceptor? Subclasses can override
	 * if they wish: for example, to implement pooling. This implementation always returns false.
	 * @param bean bean in question
	 * @param beanName name of the bean
	 */
	protected boolean hasCustomInvoker(Object bean, String beanName) {
		return false;
	}
	
	/**
	 * We override this to ensure that we don't get into circular reference hell
	 * when our own infrastructure (such as this class) depends on advisors that depend
	 * on beans...
	 * @see org.springframework.aop.framework.support.AbstractAutoProxyCreator#shouldSkip(java.lang.Object, java.lang.String)
	 */
	protected boolean shouldSkip(Object bean, String name) {
		return name.startsWith(AUTO_PROXY_INFRASTRUCTURE_PREFIX);
	}

	
}
