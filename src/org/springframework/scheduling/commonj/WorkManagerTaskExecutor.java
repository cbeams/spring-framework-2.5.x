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

package org.springframework.scheduling.commonj;

import java.util.Collection;

import javax.naming.NamingException;

import commonj.work.Work;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;
import commonj.work.WorkManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.scheduling.SchedulingException;
import org.springframework.util.Assert;

/**
 * TaskExecutor implementation that delegates to a CommonJ WorkManager,
 * which either needs to be specified as reference or through the JNDI name.
 *
 * <p><b>This is the central convenience class for setting up a
 * CommonJ WorkManager in a Spring context.</b>
 *
 * <p>Also implements the WorkManager interface itself, delegating all calls
 * to the target WorkManager. Hence, a caller can choose whether it wants
 * to talk to this executor through the Spring TaskExecutor interface or
 * the CommonJ WorkManager interface.
 *
 * <p>The CommonJ WorkManager will usually be retrieved from the application
 * server's JNDI environment, as defined in the server's management console.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class WorkManagerTaskExecutor extends JndiLocatorSupport
		implements TaskExecutor, WorkManager, InitializingBean {

	private WorkManager workManager;

	private String workManagerName;


	/**
	 * Specify the CommonJ WorkManager to delegate to.
	 * <p>Alternatively, you can also specify the JNDI name
	 * of the target WorkManager.
	 * @see #setWorkManagerName
	 */
	public void setWorkManager(WorkManager workManager) {
		this.workManager = workManager;
	}

	/**
	 * Set the JNDI name of the CommonJ WorkManager.
	 * <p>This can either be a fully qualified JNDI name,
	 * or the JNDI name relative to the current environment
	 * naming context if "resourceRef" is set to "true".
	 * @see #setWorkManager
	 * @see #setResourceRef
	 */
	public void setWorkManagerName(String workManagerName) {
		this.workManagerName = workManagerName;
	}

	public void afterPropertiesSet() throws NamingException {
		if (this.workManager == null) {
			if (this.workManagerName == null) {
				throw new IllegalArgumentException("Either 'workManager' or 'workManagerName' must be specified");
			}
			this.workManager = (WorkManager) lookup(this.workManagerName, WorkManager.class);
		}
	}


	//-------------------------------------------------------------------------
	// Implementation of the Spring TaskExecutor interface
	//-------------------------------------------------------------------------

	public void execute(Runnable task) {
		Assert.notNull(this.workManager, "WorkManager is required");
		try {
			this.workManager.schedule(new DelegatingWork(task));
		}
		catch (WorkException ex) {
			throw new SchedulingException("Could not schedule work on CommonJ WorkManager", ex);
		}
	}


	//-------------------------------------------------------------------------
	// Implementation of the CommonJ WorkManager interface
	//-------------------------------------------------------------------------

	public WorkItem schedule(Work work)
			throws WorkException, IllegalArgumentException {

		return this.workManager.schedule(work);
	}

	public WorkItem schedule(Work work, WorkListener workListener)
			throws WorkException, IllegalArgumentException {

		return this.workManager.schedule(work, workListener);
	}

	public boolean waitForAll(Collection workItems, long timeout)
			throws InterruptedException, IllegalArgumentException {

		return this.workManager.waitForAll(workItems, timeout);
	}

	public Collection waitForAny(Collection workItems, long timeout)
			throws InterruptedException, IllegalArgumentException {

		return this.workManager.waitForAny(workItems, timeout);
	}

}
