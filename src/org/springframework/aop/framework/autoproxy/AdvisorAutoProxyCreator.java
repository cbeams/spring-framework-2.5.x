/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy;

import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanFactoryUtils;

/**
 * BeanPostProcessor implementation that creates AOP proxies based on all candidate
 * advices in the current BeanFactory. This class is completely generic; it contains
 * no special code to handle any particular aspects, such as pooling aspects.
 * <br>
 * Note that all beans required to support the auto-proxying infrastructure, such as
 * advisors and all beans they reference, must have names beginning with the prefix
 * <code>auto_</code>. This allows us to avoid circular references, which might otherwise arise when
 * a PostProcessor attempted to use an advisor which referenced another bean that
 * was post processed by this post processor.
 * Note that this default value (_auto) can be changed by setting the
 * <code>infrastructureBeanNamePrefix</code> property.
 * @author Rod Johnson
 * @version $Id: AdvisorAutoProxyCreator.java,v 1.2 2003-12-12 20:52:01 johnsonr Exp $
 */
public class AdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	public final static int UNORDERED = 100;

	/**
	 * Prefix for candidate Advice bean names
	 */	
	public final static String AUTO_ADVICE_PREFIX = "auto_";
	
	public final static String DEFAULT_INFRASTRUCTURE_BEAN_NAME_PREFIX = "auto_";
	
	/**
	 * BeanFactory that owns this post processor
	 */
	private ListableBeanFactory owningFactory;
	
	/** Prefix that will screen out auto proxying */
	private String infrastructureBeanNamePrefix = DEFAULT_INFRASTRUCTURE_BEAN_NAME_PREFIX;

	/**
	 * @return return the prefix for bean names that will cause them not to
	 * be considered for autoproxying by this object.
	 */
	public String getInfrastructureBeanNamePrefix() {
		return this.infrastructureBeanNamePrefix;
	}

	/** 
	 * Set the prefix for bean names that will cause them to 
	 * be excluded for autoproxying by this object. This prefix
	 * should be set to avoid circular references. Default value is
	 * <code>_auto</code>.
	 * @param infrastructureBeanNamePrefix new exclusion prefix
	 */
	public void setInfrastructureBeanNamePrefix(String infrastructureBeanNamePrefix) {
		this.infrastructureBeanNamePrefix = infrastructureBeanNamePrefix;
	}
	
	
	/**
	 * If subclasses implement BeanFactoryAware, they should invoke this
	 * form of the method as well.
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalStateException("Cannot use AdviceAutoProxyCreator without a ListableBeanFactory");
		}
		this.owningFactory = (ListableBeanFactory) beanFactory;
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
			if (name.startsWith(infrastructureBeanNamePrefix)) {
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
		return name.startsWith(infrastructureBeanNamePrefix);
	}

	
}
