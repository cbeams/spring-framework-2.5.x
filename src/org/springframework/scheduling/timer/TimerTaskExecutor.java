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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;

/**
 * TaskExecutor implementation that uses a single <code>java.util.Timer</code>
 * for executing all tasks, effectively resulting in serialized asynchronous
 * execution on a single thread.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.Timer
 */
public class TimerTaskExecutor implements SchedulingTaskExecutor, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Timer timer;

	private int delay = 0;

	private boolean internalTimer = false;


	/**
	 * Create a new TimerTaskExecutor that needs to be further
	 * configured and initialized.
	 * @see #setTimer
	 * @see #afterPropertiesSet
	 */
	public TimerTaskExecutor() {
	}

	/**
	 * Create a new TimerTaskExecutor for the given Timer.
	 * @param timer the Timer to wrap
	 */
	public TimerTaskExecutor(Timer timer) {
		Assert.notNull(timer, "Timer must not be null");
		this.timer = timer;
	}

	/**
	 * Set the <code>java.util.Timer</code> to use for this TaskExecutor,
	 * for example a shared Timer instance defined by a TimerFactoryBean.
	 * <p>If not specified, a default Timer instance will be used.
	 * @see TimerFactoryBean
	 */
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * Set the delay to use for scheduling tasks passed into the
	 * <code>execute</code> method. Default is 0.
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}


	public void afterPropertiesSet() {
		if (this.timer == null) {
			logger.info("Initializing Timer");
			this.timer = new Timer(true);
			this.internalTimer = true;
		}
	}

	/**
	 * Create a new Timer instance. Called by <code>afterPropertiesSet</code>
	 * if no Timer has been specified explicitly.
	 * <p>Default implementation creates a plain daemon Timer.
	 * @return a new Timer instance
	 * @see #afterPropertiesSet
	 * @see java.util.Timer#Timer(boolean)
	 */
	protected Timer createTimer() {
		return new Timer(true);
	}


	/**
	 * Schedules the given Runnable on this executor's Timer instance,
	 * wrapping it in a DelegatingTimerTask.
	 */
	public void execute(Runnable task) {
		Assert.notNull(this.timer, "timer is required");
		this.timer.schedule(new DelegatingTimerTask(task), this.delay);
	}

	public boolean isShortLivedPreferred() {
		return true;
	}


	/**
	 * Cancel the Timer on bean factory shutdown, stopping all scheduled tasks.
	 * @see java.util.Timer#cancel()
	 */
	public void destroy() {
		if (this.internalTimer) {
			logger.info("Cancelling Timer");
			this.timer.cancel();
		}
	}

}
