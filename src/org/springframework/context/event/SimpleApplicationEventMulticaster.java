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

package org.springframework.context.event;

import java.util.Iterator;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Simple implementation of the ApplicationEventMulticaster interface.
 *
 * <p>Multicasts all events to all registered listeners, leaving it up to
 * the listeners to ignore events that they are not interested in.
 * Listeners will usually perform corresponding <code>instanceof</code>
 * checks on the passed-in event object.
 *
 * <p>All listeners are invoked in the calling thread. This allows the danger of
 * a rogue listener blocking the entire application, but adds minimal overhead.
 *
 * <p>An alternative implementation could be more sophisticated in both these respects.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	public void multicastEvent(ApplicationEvent event) {
		for (Iterator it = getApplicationListeners().iterator(); it.hasNext();) {
			ApplicationListener listener = (ApplicationListener) it.next();
			listener.onApplicationEvent(event);
		}
	}

}
