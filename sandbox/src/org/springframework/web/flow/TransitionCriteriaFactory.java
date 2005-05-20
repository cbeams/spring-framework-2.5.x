package org.springframework.web.flow;

/**
 * A factory producing transition criteria.
 * @author Keith Donald
 */
public class TransitionCriteriaFactory {

	/**
	 * Shared instance.
	 */
	public static final TransitionCriteria wildcardCriteria = new WildcardTransitionCriteria();
	
	/**
	 * Always returns true.
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
	 * Negates the result of a criteria evaluation.
	 * @author Keith Donald
	 */
	public static class NotTransitionCriteria implements TransitionCriteria {

		private TransitionCriteria criteria;
		
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
	 * Return a criteria that always returns 'true'.
	 * @return the wildcard criteria
	 */
	public static TransitionCriteria any() {
		return wildcardCriteria;
	}
	
	/**
	 * Not the specified criteria
	 * @param criteria the criteria
	 * @return the negation
	 */
	public static TransitionCriteria not(TransitionCriteria criteria) {
		return new NotTransitionCriteria(criteria);
	}
}