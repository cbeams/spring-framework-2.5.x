/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.ControlFlow;
import org.springframework.core.ControlFlowFactory;

/**
 * Abstract BeanPostProcessor implementation that creates AOP proxies.
 * This class is completely generic; it contains no special code to handle
 * any particular aspects, such as pooling aspects.
 *
 * <p>Subclasses must implement the abstract <code>findCandidateAdvisors()</code>
 * method to return a list of Advisors applying to any object. Subclasses can
 * also override the inherited <code>shouldSkip</code> method to exclude certain
 * objects from auto-proxying, but they must be careful to invoke the base
 * <code>shouldSkip</code> method, which tries to avoid circular reference
 * problems and infinite loops.
 *
 * <p>Advisors or advices requiring ordering should implement the Ordered interface.
 * This class sorts advisors by Ordered order value. Advisors that don't implement
 * the Ordered interface will be considered to be unordered, and will appear
 * at the end of the advisor chain in undefined order.
 *
 * @author Rod Johnson
 * @see #findCandidateAdvisors
 * @see org.springframework.aop.support.AopUtils
 */
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	/**
	 * We override this method to ensure that all candidate advisors are materialized
	 * under a stack trace including this bean. Otherwise, the dependencies won't
	 * be apparent to the circular-reference prevention strategy in AbstractBeanFactory.
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		findCandidateAdvisors();
	}


	protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String name, TargetSource targetSource) {
		List advisors = findEligibleAdvisors(beanClass);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		advisors = sortAdvisors(advisors);
		return advisors.toArray();
	}

	/**
	 * Find all eligible advices for auto-proxying this class.
	 * @return the empty list, not null, if there are no pointcuts or interceptors
	 * @see #findCandidateAdvisors
	 */
	protected List findEligibleAdvisors(Class clazz) {
		List eligibleAdvisors = AopUtils.findAdvisorsThatCanApply(findCandidateAdvisors(), clazz);
		extendCandidateAdvisors(eligibleAdvisors);
		return eligibleAdvisors;
	}

	/**
	 * Sort advisors based on ordering. Subclasses may choose to override this
	 * method to customize the sorting strategy.
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.OrderComparator
	 */
	protected List sortAdvisors(List advisors) {
		return AopUtils.sortAdvisorsByOrder(advisors);
	}

	/**
	 * We override this to ensure that we don't get into circular reference hell
	 * when our own infrastructure (such as this class) depends on advisors that depend
	 * on beans... We use a ControlFlow object to check that we didn't arrived at this
	 * call via our own <code>findCandidateAdvisors()</code> method.
	 * @see #findCandidateAdvisors()
	 * @see org.springframework.core.ControlFlow
	 */
	protected boolean shouldSkip(Class beanClass, String name) {
		// TODO consider pulling this test into AbstractBeanFactory.applyBeanPostProcessors(),
		// to protect all BeanPostProcessors.
		ControlFlow cflow = ControlFlowFactory.createControlFlow();
		return cflow.under(AbstractAdvisorAutoProxyCreator.class, "findCandidateAdvisors");
	}


	/**
	 * Find all candidate advisors to use in auto-proxying.
	 * @return list of Advisors
	 */
	protected List findCandidateAdvisors() {
		if (!(getBeanFactory() instanceof ListableBeanFactory)) {
			throw new IllegalStateException(
					"Cannot use DefaultAdvisorAutoProxyCreator without a ListableBeanFactory");
		}
		ListableBeanFactory owningFactory = (ListableBeanFactory) getBeanFactory();

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the auto-proxy creator apply to them!
		String[] adviceNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(owningFactory, Advisor.class, true, false);
		List candidateAdvisors = new LinkedList();
		for (int i = 0; i < adviceNames.length; i++) {
			String name = adviceNames[i];
			if (isEligibleForProxying(name)) {
				Advisor advisor = (Advisor) owningFactory.getBean(name);
				candidateAdvisors.add(advisor);
			}
		}
		return candidateAdvisors;
	}

	/**
	 * Return whether the bean with the given name is eligible for proxying
	 * in the first place.
	 * @param beanName the name of the target bean
	 */
	protected boolean isEligibleForProxying(String beanName) {
		return true;
	}

	/**
	 * Extension hook that subclasses can choose to use to additional Advisors,
	 * given the sorted Advisors obtained to date.
	 * <p>Typically used to add Advisors that expose contextual information
	 * required by some of the later advisors.
	 * @param candidateAdvisors Advisors that have already been identified as
	 * applying to a given bean
	 */
	protected void extendCandidateAdvisors(List candidateAdvisors) {
		// Empty default implementation.
	}

}
