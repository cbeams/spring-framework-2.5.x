/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.target;

import org.springframework.aop.TargetSource;


/**
 * Implementation of Interceptor interface that 
 * invokes a local target object using reflection.
 * This is a simple JavaBean that caches a local object.
 * This should always be the last interceptor in the chain.
 * It does not invoke proceed() on the MethodInvocation.
 * This class is final as it has a special purpose to the AOP
 * framework and cannot be modified.
 * <br>Note that this class used to extend AbstractReflectionInvokerInterceptor
 * but at the price of a little code duplication making it implement invoke()
 * itself simplifies stack traces and produces a slight performance improvement.
 * @author Rod Johnson
 * @version $Id: SingletonTargetSource.java,v 1.3 2003-12-02 11:52:57 johnsonr Exp $
 */
public final class SingletonTargetSource implements TargetSource {

	/** Target cached and invoked using reflection */	
	private Object target;
	
	public SingletonTargetSource() {
	}
	
	public SingletonTargetSource(Object target) {
		this.target = target;
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public Class getTargetClass() {
		return target.getClass();
	}
	
	public Object getTarget() {
		return this.target;
	}
	
	public void releaseTarget(Object o) {
	}
	
	public String toString() {
		return "Singleton target source (not dynamic): target=[" + target + "]";
	}
	

	/**
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public boolean isStatic() {
		return true;
	}

	/**
	 * Two invoker interceptors are equal if they have the same target or if the targets
	 * or the targets are equal.
	 */
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof SingletonTargetSource))
			return false;
		SingletonTargetSource b = (SingletonTargetSource) other;
		if (this.target == null)
			return b.target == null;
		return this.target.equals(b.target);
	}
}
