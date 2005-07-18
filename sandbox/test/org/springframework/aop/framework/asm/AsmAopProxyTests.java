package org.springframework.aop.framework.asm;

import org.springframework.aop.framework.asm.AsmAopProxy;
import org.springframework.aop.framework.AbstractAopProxyTests;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.TestBean;
import org.springframework.beans.ITestBean;

import java.lang.reflect.Method;

/**
 * @author robh
 */
public class AsmAopProxyTests extends AbstractAopProxyTests {
	protected Object createProxy(AdvisedSupport as) {
		return createAopProxy(as).getProxy();
	}

	protected AopProxy createAopProxy(AdvisedSupport as) {
		return new AsmAopProxy(as);
	}

	protected boolean requiresTarget() {
		return true;
	}
}
