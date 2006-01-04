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

package org.springframework.aop.target.dynamic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;

/**
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0M2
 */
public abstract class AbstractRefreshableTargetSource implements TargetSource, Refreshable {

	public static final int REFRESH_CHECK_NEVER = -1;

	public static final int REFRESH_CHECK_ALWAYS = 0;

	protected Log logger = LogFactory.getLog(getClass());

	protected Object targetObject;

	private long lastRefreshCheck = System.currentTimeMillis();

	private long refreshCheckDelay = REFRESH_CHECK_NEVER;

	private long lastRefreshTime;

	private long refreshCount;


	public long getRefreshCount() {
		return this.refreshCount;
	}

	public long getLastRefreshTime() {
		return this.lastRefreshTime;
	}

	public void setRefreshCheckDelay(long refreshCheckDelay) {
		this.refreshCheckDelay = refreshCheckDelay;
	}

	public Class getTargetClass() {
		return (this.targetObject == null) ? null : this.targetObject.getClass();
	}

	public boolean isStatic() {
		return false;
	}

	public final synchronized Object getTarget() throws Exception {
		if ((this.targetObject == null) || (refreshCheckDelayElapsed() && requiresRefresh())) {
			refresh();
		}

		return this.targetObject;
	}

	public void releaseTarget(Object object) throws Exception {
	}

	public final synchronized void refresh() {
		logger.debug("Attempting to refresh target.");

		this.refreshCount++;
		this.lastRefreshTime = System.currentTimeMillis();
		this.targetObject = freshTarget();

		logger.debug("Target refreshed successfully.");
	}

	private boolean refreshCheckDelayElapsed() {
		if (this.refreshCheckDelay == REFRESH_CHECK_NEVER) {
			return false;
		}
		long currentTimeMillis = System.currentTimeMillis();
		boolean elapsed = (currentTimeMillis - this.lastRefreshCheck) > this.refreshCheckDelay;

		if (elapsed) {
			// going to perform a refresh check - update the time
			logger.debug("Refresh check delay elapsed - will check if refresh is necessary.");
			this.lastRefreshCheck = currentTimeMillis;
		}
		return elapsed;
	}

	protected boolean requiresRefresh() {
		return false;
	}

	protected abstract Object freshTarget();

}
