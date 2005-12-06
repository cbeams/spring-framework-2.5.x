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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean that sets up a JDK 1.5 ScheduledExecutorService
 * (by default: ScheduledThreadPoolExecutor as implementation)
 * and exposes it for bean references.
 *
 * <p>Allows for registration of ScheduledExecutorTasks, automatically starting
 * the ScheduledExecutorService on initialization and cancelling it on
 * destruction of the context. In scenarios that just require static
 * registration of tasks at startup, there is no need to access the
 * ScheduledExecutorService instance itself in application code.
 *
 * <p>Note that ScheduledExecutorService uses a Runnable instance that is
 * shared between repeated executions, in contrast to Quartz which
 * instantiates a new Job for each execution.
 *
 * <p>This is the direct analogon of the TimerFactoryBean class for
 * the JDK 1.3 Timer mechanism.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ScheduledExecutorTask
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see org.springframework.scheduling.timer.TimerFactoryBean
 */
public class ScheduledExecutorFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private ScheduledExecutorTask[] scheduledExecutorTasks;

	private int poolSize = 1;

	private ScheduledExecutorService executor;


	/**
	 * Register a list of ScheduledExecutorTask objects with the ScheduledExecutorService
	 * that this FactoryBean creates. Depending on each ScheduledExecutorTask's settings,
	 * it will be registered via one of ScheduledExecutorService's schedule methods.
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public void setScheduledExecutorTasks(ScheduledExecutorTask[] scheduledExecutorTasks) {
		this.scheduledExecutorTasks = scheduledExecutorTasks;
	}

	/**
	 * Set the ScheduledExecutorService's pool size.
	 * Default is 1.
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}


	public void afterPropertiesSet() {
		logger.info("Initializing SchedulerExecutorService");
		this.executor = createExecutor(this.poolSize);

		// register all ScheduledExecutorTasks
		for (int i = 0; i < this.scheduledExecutorTasks.length; i++) {
			ScheduledExecutorTask scheduledTask = this.scheduledExecutorTasks[i];
			if (scheduledTask.getPeriod() > 0) {
				// repeated task execution
				if (scheduledTask.isFixedRate()) {
					this.executor.scheduleAtFixedRate(
							scheduledTask.getRunnable(), scheduledTask.getDelay(), scheduledTask.getPeriod(),
							TimeUnit.MILLISECONDS);
				}
				else {
					this.executor.scheduleWithFixedDelay(
							scheduledTask.getRunnable(), scheduledTask.getDelay(), scheduledTask.getPeriod(),
							TimeUnit.MILLISECONDS);
				}
			}
			else {
				// one-time task execution
				this.executor.schedule(scheduledTask.getRunnable(), scheduledTask.getDelay(), TimeUnit.MILLISECONDS);
			}
		}
	}

	/**
	 * Create a new ScheduledExecutorService instance.
	 * Called by <code>afterPropertiesSet</code>.
	 * <p>Default implementation creates a ScheduledThreadPoolExecutor.
	 * Can be overridden in subclasses to provide custom
	 * ScheduledExecutorService instances.
	 * @param poolSize the specified pool size
	 * @return a new ScheduledExecutorService instance
	 * @see #afterPropertiesSet()
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#ScheduledThreadPoolExecutor(int)
	 */
	protected ScheduledExecutorService createExecutor(int poolSize) {
		return new ScheduledThreadPoolExecutor(poolSize);
	}


	public Object getObject() {
		return this.executor;
	}

	public Class getObjectType() {
		return ScheduledExecutorService.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Cancel the ScheduledExecutorService on bean factory shutdown,
	 * stopping all scheduled tasks.
	 * @see java.util.concurrent.ScheduledExecutorService#shutdown()
	 */
	public void destroy() {
		logger.info("Shutting down ScheduledExecutorService");
		this.executor.shutdown();
	}

}
