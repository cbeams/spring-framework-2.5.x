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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;

/**
 * JavaBean that allows for configuring a JDK 1.5 ThreadPoolExecutor in bean style
 * (through "corePoolSize", "maxPoolSize", "keepAliveSeconds", "queueCapacity" properties),
 * exposing it as both Spring TaskExecutor and JDK 1.5 Executor. This is an alternative
 * to configuring a ThreadPoolExecutor instance directly using constructor injection,
 * with a separate ConcurrentTaskExecutor adapter if necessary.
 *
 * <p>For any custom needs, in particular for defining a ScheduledThreadPoolExecutor,
 * it is recommended to use a straight definition of the JDK 1.5 Executor implementation
 * instance or a factory method definition that points to the JDK 1.5 Executors class.
 * To expose it as a Spring TaskExecutor, wrap it with a ConcurrentTaskExecutor adapter.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.core.task.TaskExecutor
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see ConcurrentTaskExecutor
 */
public class ThreadPoolTaskExecutor implements SchedulingTaskExecutor, Executor, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private int corePoolSize = 1;

	private int maxPoolSize = Integer.MAX_VALUE;

	private int keepAliveSeconds = 60;

	private int queueCapacity = Integer.MAX_VALUE;

	private ThreadFactory threadFactory = Executors.defaultThreadFactory();

	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

	private ThreadPoolExecutor executorService;


	/**
	 * Set the ThreadPoolExecutor's core pool size.
	 * Default is 1.
	 */
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	/**
	 * Set the ThreadPoolExecutor's maximum pool size.
	 * Default is <code>Integer.MAX_VALUE</code>.
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * Set the ThreadPoolExecutor's keep alive seconds.
	 * Default is 60.
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	/**
	 * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
	 * Default is <code>Integer.MAX_VALUE</code>.
	 * <p>Any positive value will lead to a LinkedBlockingQueue instance;
	 * any other value will lead to a SynchronousQueue instance.
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	/**
	 * Set the ThreadFactory to use for the ThreadPoolExecutor's thread pool.
	 * Default is the ThreadPoolExecutor's default thread factory.
	 * @see java.util.concurrent.Executors#defaultThreadFactory()
	 */
	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = (threadFactory != null ? threadFactory : Executors.defaultThreadFactory());
	}

	/**
	 * Set the RejectedExecutionHandler to use for the ThreadPoolExecutor.
	 * Default is the ThreadPoolExecutor's default abort policy.
	 * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
	 */
	public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler =
				(rejectedExecutionHandler != null ? rejectedExecutionHandler : new ThreadPoolExecutor.AbortPolicy());
	}


	/**
	 * Calls <code>initialize()</code> after the container applied all property values.
	 * @see #initialize()
	 */
	public void afterPropertiesSet() {
		initialize();
	}

	/**
	 * Creates the BlockingQueue and the ThreadPoolExecutor.
	 * @see #createQueue
	 */
	public void initialize() {
		logger.info("Creating ThreadPoolExecutor");
		BlockingQueue queue = createQueue(this.queueCapacity);
		this.executorService = new ThreadPoolExecutor(
				this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
				queue, this.threadFactory, this.rejectedExecutionHandler);
	}

	/**
	 * Create the BlockingQueue to use for the ThreadPoolExecutor.
	 * <p>A LinkedBlockingQueue instance will be created for a positive
	 * capacity value; a SynchronousQueue else.
	 * @param queueCapacity the specified queue capacity
	 * @return the BlockingQueue instance
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	protected BlockingQueue createQueue(int queueCapacity) {
		if (this.queueCapacity > 0) {
			return new LinkedBlockingQueue(this.queueCapacity);
		}
		else {
			return new SynchronousQueue();
		}
	}


	/**
	 * Implementation of both the JDK 1.5 Executor interface
	 * and the Spring TaskExecutor interface, delegating to
	 * the ThreadPoolExecutor instance.
	 * @see java.util.concurrent.Executor#execute(Runnable)
	 * @see org.springframework.core.task.TaskExecutor#execute(Runnable)
	 */
	public void execute(Runnable task) {
		Assert.notNull(this.executorService, "ThreadPoolTaskExecutor not initialized");
		this.executorService.execute(task);
	}

	/**
	 * This task executor prefers short-lived work units.
	 */
	public boolean prefersShortLivedTasks() {
		return true;
	}


	/**
	 * Calls <code>shutdown</code> when the BeanFactory destroys
	 * the task executor instance.
	 * @see #shutdown()
	 */
	public void destroy() {
		shutdown();
	}

	/**
	 * Perform a shutdown on the ThreadPoolExecutor.
	 * @see java.util.concurrent.ThreadPoolExecutor#shutdown()
	 */
	public void shutdown() {
		logger.info("Shutting down ThreadPoolExecutor");
		this.executorService.shutdown();
	}

}
