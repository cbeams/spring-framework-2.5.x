/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.target;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;

/**
 * Implementation of TargetSource interface that caches a local
 * target object, but allows the target to be swapped while the application is running.
 * If configuring an object of this class in a Spring IoC container, 
 * use Type 3 (constructor-style) IoC.
 * @author Rod Johnson
 * @version $Id: HotSwappableTargetSource.java,v 1.4 2003-12-30 01:07:12 jhoeller Exp $
 */
public class HotSwappableTargetSource implements TargetSource {

	/** Target cached and invoked using reflection */
	private Object target;

	/**
	 * Create a new HotSwappableTargetSource with the initial target
	 * @param initialTarget initial target
	 */
	public HotSwappableTargetSource(Object initialTarget) {
		this.target = initialTarget;
	}
	
	public Class getTargetClass() {
		return target.getClass();
	}

	/**
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public final boolean isStatic() {
		return false;
	}

	/**
	 * Synchronization around something that takes so little time is fine
	 * @see org.springframework.aop.TargetSource#getTarget()
	 */
	public final synchronized Object getTarget() {
		return this.target;
	}

	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
	 */
	public void releaseTarget(Object o) {
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
	 * Two invoker interceptors are equal if they have the same target or
	 * if the targets are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof HotSwappableTargetSource)) {
			return false;
		}
		HotSwappableTargetSource otherII = (HotSwappableTargetSource) other;
		return otherII.target == this.target || otherII.target.equals(this.target);
	}

}
