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
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.action.EventParameterMapperAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.AutowireMode;
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
		addViewState("viewCriteria", "person.Search.criteria.view", on(submit(), "bindAndValidate"));

		// bind and validate search criteria 
		// TODO - investigate multi action builder helpers
		// e.g what i'd like to do here is map this state to a method on a multi action bean...
		addActionState("bindAndValidate", action("person.Search.bindAndValidate"), new Transition[] {
				on(error(), "viewCriteria"), on(success(), "executeQuery") });

		// execute query
		addActionState("executeQuery", action(ExecuteQueryAction.class, AutowireMode.BY_TYPE), new Transition[] {
				on(error(), "viewCriteria"), on(success(), "viewResults") });

		// view results
		addViewState("viewResults", "person.Search.results.view", new Transition[] { on("newSearch", "viewCriteria"),
				on(select(), "setId") });

		// set a user id in the model (selected from result list)
		EventParameterMapperAction setAction = new EventParameterMapperAction(new Mapping(ID,
				getConversionExecutor(Long.class)));
		setAction.setTargetScope(ScopeType.FLOW);
		addActionState("setId", setAction, new Transition[] { on(error(), "error"), on(success(), "person.Detail") });

		// view details for selected user id
		addSubFlowState("person.Detail", flow("person.Detail", PersonDetailFlowBuilder.class),
				attributeMapper("id.attributeMapper"), new Transition[] { on(finish(), "executeQuery"),
						on(error(), "error") });

		// end - an error occured
		addEndState(error(), "error.view");
	}
}