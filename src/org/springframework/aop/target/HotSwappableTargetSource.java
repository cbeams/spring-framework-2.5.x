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

import java.io.Serializable;

import org.springframework.aop.TargetSource;

/**
 * Implementation of TargetSource interface that caches a local target object,
 * but allows the target to be swapped while the application is running.
 *
 * <p>If configuring an object of this class in a Spring IoC container,
 * use constructor injection.
 * 
 * <p>This TargetSource is serializable if the target is
 * at the time of serialization.
 *
 * @author Rod Johnson
 */
public class HotSwappableTargetSource implements TargetSource, Serializable {

	/** Target cached and invoked using reflection */
	protected Object target;

	/**
	 * Create a new HotSwappableTargetSource with the initial target.
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
	public synchronized Object getTarget() {
		return this.target;
	}

	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
	 */
	public void releaseTarget(Object o) {
		// No implementation needed
	}

	/**
	 * Swap the target, returning the old target.
	 * @param newTarget new target
	 * @return the old target
	 * @throws IllegalArgumentException if the new target is invalid
	 */
	public synchronized Object swap(Object newTarget) throws IllegalArgumentException {
		if (newTarget == null) {
			throw new IllegalArgumentException("Cannot swap to null");
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
		HotSwappableTargetSource otherTargetSource = (HotSwappableTargetSource) other;
		return otherTargetSource.target == this.target || otherTargetSource.target.equals(this.target);
	}
	
	public String toString() {
		return "Swappable TargetSource (" + getClass().getName() + "): " +
		 	((target != null) ? "targetClass=" + target.getClass() : "UNITIALIZED");
	}

}
