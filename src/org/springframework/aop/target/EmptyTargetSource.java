/*
 * Copyright 2002-2007 the original author or authors.
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
 * Canonical <code>TargetSource</code> when there is no target
 * (or just the target class known), and behavior is supplied
 * by interfaces and advisors only.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class EmptyTargetSource implements TargetSource, Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = 3680494563553489691L;


	//---------------------------------------------------------------------
	// Static factory methods
	//---------------------------------------------------------------------

	/**
	 * The canonical (Singleton) instance of this {@link EmptyTargetSource}.
	 */
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null);


	/**
	 * Return an EmptyTargetSource for the given target Class.
	 * @param targetClass the target Class (may be <code>null</code>)
	 * @see #getTargetClass()
	 */
	public static EmptyTargetSource forClass(Class targetClass) {
		return (targetClass == null ? INSTANCE : new EmptyTargetSource(targetClass));
	}


	//---------------------------------------------------------------------
	// Instance implementation
	//---------------------------------------------------------------------

	private final Class targetClass;


	/**
	 * Create a new instance of the {@link EmptyTargetSource} class.
	 * <p>This constructor is <code>private</code> to enforce the
	 * Singleton pattern / factory method pattern.
	 * @param targetClass the target class to expose (may be <code>null</code>)
	 */
	private EmptyTargetSource(Class targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * Always returns the specified target Class, or <code>null</code> if none.
	 */
	public Class getTargetClass() {
		return this.targetClass;
	}

	/**
	 * Always returns <code>true</code>.
	 */
	public boolean isStatic() {
		return true;
	}

	/**
	 * Always returns <code>null</code>.
	 */
	public Object getTarget() {
		return null;
	}

	/**
	 * Nothing to release.
	 */
	public void releaseTarget(Object target) {
	}


	public String toString() {
		return "EmptyTargetSource: " +
				(this.targetClass != null ? "target class [" + this.targetClass + "]" : "no target");
	}

	/**
	 * Returns the canonical instance on deserialization in case
	 * of no target class, thus protecting the Singleton pattern.
	 */
	private Object readResolve() {
		return (this.targetClass == null ? INSTANCE : this);
	}

}
