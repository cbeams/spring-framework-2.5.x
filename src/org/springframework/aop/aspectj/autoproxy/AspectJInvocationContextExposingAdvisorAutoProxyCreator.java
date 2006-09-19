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

package org.springframework.aop.aspectj.autoproxy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.PartialOrder.PartialComparable;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.core.Ordered;

/**
 * An InvocationContextExposingAdvisorAutoProxyCreator that understands AspectJ's
 * rules for advice precedence when multiple pieces of advice come from the same
 * aspect.
 *
 * @author Adrian Colyer
 * @since 2.0
 */
public class AspectJInvocationContextExposingAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {

	/**
	 * Adds an {@link ExposeInvocationInterceptor} to the beginning of the advice chain.
	 * These additional advices are needed when using AspectJ expression pointcuts
	 * and when using AspectJ-style advice.
	 */
	protected void extendCandidateAdvisors(List candidateAdvisors) {
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(candidateAdvisors);
	}

	/**
	 * <p>
	 * Keep the special ExposeInvocationInterceptor at position 0 if
	 * present. Sort the rest by order. If two pieces of advice have
	 * come from the same aspect they will have the same order.
	 * Advice from the same aspect is then further ordered according to the
	 * following rules:</p>
	 * <ul>
	 * <li>if either of the pair is after advice, then the advice declared
	 * last gets highest precedence (runs last)</li>
	 * <li>otherwise the advice declared first gets highest precedence (runs first)</li>
	 * </ul>
	 * <p><b>Important:</b> advisors are sorted in precedence order, from highest
	 * precedence to lowest. "On the way in" to a join point, the highest precedence
	 * advisor should run first. "On the way out" of a join point, the highest precedence
	 * advisor should run last.</p>
	 */
	protected List sortAdvisors(List advisors) {
		if (advisors == null || advisors.isEmpty()) {
			return advisors;
		}
		
		boolean hasExposeInvocationInterceptor = excludeExposeInvocationInterceptorFromSorting(advisors);	
		List ordered = sortRemainingAdvisors(advisors);

		if (hasExposeInvocationInterceptor) {
			ordered.add(0,ExposeInvocationInterceptor.ADVISOR);
		}
		return ordered;
	
	}

	private List sortRemainingAdvisors(List advisors) {
		AspectJPrecedenceAwareOrderComparator comparator = new AspectJPrecedenceAwareOrderComparator();
		
		// build list for sorting
		List partiallyComparableAdvisors = new ArrayList();
		for (Iterator iter = advisors.iterator(); iter.hasNext();) {
			Advisor element = (Advisor) iter.next();
			PartiallyComparableAdvisor advisor = new PartiallyComparableAdvisor(element,comparator);
			partiallyComparableAdvisors.add(advisor);
		}		
		
		// sort it
		List sorted =  PartialOrder.sort(partiallyComparableAdvisors);
		if (sorted == null) {
			// TODO: work much harder to give a better error message here.
			throw new IllegalArgumentException("Advice precedence circularity error");
		}
		
		// extract results again
		List result = new LinkedList();
		for (Iterator iter = sorted.iterator(); iter.hasNext();) {
			PartiallyComparableAdvisor pcAdvisor = (PartiallyComparableAdvisor) iter.next();
			result.add(pcAdvisor.getAdvisor());
		}
		
		return result;
	}

	private boolean excludeExposeInvocationInterceptorFromSorting(List advisors) {
		if (advisors.get(0) == ExposeInvocationInterceptor.ADVISOR) {
			advisors.remove(0);
			return true;
		}
		return false;
	}


	/**
	 * Implements AspectJ PartialComparable interface for defining partial orderings.
	 */
	private static class PartiallyComparableAdvisor implements PartialComparable {

		private final Advisor advisor;

		private final Comparator comparator;
		
		public PartiallyComparableAdvisor(Advisor advisor, AspectJPrecedenceAwareOrderComparator comparator) {
			this.advisor = advisor;
			this.comparator = comparator;
		}
				
		public int compareTo(Object obj) {
			Advisor otherAdvisor = ((PartiallyComparableAdvisor) obj).advisor;
			return this.comparator.compare(this.advisor, otherAdvisor);
		}

		public int fallbackCompareTo(Object obj) {
			return 0;
		}

		public Advisor getAdvisor() {
			return this.advisor;
		}
		
		// To assist in PD...
		public String toString() {
			StringBuffer sb = new StringBuffer();
			Advice advice = advisor.getAdvice();
			sb.append(advice.getClass().getSimpleName());
			sb.append(":");
			if (advisor instanceof Ordered) {
				sb.append("order " + ((Ordered)advisor).getOrder() + ", ");
			}
			if (advice instanceof AbstractAspectJAdvice) {
				AbstractAspectJAdvice ajAdvice = (AbstractAspectJAdvice) advice;
				sb.append(ajAdvice.getAspectName());
				sb.append(", declaration order= ");
				sb.append(ajAdvice.getOrder());
			}
			return sb.toString();
		}
	}

}
