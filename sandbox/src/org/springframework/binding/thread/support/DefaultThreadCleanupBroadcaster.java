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
package org.springframework.binding.thread.support;

import java.util.Iterator;

import org.springframework.binding.thread.ThreadCleanupBroadcaster;
import org.springframework.binding.thread.ThreadCleanupListener;
import org.springframework.util.EventListenerListHelper;

public class DefaultThreadCleanupBroadcaster implements ThreadCleanupBroadcaster {
	private ThreadLocal listenerListHolder = new ThreadLocal();

	public void addThreadCleanupListener(ThreadCleanupListener listener) {
		EventListenerListHelper list = (EventListenerListHelper)listenerListHolder.get();
		if (list == null) {
			list = new EventListenerListHelper(ThreadCleanupListener.class);
			listenerListHolder.set(list);
		}
		list.add(listener);
	}

	public void removeThreadCleanupListener(ThreadCleanupListener listener) {
		EventListenerListHelper list = (EventListenerListHelper)listenerListHolder.get();
		if (list != null) {
			list.remove(listener);
		}
	}

	public void fireCleanupEvent() {
		EventListenerListHelper list = (EventListenerListHelper)listenerListHolder.get();
		if (list == null) {
			return;
		}
		Iterator it = list.iterator();
		while (it.hasNext()) {
			ThreadCleanupListener listener = (ThreadCleanupListener)it.next();
			listener.onCleanup();
		}
	}
}