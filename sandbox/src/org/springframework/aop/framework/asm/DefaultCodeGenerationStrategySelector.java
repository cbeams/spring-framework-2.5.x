/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author Rob Harrop
 */
public class DefaultCodeGenerationStrategySelector implements CodeGenerationStrategySelector {

	private static final Log logger = LogFactory.getLog(DefaultCodeGenerationStrategySelector.class);

	public CodeGenerationStrategy select(AdvisedSupport advised, Method method, Class targetClass) {
		if (Advised.class == method.getDeclaringClass()) {
			return new AdvisedMixinCodeGenerationStrategy();
		}

		// need the advice chain to do perform anymore selections
		List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(advised, method, targetClass);

		CodeGenerationStrategy strategy = null;

		if(isHashCodeMethod(method)) {
			strategy = new HashCodeCodeGenerationStrategy();
		}
		
		// TODO: consider adding explicit expose proxy support to certain strategies
		// TODO: consider factoring out certain calls such as release for the target source
		if (chain.isEmpty() && (!advised.isExposeProxy())) {
			if (advised.getTargetSource().isStatic()) {
				strategy = new StraightToTargetCodeGenerationStrategy();
			}
			else {
				strategy = new NonStaticTargetSourceCodeGenerationStrategy();
			}
			// TODO: add explicit support for empty target source
		}
		else {
			// TODO: add explicit support for static target sources
			// TODO: add explicit support for an empty target source
			// TODO: add agressive inlining for before/after advice
			strategy = new AdvisedCodeGenerationStrategy();
		}

		if(logger.isInfoEnabled()) {
			logger.info("Selected strategy [" + strategy.getClass().getName() + "] for method [" + method + "].");
		}

		return strategy;
	}

	private boolean isHashCodeMethod(Method method) {
		return ((method.getDeclaringClass() == Object.class) &&
		        (method.getParameterTypes().length == 0) &&
		         "hashCode".equals(method.getName()));
	}
}
