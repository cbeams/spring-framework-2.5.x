/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.context.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Simple implementation of the ApplicationEventMulticaster interface.
 * Multicasts all events to all registered listeners.
 *
 * <p>Doesn't permit multiple instances of the same listener, as it keeps
 * listeners in a HashSet.
 *
 * <p>Note that this class doesn't try to do anything clever to ensure thread
 * safety if listeners are added or removed at runtime. A technique such as
 * Copy-on-Write (Lea:137) could be used to ensure this, but the assumption in
 * this version of this framework is that listeners will be added at application
 * configuration time and not added or removed as the application runs.
 *
 * <p>All listeners are invoked in the calling thread. This allows the danger of
 * a rogue listener blocking the entire application, but adds minimal overhead.
 *
 * <p>An alternative implementation could be more sophisticated in both these respects.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class SimpleApplicationEventMulticaster implements ApplicationEventMulticaster {

	/** Set of listeners */
	private final Set applicationListeners = new HashSet();

	public void addApplicationListener(ApplicationListener listener) {
		this.applicationListeners.add(listener);
	}

	public void removeApplicationListener(ApplicationListener listener) {
		this.applicationListeners.remove(listener);
	}

	public void removeAllListeners() {
		this.applicationListeners.clear();
	}

	public void multicastEvent(ApplicationEvent event) {
		Iterator it = this.applicationListeners.iterator();
		while (it.hasNext()) {
			ApplicationListener listener = (ApplicationListener) it.next();
			listener.onApplicationEvent(event);
		}
	}

}
