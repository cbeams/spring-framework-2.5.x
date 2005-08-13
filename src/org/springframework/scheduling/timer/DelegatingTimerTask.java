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

package org.springframework.scheduling.timer;

import java.util.TimerTask;

import org.springframework.util.Assert;

/**
 * Simple TimerTask adapter that delegates to a given Runnable.
 *
 * <p>This is often preferable to deriving from TimerTask, to be able to
 * implement an interface rather than extend an abstract base class.
 *
 * @author Juergen Hoeller
 * @since 1.2.4
 * @see java.util.TimerTask
 * @see java.lang.Runnable
 */
public class DelegatingTimerTask extends TimerTask {

	private final Runnable runnable;


	/**
	 * Create a new DelegatingTimerTask.
	 * @param runnable the Runnable implementation to delegate to
	 */
	public DelegatingTimerTask(Runnable runnable) {
		Assert.notNull(runnable, "Runnable is required");
		this.runnable = runnable;
	}


	/**
	 * Delegates execution to the underlying Runnable.
	 */
	public void run() {
		this.runnable.run();
	}

}
