/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy;

import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * BeanPostProcessor implementation that creates AOP proxies based on all candidate
 * Advisors in the current BeanFactory. This class is completely generic; it contains
 * no special code to handle any particular aspects, such as pooling aspects.
 *
 * <p>It's possible to filter out advisors -- for example, to use multiple post processors
 * of this type in the same factory - -by setting the <code>usePrefix</code> property
 * to true, in which case only advisors beginning with the AdvisorAutoProxyCreator's
 * bean name followed by a dot (like "aapc.") will be used. This default prefix can be
 * changed from the bean name by setting the <code>advisorBeanNamePrefix</code> property.
 * The separator (.) will also be used in this case.
 *
 * @author Rod Johnson
 * @version $Id: AdvisorAutoProxyCreator.java,v 1.6 2004-02-04 17:42:50 jhoeller Exp $
 */
public class AdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator implements BeanNameAware {

	/** Separator between prefix and remainder of bean name */
	public final static String SEPARATOR = ".";
	
	/** Prefix that will screen out auto proxying */
	private String advisorBeanNamePrefix;
	
	private boolean usePrefix;
	
	
	/**
	 * Return the prefix for bean names that will cause them not to
	 * be considered for autoproxying by this object.
	 */
	public String getAdvisorBeanNamePrefix() {
		return this.advisorBeanNamePrefix;
	}

	/** 
	 * Set the prefix for bean names that will cause them to be excluded for
	 * autoproxying by this object. This prefix should be set to avoid
	 * circular references. Default value is the bean name of this object.
	 * @param infrastructureBeanNamePrefix new exclusion prefix
	 */
	public void setAdvisorBeanNamePrefix(String infrastructureBeanNamePrefix) {
		this.advisorBeanNamePrefix = infrastructureBeanNamePrefix;
	}
	

	/**
	 * @return Returns the usePrefix.
	 */
	public boolean getUsePrefix() {
		return this.usePrefix;
	}
	
	/**
	 * @param usePrefix The usePrefix to set.
	 */
	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}
	
	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		// If no infrastructure bean name prefix has been set, override it
		if (advisorBeanNamePrefix == null) {
			advisorBeanNamePrefix = name + SEPARATOR;
		}
	}


	/**
	 * Find all candidate advices to use in auto proxying.
	 * @return list of Advice
	 */
	protected List findCandidateAdvisors() {
		if (!(getBeanFactory() instanceof ListableBeanFactory)) {
			throw new IllegalStateException("Cannot use AdvisorAutoProxyCreator without a ListableBeanFactory");
		}
		ListableBeanFactory owningFactory = (ListableBeanFactory) getBeanFactory();
		
		String[] adviceNames = BeanFactoryUtils.beanNamesIncludingAncestors(owningFactory, Advisor.class);
		List candidateAdvisors = new LinkedList();
		for (int i = 0; i < adviceNames.length; i++) {
			String name = adviceNames[i];
			if (!usePrefix || name.startsWith(advisorBeanNamePrefix)) {
				Advisor advisor = (Advisor) owningFactory.getBean(name);
				candidateAdvisors.add(advisor);
			}
		}
		return candidateAdvisors;
	}
	
}
