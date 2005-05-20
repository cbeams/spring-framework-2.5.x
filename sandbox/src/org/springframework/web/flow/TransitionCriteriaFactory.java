package org.springframework.web.flow;

/**
 * A factory producing commonly used transition criteria objects.
 * 
 * @author Keith Donald
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
}