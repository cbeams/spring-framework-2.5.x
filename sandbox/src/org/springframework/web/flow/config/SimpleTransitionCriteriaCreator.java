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
package org.springframework.web.flow.config;

import java.io.Serializable;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * Simple default implementation of the transition criteria factory.
 * It creates a default constraint implementation that will match true
 * on events with the provided event id. If the given event id is "*",
 * a wildcard event criteria object will be returned that matches any event.
 * Otherwise you get a criteria object that matches given event id exactly.
 * 
 * @author Rob Harraop
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class SimpleTransitionCriteriaCreator implements TransitionCriteriaCreator {

	/**
	 * Event id value ("*") that will cause the transition to match
	 * on any event.
	 */
	public static final String WILDCARD_EVENT_ID = "*";
	
	public TransitionCriteria create(String eventId) {
		return createDefaultTransitionCriteria(eventId);
	}

	/**
	 * Create a default constraint implementation that will match true on events
	 * with the provided event id.
	 * <p>
	 * If the given event id is "*", a wildcard event criteria object will be
	 * returned that matches any event. Otherwise you get a criteria object that
	 * matches given event id exactly.
	 */
	protected TransitionCriteria createDefaultTransitionCriteria(String eventId) {
		if (WILDCARD_EVENT_ID.equals(eventId)) {
			return WILDCARD_TRANSITION_CRITERIA;
		}
		else {
			// implementation note: this inner class is not a class constant
			// because we need the eventId
			return new EventIdTransitionCriteria(eventId);
		}
	}

	/**
	 * Event matching criteria that matches on any event.
	 */
	public static final TransitionCriteria WILDCARD_TRANSITION_CRITERIA = new TransitionCriteria() {
		public boolean test(RequestContext context) {
			return true;
		}

		public String toString() {
			return WILDCARD_EVENT_ID;
		}
	};

	/**
	 * Simple, default transition criteria that matches on an eventId and
	 * nothing else. Specifically, if the last event that occured has id
	 * ${eventId}, this criteria will return true.
	 */
	public static class EventIdTransitionCriteria implements TransitionCriteria, Serializable {

		private String eventId;

		public EventIdTransitionCriteria(String eventId) {
			this.eventId = eventId;
		}

		public boolean test(RequestContext context) {
			return context.getLastEvent().getId().equals(eventId);
		}

		public String toString() {
			return eventId;
		}
	}
}
