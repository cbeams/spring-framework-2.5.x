
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.Advised;

/**
 * @author robh
 */
public class DefaultCodeGenerationStrategySelector implements CodeGenerationStrategySelector {

	public CodeGenerationStrategy select(AdvisedSupport advised, Method method, Class targetClass) {
		if(Advised.class == method.getDeclaringClass()) {
			return new AdvisedMixinCodeGenerationStrategy();
		}

		// need the advice chain to do perform anymore selections
		List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(advised, null, method, targetClass);

		if (chain.isEmpty()) {
			if (advised.getTargetSource().isStatic()) {
				return new StraightToTargetCodeGenerationStrategy();
			}
			else {
				return new NonStaticTargetSourceCodeGenerationStrategy();
			}
		}
		else {
			return new AdvisedCodeGenerationStrategy();
		}
	}
}
