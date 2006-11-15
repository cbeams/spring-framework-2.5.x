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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import commonj.timers.Timer;
import commonj.timers.TimerManager;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiLocatorSupport;

/**
 * FactoryBean that retrieves a CommonJ TimerManager and exposes it for
 * bean references.
 *
 * <p><b>This is the central convenience class for setting up a
 * CommonJ TimerManager in a Spring context.</b>
 *
 * <p>Allows for registration of ScheduledTimerListeners. This is the main
 * purpose of this class; the TimerManager itself could also be fetched
 * from JNDI via JndiObjectFactoryBean. In scenarios that just require
 * static registration of tasks at startup, there is no need to access
 * the TimerManager itself in application code.
 *
 * <p>Note that the TimerManager uses a TimerListener instance that is
 * shared between repeated executions, in contrast to Quartz which
 * instantiates a new Job for each execution.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ScheduledTimerListener
 * @see commonj.timers.TimerManager
 * @see commonj.timers.TimerListener
 */
public class TimerManagerFactoryBean extends JndiLocatorSupport
		implements FactoryBean, InitializingBean, DisposableBean {

	private TimerManager timerManager;

	private String timerManagerName;

	private ScheduledTimerListener[] scheduledTimerListeners;

	private final List timers = new LinkedList();


	/**
	 * Specify the CommonJ TimerManager to delegate to.
	 * <p>Alternatively, you can also specify the JNDI name
	 * of the target TimerManager.
	 * @see #setTimerManagerName
	 */
	public void setTimerManager(TimerManager timerManager) {
		this.timerManager = timerManager;
	}

	/**
	 * Set the JNDI name of the CommonJ TimerManager.
	 * <p>This can either be a fully qualified JNDI name,
	 * or the JNDI name relative to the current environment
	 * naming context if "resourceRef" is set to "true".
	 * @see #setTimerManager
	 * @see #setResourceRef
	 */
	public void setTimerManagerName(String timerManagerName) {
		this.timerManagerName = timerManagerName;
	}

	/**
	 * Register a list of ScheduledTimerListener objects with the TimerManager
	 * that this FactoryBean creates. Depending on each ScheduledTimerListener's settings,
	 * it will be registered via one of TimerManager's schedule methods.
	 * @see commonj.timers.TimerManager#schedule(commonj.timers.TimerListener, long)
	 * @see commonj.timers.TimerManager#schedule(commonj.timers.TimerListener, long, long)
	 * @see commonj.timers.TimerManager#scheduleAtFixedRate(commonj.timers.TimerListener, long, long)
	 */
	public void setScheduledTimerListeners(ScheduledTimerListener[] scheduledTimerListeners) {
		this.scheduledTimerListeners = scheduledTimerListeners;
	}


	public void afterPropertiesSet() throws NamingException {
		if (this.timerManager == null) {
			if (this.timerManagerName == null) {
				throw new IllegalArgumentException("Either 'timerManager' or 'timerManagerName' must be specified");
			}
			this.timerManager = (TimerManager) lookup(this.timerManagerName, TimerManager.class);
		}

		// register all ScheduledTimerListeners
		for (int i = 0; i < this.scheduledTimerListeners.length; i++) {
			ScheduledTimerListener scheduledTask = this.scheduledTimerListeners[i];
			Timer timer = null;
			if (scheduledTask.getPeriod() > 0) {
				// repeated task execution
				if (scheduledTask.isFixedRate()) {
					timer = this.timerManager.scheduleAtFixedRate(
							scheduledTask.getTimerListener(), scheduledTask.getDelay(), scheduledTask.getPeriod());
				}
				else {
					timer = this.timerManager.schedule(
							scheduledTask.getTimerListener(), scheduledTask.getDelay(), scheduledTask.getPeriod());
				}
			}
			else {
				// one-time task execution
				timer = this.timerManager.schedule(scheduledTask.getTimerListener(), scheduledTask.getDelay());
			}
			this.timers.add(timer);
		}
	}


	public Object getObject() {
		return this.timerManager;
	}

	public Class getObjectType() {
		return TimerManager.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Cancel all statically registered Timers on shutdown.
	 */
	public void destroy() {
		for (Iterator it = this.timers.iterator(); it.hasNext();) {
			Timer timer = (Timer) it.next();
			try {
				timer.cancel();
			}
			catch (Throwable ex) {
				logger.warn("Could not cancel CommonJ Timer", ex);
			}
		}
		this.timers.clear();
	}

}
