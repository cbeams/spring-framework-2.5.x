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

package org.springframework.scheduling.quartz;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import org.springframework.core.task.TaskExecutor;

/**
 * Quartz ThreadPool adapter that delegates to a Spring-managed
 * TaskExecutor instance, specified on SchedulerFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SchedulerFactoryBean#setTaskExecutor
 */
public class LocalTaskExecutorThreadPool implements ThreadPool {

	private TaskExecutor taskExecutor;


	public void initialize() throws SchedulerConfigException {
		// Absolutely needs thread-bound TaskExecutor to initialize.
		this.taskExecutor = SchedulerFactoryBean.getConfigTimeTaskExecutor();
		if (this.taskExecutor == null) {
			throw new SchedulerConfigException(
			    "No local TaskExecutor found for configuration - " +
			    "'taskExecutor' property must be set on SchedulerFactoryBean");
		}
	}

	public void shutdown(boolean waitForJobsToComplete) {
	}

	public int getPoolSize() {
		return -1;
	}


	public boolean runInThread(Runnable runnable) {
		if (runnable == null) {
			return false;
		}
		this.taskExecutor.execute(runnable);
		return true;
	}

}
