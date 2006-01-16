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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;

/**
 * <code>TargetSource</code> that will lazy initialize a user-managed object.
 *
 * <p>Creation of the lazy target object is controlled by the user by implementing the
 * {@link #createObject()} method. This <code>TargetSource</code> will invoke this
 * method the first time the proxy is accessed.
 *
 * <p>Useful when you need to pass a reference to some dependency to an object but you
 * don't actually want the dependency to be created until it is first used. A typical
 * scenario for this is a connection to a remote resource
 *
 * @author Rob Harrop
 * @since 1.2.4
 * @see #createObject()
 */
public abstract class AbstractLazyInitTargetSource implements TargetSource {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Stores the lazily initialized target */
	private Object lazyTarget;


	/**
	 * Default implementation returns <code>null</code> if the target is <code>null</code>
	 * (it is hasn't yet been initialized) or the target class if the target has already
	 * been initialized. Subclasses may wish to override this method to provide more
	 * meaningful values when the target is still <code>null</code>.
	 */
	public Class getTargetClass() {
		return (this.lazyTarget != null ? this.lazyTarget.getClass() : null);
	}

	/**
	 * Not static.
	 */
	public boolean isStatic() {
		return false;
	}

	/**
	 * Returns the lazy-initialized target object, creating it if it doesn't exist.
	 * @see #createObject()
	 */
	public Object getTarget() throws Exception {
		if(this.lazyTarget == null) {
			logger.debug("Initializing lazy target object.");
			synchronized(this) {
				this.lazyTarget = createObject();
			}
		}

		return this.lazyTarget;
	}

	/**
	 * No need to release target.
	 */
	public void releaseTarget(Object target) throws Exception {
	}


	/**
	 * Subclasses should implement this method to return the lazy initialized object.
	 * Called the first time the proxy is invoked.
	 */
	protected abstract Object createObject() throws Exception;

}
