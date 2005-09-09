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

package org.springframework.core.task;

import java.io.Serializable;

import org.springframework.util.ConcurrencyThrottleSupport;

/**
 * TaskExecutor implementation that fires up a new Thread for each task,
 * executing it asynchronously.
 *
 * <p>Supports limiting concurrent threads through the "concurrencyLimit"
 * bean property. By default, only 1 concurrent thread is allowed.
 *
 * <p>Does not reuse threads: Consider a thread-pooling TaskExecutor
 * implementation instead.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see #setConcurrencyLimit
 * @see SyncTaskExecutor
 * @see org.springframework.scheduling.timer.TimerTaskExecutor
 */
public class SimpleAsyncTaskExecutor extends ConcurrencyThrottleSupport
		implements TaskExecutor, Serializable {

	public void execute(Runnable task) {
		beforeAccess();
		try {
			new Thread(task).start();
		}
		finally {
			afterAccess();
		}
	}

}
