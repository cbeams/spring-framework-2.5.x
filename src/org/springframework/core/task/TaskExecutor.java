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

/**
 * Simple task executor interface that abstracts the execution
 * of a Runnable. Implementations can use all sorts of different
 * execution strategies, such as: synchronous, asynchronous,
 * using a thread pool, etc.
 *
 * <p>Identical to JDK 1.5's <code>java.util.concurrent.Executor</code>
 * interface. Separate mainly for compatibility with JDK 1.3+.
 * Implementations can simply implement the JDK 1.5 Executor interface
 * as well, as it defines the exact same method signature.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 */
public interface TaskExecutor {

	/**
	 * Execute the given task.
	 * <p>The call might return immediately if the
	 * executor uses an asynchronous execution strategy,
	 * or might block in case synchronous execution.
	 * @param task the Runnable to execute
	 * @see java.util.concurrent.Executor#execute(Runnable)
	 */
	void execute(Runnable task);

}
