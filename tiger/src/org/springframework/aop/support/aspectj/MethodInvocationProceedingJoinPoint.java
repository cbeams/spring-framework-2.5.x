package org.springframework.aop.support.aspectj;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

/**
 * Implementation of AspectJ ProceedingJoinPoint interface
 * wrapping an AOP Alliance MethodInvocation.
 * 
 * @author Rod Johnson
 * @since 1.3
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint {
	
	private MethodInvocation methodInvocation;
	
	public MethodInvocationProceedingJoinPoint(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
	}

	public void set$AroundClosure(AroundClosure aroundClosure) {
		throw new UnsupportedOperationException();
	}

	public Object proceed() throws Throwable {
		return methodInvocation.proceed();
	}

	public Object proceed(Object[] args) throws Throwable {
		Object[] oldArgs = methodInvocation.getArguments();
		for (int i = 0; i < oldArgs.length; i++) {
			oldArgs[i] = args[i];
		}
		return methodInvocation.proceed();
	}

	public String toShortString() {
		return "execution of " + methodInvocation.getMethod().getName();
	}

	public String toLongString() {
		return "execution of " + methodInvocation.getMethod().getName();
	}

	public Object getThis() {
		throw new UnsupportedOperationException("Cannot support caller semantics");
	}

	public Object getTarget() {
		return methodInvocation.getThis();
	}

	public Object[] getArgs() {
		return methodInvocation.getArguments();
	}

	public Signature getSignature() {
		throw new UnsupportedOperationException();
	}

	public SourceLocation getSourceLocation() {
		throw new UnsupportedOperationException();
	}

	public String getKind() {
		return ProceedingJoinPoint.METHOD_EXECUTION;
	}

	public StaticPart getStaticPart() {
		throw new UnsupportedOperationException();
	}

}
