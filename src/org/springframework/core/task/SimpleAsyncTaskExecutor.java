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

package org.springframework.core.task;

import java.io.Serializable;

import org.springframework.util.ConcurrencyThrottleSupport;

/**
 * TaskExecutor implementation that fires up a new Thread for each task,
 * executing it asynchronously.
 *
 * <p>Supports limiting concurrent threads through the "concurrencyLimit"
 * bean property. By default, only 1 concurrent thread is allowed.
 *
 * <p>Does not reuse threads: Consider a thread-pooling TaskExecutor
 * implementation instead.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setConcurrencyLimit
 * @see SyncTaskExecutor
 * @see org.springframework.scheduling.timer.TimerTaskExecutor
 */
public class SimpleAsyncTaskExecutor extends ConcurrencyThrottleSupport
		implements TaskExecutor, Serializable {

	/**
	 * Default thread name prefix: "SimpleAsyncTaskExecutor-".
	 */
	public static final String DEFAULT_THREAD_NAME_PREFIX = "SimpleAsyncTaskExecutor-";


	private String threadNamePrefix = DEFAULT_THREAD_NAME_PREFIX;

	private int threadCount = 0;


	/**
	 * Create a new SimpleAsyncTaskExecutor with default thread name prefix.
	 */
	public SimpleAsyncTaskExecutor() {
	}

	/**
	 * Create a new SimpleAsyncTaskExecutor with the given thread name prefix.
	 * @param threadNamePrefix the prefix to use for the names of newly created threads
	 */
	public SimpleAsyncTaskExecutor(String threadNamePrefix) {
		setThreadNamePrefix(threadNamePrefix);
	}


	/**
	 * Specify the prefix to use for the names of newly created threads.
	 * Default is "SimpleAsyncTaskExecutor-".
	 */
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : DEFAULT_THREAD_NAME_PREFIX);
	}

	/**
	 * Return the thread name prefix to use for the names of newly
	 * created threads.
	 */
	protected String getThreadNamePrefix() {
		return threadNamePrefix;
	}


	/**
	 * Executes the given task, within a concurrency throttle
	 * if configured (through the superclass's settings).
	 * @see #beforeAccess()
	 * @see #doExecute(Runnable)
	 * @see #afterAccess()
	 */
	public final void execute(Runnable task) {
		beforeAccess();
		try {
			doExecute(task);
		}
		finally {
			afterAccess();
		}
	}

	/**
	 * Template method for the actual execution of a task.
	 * <p>Default implementation creates a new Thread and starts it.
	 * @param task the Runnable to execute
	 */
	protected void doExecute(Runnable task) {
		new Thread(task, getThreadName()).start();
	}

	/**
	 * Return the thread name to use for a newly created thread.
	 * <p>Default implementation returns the specified thread name prefix
	 * with an increasing thread count appended: for example,
	 * "SimpleAsyncTaskExecutor-0".
	 * @see #getThreadNamePrefix()
	 */
	protected synchronized String getThreadName() {
		return (getThreadNamePrefix() + (this.threadCount++));
	}

}
