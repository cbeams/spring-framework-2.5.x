/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.thread;

/**
 * Service that publishes events to <code>ThreadCleanupListeners</code>
 * notifying them they should cleanup their thread specific storage.
 * @author Keith Donald
 */
public interface ThreadCleanupBroadcaster {

	/**
	 * Add a listener
	 * @param listener
	 */
	public void addThreadCleanupListener(ThreadCleanupListener listener);

	/**
	 * Remove the listener
	 * @param listener
	 */
	public void removeThreadCleanupListener(ThreadCleanupListener listener);

	/**
	 * Notify all listeners to cleanup. Typically called after processing a
	 * transaction; sometimes called when the app is shutdown.
	 */
	public void fireCleanupEvent();
}