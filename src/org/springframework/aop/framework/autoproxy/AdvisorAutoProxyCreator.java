/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version $Id: AdvisorAutoProxyCreator.java,v 1.7 2004-03-18 02:46:16 trisberg Exp $
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
