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

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.support.Mapping;
import org.springframework.samples.phonebook.web.flow.action.ExecuteQueryAction;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.action.SetAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;

/**
 * Java-based flow builder that searches for people in the phonebook. The flow
 * defined by this class is exactly the same as that defined in the
 * "search-flow.xml" XML flow definition.
 * <p>
 * This encapsulates the page flow of searching for some people, selecting a
 * person you care about, and viewing their person's details and those of their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class SearchPersonFlowBuilder extends AbstractFlowBuilder {

	private static final String SEARCH = "person.Search";

	private static final String CRITERIA = SEARCH + ".criteria";

	private static final String RESULTS = SEARCH + ".results";

	private static final String ID = "id";

	private ConversionService conversionService;

	protected ConversionExecutor getConversionExecutor(Class targetClass) {
		return conversionService.getConversionExecutor(String.class, targetClass);
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	protected String flowId() {
		return SEARCH;
	}

	public void buildStates() throws FlowBuilderException {
		// view search criteria
		addViewState(CRITERIA, onSubmitBindAndValidate(CRITERIA));

		// bind and validate search criteria
		addBindAndValidateState(CRITERIA, new Transition[] { onErrorView(CRITERIA), onSuccess("executeQuery") });

		// execute query
		addActionState("executeQuery", executeAction(ExecuteQueryAction.class), new Transition[] {
				onErrorView(CRITERIA), onSuccessView(RESULTS) });

		// view results
		String setId = set(ID);
		addViewState(RESULTS, new Transition[] { onEvent("newSearch", view(CRITERIA)), onSelect(setId) });

		// set a user id in the model (selected from result list)
		Action setAction = new SetAction(new Mapping(ID, getConversionExecutor(Long.class)));
		addActionState(setId, setAction, new Transition[] { onError("error"), onSuccess("person.Detail") });

		// view details for selected user id
		addSubFlowState("person.Detail", PersonDetailFlowBuilder.class, useModelMapper(ID), new Transition[] {
				onFinish(view(RESULTS)), onError("error") });

		// end - an error occured
		addErrorEndState("error.view");
	}
}