/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.target;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;

/**
 * Implementation of Interceptor interface that invokes a local target object
 * using reflection. Unlike the simple InvokerInterceptor, this allows the
 * target to be swapped while the application is running.
 *
 * <P>This should always be the last interceptor in the chain.
 * It does not invoke proceed() on the MethodInvocation.
 *
 * @author Rod Johnson
 * @version $Id: HotSwappableTargetSource.java,v 1.1 2003-11-30 17:17:34 johnsonr Exp $
 */
public class HotSwappableTargetSource implements TargetSource {

	/** Target cached and invoked using reflection */
	private Object target;

	/**
	 * JavaBean constructor.
	 * Set the initialTarget property before use.
	 */
	public HotSwappableTargetSource() {
	}

	public HotSwappableTargetSource(Object initialTarget) {
		this.target = initialTarget;
	}

	/**
	 * Set the initial target. Construction time only.
	 */
	public void setInitialTarget(Object target) {
		this.target = target;
	}
	
	public Class getTargetClass() {
		return target.getClass();
	}

	/**
	 * Swap the target, returning the old target
	 * @param newTarget new target
	 * @return the old target
	 * @throws AopConfigException if the new target is invalid
	 */
	public synchronized Object swap(Object newTarget) throws AopConfigException {
		if (newTarget == null) {
			throw new AopConfigException("Cannot swap to null");
		}
		// TODO type checks
		Object old = this.target;
		this.target = newTarget;
		return old;
	}

	/**
	 * Synchronization around something that takes so little time is fine
	 * @see org.springframework.aop.ProxyInterceptor#getTarget()
	 */
	public synchronized Object getTarget() {
		return this.target;
	}
	
	public void releaseTarget(Object o) {
		
	}

	/**
	 * Two invoker interceptors are equal if they have the same target or if the targets
	 * are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof HotSwappableTargetSource))
			return false;
		HotSwappableTargetSource otherII = (HotSwappableTargetSource) other;
		return otherII.target == this.target || otherII.target.equals(this.target);
	}

}
