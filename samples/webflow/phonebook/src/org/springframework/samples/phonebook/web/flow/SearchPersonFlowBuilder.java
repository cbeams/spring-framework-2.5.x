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
package org.springframework.samples.phonebook.web.flow;

import org.springframework.samples.phonebook.web.flow.action.QueryAction;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;

/**
 * Java-based flow builder that searches for people in the phonebook.
 * 
 * This encapsulates the page flow of searching for some people, selecting a
 * person you care about, and viewing their person's details and thosr of their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class SearchPersonFlowBuilder extends AbstractFlowBuilder {

	private static final String RESULTS = "results";

	private static final String CRITERIA = "criteria";

	protected String flowId() {
		return "person.Search";
	}

	public void buildStates() throws FlowBuilderException {
		// view search criteria
		addViewState(CRITERIA, onSubmitBindAndValidate(CRITERIA));

		// bind and validate search criteria
		addBindAndValidateState(CRITERIA, new Transition[] { onErrorView(CRITERIA), onSuccess("query") });

		// execute query
		addActionState("query", executeAction(QueryAction.class), new Transition[] { onErrorView(CRITERIA),
				onSuccessView(RESULTS) });

		// view results
		String setUserId = qualify(set("userId"));
		addViewState(RESULTS, new Transition[] { onEvent("newSearch", view(CRITERIA)), onSelect(setUserId) });

		// set a user id in the model (selected from result list)
		addActionState(setUserId, new Transition[] { onError("error"), onSuccess("person.Detail") });

		// view details for selected user id
		addSubFlowState("person.Detail", PersonDetailFlowBuilder.class, useModelMapper("userId"), new Transition[] {
				onFinish(view(RESULTS)), onError("error") });

		// end - an error occured
		addErrorEndState("error.view");
	}
}