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

package org.springframework.scheduling.timer;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean that sets up a J2SE Timer and exposes it for bean references.
 *
 * <p>Allows registration of ScheduledTimerTasks, automatically starting
 * the timer on initialization and cancelling it on destruction.
 * In typical scenarios, there is no need to access the Timer instance
 * itself in application code.
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
	 * this FactoryBean creates.
	 * @see java.util.Timer#schedule(java.util.TimerTask, long, long)
	 * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
	 */
	public void setScheduledTimerTasks(ScheduledTimerTask[] scheduledTimerTasks) {
		this.scheduledTimerTasks = scheduledTimerTasks;
	}

	/**
	 * Set whether the timer should use a daemon thread,
	 * just executing as long as the application itself is running.
	 * <p>Default is true: In a J2EE environment, the container is in
	 * control of the application lifecycle.
	 * @see java.util.Timer#Timer(boolean)
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public void afterPropertiesSet() {
		logger.info("Initializing Timer");
		this.timer = createTimer(this.daemon);
		for (int i = 0; i < this.scheduledTimerTasks.length; i++) {
			ScheduledTimerTask scheduledTask = this.scheduledTimerTasks[i];
			if (scheduledTask.isFixedRate()) {
				this.timer.scheduleAtFixedRate(scheduledTask.getTimerTask(), scheduledTask.getDelay(),
																			 scheduledTask.getPeriod());
			}
			else {
				this.timer.schedule(scheduledTask.getTimerTask(), scheduledTask.getDelay(),
														scheduledTask.getPeriod());
			}
		}
	}

	/**
	 * Create a new Timer instance. Called by afterPropertiesSet.
	 * Can be overridden in subclasses to provide custom Timer subclasses.
	 * @return a new Timer instance
	 * @see #afterPropertiesSet
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
	 * This implementation cancels the Timer, stopping all scheduled tasks.
	 * @see java.util.Timer#cancel
	 */
	public void destroy() {
		logger.info("Cancelling Timer");
		this.timer.cancel();
	}

}
