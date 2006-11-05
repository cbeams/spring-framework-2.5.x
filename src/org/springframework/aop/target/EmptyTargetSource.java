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

import org.springframework.aop.TargetSource;

import java.io.Serializable;

/**
 * Canonical <code>TargetSource</code> when there is no target,
 * and behavior is supplied by interfaces and advisors.
 *
 * @author Rod Johnson
 */
public class EmptyTargetSource implements TargetSource, Serializable {

	/**
	 * The canonical (Singleton) instance of this {@link EmptyTargetSource}.
	 */
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource();


	/**
	 * Create a new instance of the {@link EmptyTargetSource} class.
	 * <p>This constructor is <code>private</code> to enforce the
	 * Singleton pattern.
	 */
	private EmptyTargetSource() {
	}


	/**
	 * Always returns <code>null</code>.
	 * @return <code>null</code>
	 */
	public Class getTargetClass() {
		return null;
	}

	/**
	 * Always returns <code>true</code>.
	 * @return <code>true</code>
	 */
	public boolean isStatic() {
		return true;
	}

	/**
	 * Always returns <code>null</code>.
	 * @return <code>null</code>
	 */
	public Object getTarget() {
		return null;
	}

	public void releaseTarget(Object target) {
	}


	public String toString() {
		return "EmptyTargetSource: no target";
	}


	/**
	 * Returns the canonical instance on deserialization, thus
	 * protecting the Singleton pattern.
	 */
	private Object readResolve() {
		return INSTANCE;
	}

}
