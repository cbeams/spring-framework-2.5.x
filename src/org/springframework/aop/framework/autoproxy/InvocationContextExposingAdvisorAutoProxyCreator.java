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

import org.springframework.aop.aspectj.ExposeJoinPointInterceptor;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.util.List;

/**
 * Extension of {@link DefaultAdvisorAutoProxyCreator} that adds an {@link ExposeInvocationInterceptor} and
 * a {@link ExposeJoinPointInterceptor} to the beginning of the advice chain. These additional advices are
 * needed when using AspectJ expression pointcuts and when using AspectJ-style advice.
 * 
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0
 */
public class InvocationContextExposingAdvisorAutoProxyCreator extends DefaultAdvisorAutoProxyCreator {

	protected void extendCandidateAdvisors(List candidateAdvisors) {
		if (!candidateAdvisors.isEmpty()) {
			candidateAdvisors.add(0, new DefaultPointcutAdvisor(ExposeInvocationInterceptor.INSTANCE));
			candidateAdvisors.add(1, new DefaultPointcutAdvisor(ExposeJoinPointInterceptor.INSTANCE));
		}
	}
}
