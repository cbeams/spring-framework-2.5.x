package org.springframework.web.flow;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * A factory producing commonly used transition criteria objects.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TransitionCriteriaFactory {

	/**
	 * Transition criteria that always returns true.
	 * 
	 * @author Keith Donald
	 */
	public static class WildcardTransitionCriteria implements TransitionCriteria {

		/**
		 * Event id value ("*") that will cause the transition to match
		 * on any event.
		 */
		public static final String WILDCARD_EVENT_ID = "*";
		
		public boolean test(RequestContext context) {
			return true;
		}
		
		public String toString() {
			return WILDCARD_EVENT_ID;
		}

	}

	/**
	 * Transition criteria that negates the result of the evaluation of
	 * another criteria object.
	 * 
	 * @author Keith Donald
	 */
	public static class NotTransitionCriteria implements TransitionCriteria {

		private TransitionCriteria criteria;
		
		/**
		 * Create a new transition criteria object that will negate
		 * the result of given criteria object.
		 * @param criteria the criteria to negate 
		 */
		public NotTransitionCriteria(TransitionCriteria criteria) {
			this.criteria = criteria;
		}
		
		public boolean test(RequestContext context) {
			return !criteria.test(context);
		}
		
		public String toString() {
			return "not(" + criteria + ")";
		}
	}

	/**
	 * Simple transition criteria that matches on an eventId and
	 * nothing else. Specifically, if the last event that occured has id
	 * ${eventId}, this criteria will return true.
	 * 
	 * @author Erwin Vervaet
	 * @author Keith Donald
	 */
	public static class EventIdTransitionCriteria implements TransitionCriteria, Serializable {

		private String eventId;

		/**
		 * Create a new event id matching criteria object.
		 * @param eventId the event id
		 */
		public EventIdTransitionCriteria(String eventId) {
			Assert.notNull(eventId);
			this.eventId = eventId;
		}

		public boolean test(RequestContext context) {
			return eventId.equals(context.getLastEvent().getId());
		}

		public String toString() {
			return "'" + eventId + "'";
		}
	}

	/**
	 * Returns a transition criteria object that always returns 'true'.
	 * @return the wildcard criteria
	 */
	public static TransitionCriteria any() {
		return new WildcardTransitionCriteria();
	}
	
	/**
	 * Returns a transtion criteria object that negates the result of the
	 * specified criteria.
	 * @param criteria the criteria to negate
	 * @return the negating criteria
	 */
	public static TransitionCriteria not(TransitionCriteria criteria) {
		return new NotTransitionCriteria(criteria);
	}
	
	/**
	 * Returns the transition criteria object that matches given event id
	 * as the last event that occured in the request context.
	 * @param eventId the event id to match
	 * @return the event id matching transition criteria
	 */
	public static TransitionCriteria eventId(String eventId) {
		return new EventIdTransitionCriteria(eventId);
	}
}