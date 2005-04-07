
package org.springframework.web.flow.config;

import java.io.Serializable;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * @author robh
 */
public class SimpleTransitionCriteriaCreator implements TransitionCriteriaCreator {

	public TransitionCriteria create(String on) {
		return new EventIdTransitionCriteria(on);
	}

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
