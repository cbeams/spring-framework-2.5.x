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
package org.springframework.web.flow;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.springframework.util.DefaultObjectStyler;

/**
 * Thrown when no event with id <code>eventId</code> exists in the specified
 * state for the specified flow.
 * @author Keith Donald
 */
public class NoSuchEventInStateException extends FlowNavigationException {

	private TransitionableState state;

	private String eventId;

	public NoSuchEventInStateException(Flow flow, TransitionableState state, String eventId) {
		super(flow);
		this.state = state;
		this.eventId = eventId;
	}

	public NoSuchEventInStateException(Flow flow, TransitionableState state, String eventId, Throwable cause) {
		super(flow, cause);
		this.state = state;
		this.eventId = eventId;
	}

	public String getMessage() {
		Collection t = state.getTransitions();
		Collection events;
		if (!t.isEmpty()) {
			events = new LinkedHashSet(t.size());
			Iterator it = t.iterator();
			while (it.hasNext()) {
				events.add(((Transition)it.next()).getId());
			}
		}
		else {
			events = Collections.EMPTY_SET;
		}
		return "No such transition for event '" + eventId + "' in state '" + state.getId() + "' in flow '"
				+ getFlow().getId() + "' -- valid events are " + DefaultObjectStyler.call(events)
				+ " -- programmer error?";
	}
}