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

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor that throttles concurrent access, blocking invocations
 * if a specified concurrency limit is reached.
 *
 * <p>Can be applied to methods of local services that involve heavy use
 * of system resources, in a scenario where it is more efficient to
 * throttle concurrency for a specific service rather than restricting
 * the entire thread pool (e.g. the web container's thread pool).
 *
 * @author Juergen Hoeller
 * @since 11.02.2004
 */
public class ConcurrencyThrottleInterceptor implements MethodInterceptor, Serializable {

	/**
	 * Static to avoid serializing the logger
	 */
	protected static final Log logger = LogFactory.getLog(ConcurrencyThrottleInterceptor.class);

	private int concurrencyLimit = 1;

	private int concurrencyCount = 0;

	/**
	 * Set the maximum number of parallel invocations that this interceptor
	 * allows. Default is 1 (having the same effect as a synchronized block).
	 */
	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyLimit = concurrencyLimit;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		boolean debug = logger.isDebugEnabled();
		synchronized (this) {
			while (this.concurrencyCount >= this.concurrencyLimit) {
				if (debug) {
					logger.debug("Concurrency count " + this.concurrencyCount +
											 " has reached limit " + this.concurrencyLimit + " - blocking");
				}
				try {
					wait();
				}
				catch (InterruptedException ex) {
				}
			}
			if (debug) {
				logger.debug("Entering method at concurrency count " + this.concurrencyCount);
			}
			this.concurrencyCount++;
		}
		try {
			return methodInvocation.proceed();
		}
		finally {
			synchronized (this) {
				this.concurrencyCount--;
				if (debug) {
					logger.debug("Returning from method at concurrency count " + this.concurrencyCount);
				}
				notify();
			}
		}
	}

}
