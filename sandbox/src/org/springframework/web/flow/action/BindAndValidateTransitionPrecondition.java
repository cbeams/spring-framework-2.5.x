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
package org.springframework.web.flow.action;

import org.springframework.util.Assert;
import org.springframework.web.flow.ActionExecutionException;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * A transitional precondition that will bind and validate input in a submitting
 * event to a backing domain object and validate it, using the configured
 * FormAction. This precondition will return <code>true</code> if binding and
 * validation is successful (no errors), <code>false</code> otherwise.
 * 
 * @author Keith Donald
 */
public class BindAndValidateTransitionPrecondition implements TransitionCriteria {

	/**
	 * The form action that will do the bind and validate.
	 */
	private FormAction formAction;

	/**
	 * Create a bind and validate precondition delegating to the specified form
	 * action.
	 * 
	 * @param formAction
	 *            The form action
	 */
	public BindAndValidateTransitionPrecondition(FormAction formAction) {
		Assert.notNull(formAction, "The form action is required");
		this.formAction = formAction;
	}

	/* Allow the transition if the formAction returns success() on binding and validation.
	 * @see org.springframework.web.flow.TransitionCriteria#test(org.springframework.web.flow.RequestContext)
	 */
	public boolean test(RequestContext context) {
		try {
			Event result = formAction.bindAndValidate(context);
			if (result.getId().equals("success")) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			throw new ActionExecutionException("Unable to invoke bindAndValidate on target formAction " + formAction, e);
		}
	}
}
