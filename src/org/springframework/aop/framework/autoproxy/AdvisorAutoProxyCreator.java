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
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanFactoryUtils;

/**
 * BeanPostProcessor implementation that creates AOP proxies based on all candidate
 * advices in the current BeanFactory. This class is completely generic; it contains
 * no special code to handle any particular aspects, such as pooling aspects.
 * <br>
 * Note that all beans required to support the auto-proxying infrastructure, such as
 * advisors and all beans they reference, must have names beginning with the prefix
 * of the name of the AdvisorAutoProxyCreator followed by a separator, the "." character. 
 * Thus if the AdvisorAutoProxyCreator bean name is <code>aapc</code>, all its advisors and their
 * support classes should begin with names of the form <code>aapc_</code>.
 * This allows us to avoid circular references, which might otherwise arise when
 * a PostProcessor attempted to use an advisor which referenced another bean that
 * was post processed by this post processor.
 * Note that this default prefix can be changed from the bean name by setting the
 * <code>infrastructureBeanNamePrefix</code> property. The separator (.) will be used.
 * @author Rod Johnson
 * @version $Id: AdvisorAutoProxyCreator.java,v 1.4 2003-12-13 21:53:41 johnsonr Exp $
 */
public class AdvisorAutoProxyCreator extends AbstractAutoProxyCreator implements BeanNameAware {

	public final static int UNORDERED = 100;
	
	/** Separator between prefix and remainder of bean name */
	public final static String SEPARATOR = ".";
	
	/**
	 * BeanFactory that owns this post processor
	 */
	private ListableBeanFactory owningFactory;
	
	/** Prefix that will screen out auto proxying */
	private String infrastructureBeanNamePrefix;
	
	
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
	 * the bean name of this object.
	 * @param infrastructureBeanNamePrefix new exclusion prefix
	 */
	public void setInfrastructureBeanNamePrefix(String infrastructureBeanNamePrefix) {
		this.infrastructureBeanNamePrefix = infrastructureBeanNamePrefix;
	}
	

	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		// If no infrastructure bean name prefix has been set, override it
		if (infrastructureBeanNamePrefix == null) {
			infrastructureBeanNamePrefix = name + SEPARATOR;
		}
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
		if (advices.isEmpty()) {
			return DO_NOT_PROXY;
		}
		advices = sortAdvisors(advices);
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
	 * We override this to ensure that we don't get into circular reference hell
	 * when our own infrastructure (such as this class) depends on advisors that depend
	 * on beans...
	 * @see org.springframework.aop.framework.support.AbstractAutoProxyCreator#shouldSkip(java.lang.Object, java.lang.String)
	 */
	protected boolean shouldSkip(Object bean, String name) {
		return name.startsWith(infrastructureBeanNamePrefix);
	}
	
}
