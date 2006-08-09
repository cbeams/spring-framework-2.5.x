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

package org.springframework.core.task;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * TaskExecutor implementation that executes each task synchronously
 * in the calling thread. Mainly intended for testing scenarios.
 *
 * <p>Execution in the calling thread does have the advantage of participating
 * in its thread context, for example the thread context class loader or the
 * thread's current transaction association. That said, in many cases, asynchronous
 * execution will be preferable: Choose an asynchronous TaskExecutor instead for
 * such scenarios.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleAsyncTaskExecutor
 * @see org.springframework.scheduling.timer.TimerTaskExecutor
 */
public class SyncTaskExecutor implements TaskExecutor, Serializable {

	/**
	 * Executes the given task synchronously,
	 * through direct invocation of its <code>run()</code> method.
	 */
	public void execute(Runnable task) {
		Assert.notNull("Runnable must not be null");
		task.run();
	}

}
