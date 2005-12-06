/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.scheduling.commonj;

import commonj.work.Work;

import org.springframework.util.Assert;

/**
 * Simple Work adapter that delegates to a given Runnable.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see commonj.work.Work
 * @see java.lang.Runnable
 */
public class DelegatingWork implements Work {

	private final Runnable runnable;

	private boolean daemon = false;


	/**
	 * Create a new DelegatingWork, assuming non-daemon status.
	 * @param work the Runnable implementation to delegate to
	 */
	public DelegatingWork(Runnable work) {
		Assert.notNull(work, "Runnable is required");
		this.runnable = work;
	}

	/**
	 * Create a new DelegatingWork.
	 * @param work the Runnable implementation to delegate to
	 * @param daemon whether the submitted work is long-lived
	 * (<code>true</code>) versus short-lived (<code>false</code>).
	 * In the former case, the work will not allocate a thread from
	 * the thread pool but rather be considered as long-running
	 * background thread.
	 */
	public DelegatingWork(Runnable work, boolean daemon) {
		this.runnable = work;
		this.daemon = daemon;
	}


	/**
	 * Delegates execution to the underlying Runnable.
	 */
	public void run() {
		this.runnable.run();
	}

	/**
	 * Returns the daemon setting as specified in the constructor.
	 */
	public boolean isDaemon() {
		return daemon;
	}

	/**
	 * This implementation is empty, as we cannot make a plain
	 * Runnable implementation stop.
	 */
	public void release() {
	}

}
