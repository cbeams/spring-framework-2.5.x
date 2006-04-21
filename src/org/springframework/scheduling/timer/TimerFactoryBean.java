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

package org.springframework.scheduling.timer;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean that sets up a JDK 1.3+ Timer and exposes it for bean references.
 *
 * <p>Allows for registration of ScheduledTimerTasks, automatically starting
 * the Timer on initialization and cancelling it on destruction of the context.
 * In scenarios that just require static registration of tasks at startup,
 * there is no need to access the Timer instance itself in application code.
 *
 * <p>Note that Timer uses a TimerTask instance that is shared between
 * repeated executions, in contrast to Quartz which instantiates a new
 * Job for each execution.
 *
 * @author Juergen Hoeller
 * @since 19.02.2004
 * @see ScheduledTimerTask
 * @see java.util.Timer
 * @see java.util.TimerTask
 */
public class TimerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private ScheduledTimerTask[] scheduledTimerTasks;

	private boolean daemon = true;

	private Timer timer;


	/**
	 * Register a list of ScheduledTimerTask objects with the Timer that
	 * this FactoryBean creates. Depending on each SchedulerTimerTask's
	 * settings, it will be registered via one of Timer's schedule methods.
	 * @see java.util.Timer#schedule(java.util.TimerTask, long)
	 * @see java.util.Timer#schedule(java.util.TimerTask, long, long)
	 * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
	 */
	public void setScheduledTimerTasks(ScheduledTimerTask[] scheduledTimerTasks) {
		this.scheduledTimerTasks = scheduledTimerTasks;
	}

	/**
	 * Set whether the timer should use a daemon thread,
	 * just executing as long as the application itself is running.
	 * <p>Default is "true": In a J2EE environment, the container is in
	 * control of the application lifecycle.
	 * @see java.util.Timer#Timer(boolean)
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}


	public void afterPropertiesSet() {
		logger.info("Initializing Timer");
		this.timer = createTimer(this.daemon);

		// Register all ScheduledTimerTasks.
		if (this.scheduledTimerTasks != null) {
			for (int i = 0; i < this.scheduledTimerTasks.length; i++) {
				ScheduledTimerTask scheduledTask = this.scheduledTimerTasks[i];
				if (scheduledTask.getPeriod() > 0) {
					// repeated task execution
					if (scheduledTask.isFixedRate()) {
						this.timer.scheduleAtFixedRate(
								scheduledTask.getTimerTask(), scheduledTask.getDelay(), scheduledTask.getPeriod());
					}
					else {
						this.timer.schedule(
								scheduledTask.getTimerTask(), scheduledTask.getDelay(), scheduledTask.getPeriod());
					}
				}
				else {
					// One-time task execution.
					this.timer.schedule(scheduledTask.getTimerTask(), scheduledTask.getDelay());
				}
			}
		}
	}

	/**
	 * Create a new Timer instance. Called by <code>afterPropertiesSet</code>.
	 * Can be overridden in subclasses to provide custom Timer subclasses.
	 * @param daemon whether to create a Timer that runs as daemon thread
	 * @return a new Timer instance
	 * @see #afterPropertiesSet()
	 * @see java.util.Timer#Timer(boolean)
	 */
	protected Timer createTimer(boolean daemon) {
		return new Timer(daemon);
	}


	public Object getObject() {
		return this.timer;
	}

	public Class getObjectType() {
		return Timer.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Cancel the Timer on bean factory shutdown, stopping all scheduled tasks.
	 * @see java.util.Timer#cancel()
	 */
	public void destroy() {
		logger.info("Cancelling Timer");
		this.timer.cancel();
	}

}
