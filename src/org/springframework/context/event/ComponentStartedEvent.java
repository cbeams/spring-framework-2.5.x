/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.Lifecycle;

/**
 * Event raised when a Lifecycle component is started.
 *
 * @author Mark Fisher
 * @since 2.1
 */
public class ComponentStartedEvent extends ApplicationEvent {

	/**
	 * Creates a new ComponentStartedEvent.
	 * @param source the Lifecycle instance
	 */
	public ComponentStartedEvent(Lifecycle source) {
		super(source);
	}

	/**
	 * Retrieves the <code>Lifecycle</code> instance that has been started.
	 * @return the <code>Lifecycle</code> instance that has been started
	 */
	public Lifecycle getComponent() {
		return (Lifecycle) getSource();
	}

}
