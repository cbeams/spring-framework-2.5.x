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
 * @version $Id: SingletonTargetSource.java,v 1.2 2003-11-30 18:10:53 johnsonr Exp $
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
	 * Two invoker interceptors are equal if they have the same target or if the targets
	 * are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof SingletonTargetSource))
			return false;
		SingletonTargetSource otherII = (SingletonTargetSource) other;
		return otherII.target == this.target || otherII.target.equals(this.target);
	}

	/**
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public boolean isStatic() {
		return true;
	}

}
