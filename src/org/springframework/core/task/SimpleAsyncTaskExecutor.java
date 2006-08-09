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

package org.springframework.core.task;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrencyThrottleSupport;

/**
 * TaskExecutor implementation that fires up a new Thread for each task,
 * executing it asynchronously.
 *
 * <p>Supports limiting concurrent threads through the "concurrencyLimit"
 * bean property. By default, the number of concurrent threads is unlimited.
 *
 * <p><b>NOTE:</b> Does not reuse threads! Consider a thread-pooling TaskExecutor
 * implementation instead, in particular for executing a large number of
 * short-lived tasks.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setConcurrencyLimit
 * @see SyncTaskExecutor
 * @see org.springframework.scheduling.timer.TimerTaskExecutor
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
 */
public class SimpleAsyncTaskExecutor extends ConcurrencyThrottleSupport
		implements TaskExecutor, Serializable {

	/**
	 * Default thread name prefix: "SimpleAsyncTaskExecutor-".
	 */
	public static final String DEFAULT_THREAD_NAME_PREFIX =
			ClassUtils.getShortName(SimpleAsyncTaskExecutor.class) + "-";


	private final Object monitor = new Object();

	private String threadNamePrefix = DEFAULT_THREAD_NAME_PREFIX;

	private int threadPriority = Thread.NORM_PRIORITY;

	private boolean daemon = false;

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
	 * Set the priority of the threads that this executor creates.
	 * Default is 5.
	 * @see java.lang.Thread#NORM_PRIORITY
	 */
	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	/**
	 * Return the priority of the threads that this executor creates.
	 */
	protected int getThreadPriority() {
		return threadPriority;
	}

	/**
	 * Set whether this executor should create daemon threads,
	 * just executing as long as the application itself is running.
	 * <p>Default is "false": Tasks passed to this executor should be either
	 * short-lived or support explicit cancelling. Hence, if the application
	 * shuts down, tasks will by default finish their execution. Specify
	 * "true" for eager shutdown of threads that execute tasks.
	 * @see java.lang.Thread#setDaemon
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * Return whether this executor should create daemon threads.
	 */
	protected boolean isDaemon() {
		return daemon;
	}


	/**
	 * Executes the given task, within a concurrency throttle
	 * if configured (through the superclass's settings).
	 * @throws NullPointerException if the supplied task is <code>null</code>
	 * @see #beforeAccess()
	 * @see #doExecute(Runnable)
	 * @see #afterAccess()
	 */
	public final void execute(Runnable task) {
		Assert.notNull("Runnable must not be null");
		beforeAccess();
		doExecute(new ConcurrencyThrottlingRunnable(task));
	}

	/**
	 * Template method for the actual execution of a task.
	 * <p>Default implementation creates a new Thread and starts it.
	 * @param task the Runnable to execute
	 * @see #createThread
	 * @see java.lang.Thread#start()
	 */
	protected void doExecute(Runnable task) {
		createThread(task).start();
	}

	/**
	 * Template method for creation of a Thread.
	 * <p>Default implementation creates a new Thread for the given
	 * Runnable, applying an appropriate thread name.
	 * @param task the Runnable to execute
	 * @see #nextThreadName()
	 */
	protected Thread createThread(Runnable task) {
		Thread thread = new Thread(task, nextThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(isDaemon());
		return thread;
	}

	/**
	 * Return the thread name to use for a newly created thread.
	 * <p>Default implementation returns the specified thread name prefix
	 * with an increasing thread count appended: for example,
	 * "SimpleAsyncTaskExecutor-0".
	 * @see #getThreadNamePrefix()
	 */
	protected String nextThreadName() {
		int threadNumber = 0;
		synchronized (this.monitor) {
			this.threadCount++;
			threadNumber = this.threadCount;
		}
		return getThreadNamePrefix() + threadNumber;
	}


	/**
	 * This Runnable calls <code>afterAccess()</code> after the
	 * target Runnable has finished its execution.
	 */
	private class ConcurrencyThrottlingRunnable implements Runnable {

		private final Runnable target;

		public ConcurrencyThrottlingRunnable(Runnable target) {
			this.target = target;
		}

		public void run() {
			try {
				this.target.run();
			}
			finally {
				afterAccess();
			}
		}
	}

}
