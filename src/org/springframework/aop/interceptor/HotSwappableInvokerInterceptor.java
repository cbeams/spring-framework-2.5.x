/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.interceptor;

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
 * @version $Id: HotSwappableInvokerInterceptor.java,v 1.2 2003-11-28 16:39:36 jhoeller Exp $
 */
public class HotSwappableInvokerInterceptor extends AbstractReflectionInvokerInterceptor {

	/** Target cached and invoked using reflection */
	private Object target;

	/**
	 * JavaBean constructor.
	 * Set the initialTarget property before use.
	 */
	public HotSwappableInvokerInterceptor() {
	}

	public HotSwappableInvokerInterceptor(Object initialTarget) {
		this.target = initialTarget;
	}

	/**
	 * Set the initial target. Construction time only.
	 */
	public void setInitialTarget(Object target) {
		this.target = target;
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

	/**
	 * Two invoker interceptors are equal if they have the same target or if the targets
	 * are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof HotSwappableInvokerInterceptor))
			return false;
		HotSwappableInvokerInterceptor otherII = (HotSwappableInvokerInterceptor) other;
		return otherII.target == this.target || otherII.target.equals(this.target);
	}

}
