
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;

/**
 * 
 * @author robh
 */
public interface ExpressionPointcut extends Pointcut {
	String getExpression();
}
