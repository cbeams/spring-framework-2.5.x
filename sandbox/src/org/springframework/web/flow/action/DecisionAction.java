package org.springframework.web.flow.action;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.config.TransitionCriteriaCreator;
import org.springframework.web.flow.support.OgnlTransitionCriteriaCreator;

/**
 * A action that accepts a String-encoded boolean expression and evaluates it, returning a "true"
 * result event if it evaluates true, a "false" result event otherwise.
 * @author Keith Donald
 */
public class DecisionAction extends AbstractAction {

	/**
	 * Constant for the "true" result.
	 */
	private static final String TRUE_RESULT_ID = "true";

	/**
	 * Constant for the "false" result. 
	 */
	private static final String FALSE_RESULT_ID = "false";

	/**
	 * The transition criteria creator strategy - produces criteria from an encoded string expression. 
	 */
	private TransitionCriteriaCreator criteriaCreator = new OgnlTransitionCriteriaCreator();

	/**
	 * Set the transition criteria creation strategy.
	 * @param creator The creator
	 */
	public void setTransitionCriteriaCreator(TransitionCriteriaCreator creator) {
		this.criteriaCreator = creator;
	}

	protected Event doExecuteAction(RequestContext context) throws Exception {
		String encodedCriteria = (String)context.getActionAttributes().getAttribute("criteria");
		boolean result = criteriaCreator.create(encodedCriteria).test(
				context);
		if (result) {
			return result(TRUE_RESULT_ID);
		}
		else {
			return result(FALSE_RESULT_ID);
		}
	}
}