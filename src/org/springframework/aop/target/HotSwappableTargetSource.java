/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.util.Assert;

/**
 * Implementation of TargetSource interface that caches a local target object,
 * but allows the target to be swapped while the application is running.
 *
 * <p>If configuring an object of this class in a Spring IoC container,
 * use constructor injection.
 * 
 * <p>This TargetSource is serializable if the target is at the time
 * of serialization.
 *
 * @author Rod Johnson
 */
public class HotSwappableTargetSource implements TargetSource, Serializable {

	/** Target cached and invoked using reflection */
	private Object target;


	/**
	 * Create a new HotSwappableTargetSource with the given initial target object.
	 * @param initialTarget the initial target object
	 */
	public HotSwappableTargetSource(Object initialTarget) {
		Assert.notNull(initialTarget, "initialTarget is required");
		this.target = initialTarget;
	}


	public Class getTargetClass() {
		return this.target.getClass();
	}

	/**
	 * Not static.
	 */
	public final boolean isStatic() {
		return false;
	}

	public synchronized Object getTarget() {
		// Synchronization around something that takes so little time is fine.
		return this.target;
	}

	/**
	 * No need to release target.
	 */
	public void releaseTarget(Object target) {
	}


	/**
	 * Swap the target, returning the old target.
	 * @param newTarget the new target object
	 * @return the old target object
	 * @throws IllegalArgumentException if the new target is invalid
	 */
	public synchronized Object swap(Object newTarget) throws IllegalArgumentException {
		Assert.notNull(newTarget, "New target must not be null");
		// TODO type checks
		Object old = this.target;
		this.target = newTarget;
		return old;
	}


	/**
	 * Two HotSwappableTargetSources are equal if the targets are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof HotSwappableTargetSource)) {
			return false;
		}
		HotSwappableTargetSource otherTargetSource = (HotSwappableTargetSource) other;
		return this.target.equals(otherTargetSource.target);
	}

	public int hashCode() {
		return this.target.hashCode();
	}

	public String toString() {
		return "HotSwappableTargetSource for target: " + this.target;
	}

}
