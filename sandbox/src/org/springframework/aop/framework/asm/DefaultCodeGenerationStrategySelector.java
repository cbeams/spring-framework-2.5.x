
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.Advised;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author robh
 */
public class DefaultCodeGenerationStrategySelector implements CodeGenerationStrategySelector {

	private static final Log logger = LogFactory.getLog(DefaultCodeGenerationStrategySelector.class);

	public CodeGenerationStrategy select(AdvisedSupport advised, Method method, Class targetClass) {
		if(Advised.class == method.getDeclaringClass()) {
			return new AdvisedMixinCodeGenerationStrategy();
		}

		// need the advice chain to do perform anymore selections
		List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(advised, null, method, targetClass);

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
