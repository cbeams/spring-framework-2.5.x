/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version $Id: HotSwappableTargetSource.java,v 1.5 2004-03-18 02:46:13 trisberg Exp $
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
