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
import org.springframework.util.ObjectUtils;

/**
 * Implementation of the TargetSource interface that holds a local object.
 * This is the default implementation of TargetSource used by the AOP framework.
 * There is no need to create objects of this class in application code.
 * <br>This class is Serializable. However, the serializability of a SingletonTargetSource
 * will depend on whether the target is Serializable.
 * @author Rod Johnson
 */
public final class SingletonTargetSource implements TargetSource, Serializable {

	/** Target cached and invoked using reflection */	
	private final Object target;
	
	public SingletonTargetSource(Object target) {
		this.target = target;
	}
	
	public Class getTargetClass() {
		return target.getClass();
	}
	
	public Object getTarget() {
		return this.target;
	}
	
	public void releaseTarget(Object o) {
		// Nothing to do
	}

	public boolean isStatic() {
		return true;
	}

	/**
	 * Two invoker interceptors are equal if they have the same target or if the
	 * targets or the targets are equal.
	 */
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SingletonTargetSource)) {
			return false;
		}
		SingletonTargetSource otherTargetSource = (SingletonTargetSource) other;
		return ObjectUtils.nullSafeEquals(this.target, otherTargetSource.target);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "SingletonTargetSource: target=(" + target + ")";
	}
}
