
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public interface CodeGenerationStrategySelector {

	CodeGenerationStrategy select(AdvisedSupport advised, Method method, Class targetClass);
}
