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

package org.springframework.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support class for throttling concurrent access to a specific resource.
 *
 * <p>Designed for use as a base class, with the subclass invoking the
 * <code>beforeAccess</code> and <code>afterAccess</code> methods at
 * appropriate points of its workflow. Note that <code>afterAccess</code>
 * should usually be called in a finally block!.
 *
 * <p>This class is designed for serializablility, to allow for serializing
 * interceptors that derive from it. Note that the subclass needs to explicitly
 * implement the Serializable marker interface if it is actually serializable.
 *
 * @author Juergen Hoeller
 * @since 1.2.5
 * @see #setConcurrencyLimit
 * @see #beforeAccess()
 * @see #afterAccess()
 * @see org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor
 * @see java.io.Serializable
 */
public abstract class ConcurrencyThrottleSupport implements Serializable {

	/** Transient to optimize serialization */
	protected transient Log logger = LogFactory.getLog(getClass());

	/** Transient to optimize serialization */
	private transient Object monitor = new Object();

	private int concurrencyLimit = 1;

	private int concurrencyCount = 0;


	/**
	 * Set the maximum number of parallel accesses allowed.
	 * -1 indicates no concurrency limit at all. Default is 1.
	 * <p>This limit can in principle be changed at runtime,
	 * although it is generally designed as config time setting.
	 * NOTE: Do not switch between -1 and any concrete limit at runtime,
	 * as this will lead to inconsistent concurrency counts: A limit
	 * of -1 effectively turns off concurrency counting completely.
	 */
	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyLimit = concurrencyLimit;
	}

	/**
	 * Return the maximum number of parallel accesses allowed.
	 */
	public int getConcurrencyLimit() {
		return concurrencyLimit;
	}

	/**
	 * Return whether this throttle is currently active.
	 */
	public boolean isThrottleActive() {
		return (this.concurrencyLimit > 0);
	}


	protected void beforeAccess() {
		if (this.concurrencyLimit == 0) {
			throw new IllegalStateException("Currently no invocations allowed - concurrency limit set to 0");
		}
		if (this.concurrencyLimit > 0) {
			boolean debug = logger.isDebugEnabled();
			synchronized (this.monitor) {
				while (this.concurrencyCount >= this.concurrencyLimit) {
					if (debug) {
						logger.debug("Concurrency count " + this.concurrencyCount +
								" has reached limit " + this.concurrencyLimit + " - blocking");
					}
					try {
						this.monitor.wait();
					}
					catch (InterruptedException ex) {
						// Re-interrupt current thread, to allow other threads to react.
						Thread.currentThread().interrupt();
					}
				}
				if (debug) {
					logger.debug("Entering throttle at concurrency count " + this.concurrencyCount);
				}
				this.concurrencyCount++;
			}
		}
	}

	/**
	 * To be invoked after the main execution logic of concrete subclasses.
	 * @see #beforeAccess()
	 */
	protected void afterAccess() {
		if (this.concurrencyLimit >= 0) {
			synchronized (this.monitor) {
				this.concurrencyCount--;
				if (logger.isDebugEnabled()) {
					logger.debug("Returning from throttle at concurrency count " + this.concurrencyCount);
				}
				this.monitor.notify();
			}
		}
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException {
		// Rely on default serialization, just initialize state after deserialization.
		try {
			ois.defaultReadObject();
		}
		catch (ClassNotFoundException ex) {
			throw new IOException("Failed to deserialize [" + getClass().getName() + "]: " + ex.getMessage());
		}

		// Initialize transient fields.
		this.logger = LogFactory.getLog(getClass());
		this.monitor = new Object();
	}

}
