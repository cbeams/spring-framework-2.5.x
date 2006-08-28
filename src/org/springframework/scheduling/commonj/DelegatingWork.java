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

package org.springframework.scheduling.commonj;

import commonj.work.Work;

import org.springframework.scheduling.SchedulingAwareRunnable;
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


	/**
	 * Create a new DelegatingWork.
	 * @param runnable the Runnable implementation to delegate to
	 * (may be a SchedulingAwareRunnable for extended support)
	 * @see org.springframework.scheduling.SchedulingAwareRunnable
	 * @see #isDaemon()
	 */
	public DelegatingWork(Runnable runnable) {
		Assert.notNull(runnable, "Runnable must not be null");
		this.runnable = runnable;
	}


	/**
	 * Delegates execution to the underlying Runnable.
	 */
	public void run() {
		this.runnable.run();
	}

	/**
	 * This implementation delegates to <code>SchedulingAwareRunnable.isLongLived()</code>,
	 * if available.
	 * @see org.springframework.scheduling.SchedulingAwareRunnable#isLongLived()
	 */
	public boolean isDaemon() {
		if (this.runnable instanceof SchedulingAwareRunnable) {
			return ((SchedulingAwareRunnable) this.runnable).isLongLived();
		}
		return false;
	}

	/**
	 * This implementation is empty, since we expect the Runnable
	 * to terminate based on some specific shutdown signal.
	 */
	public void release() {
	}

}
