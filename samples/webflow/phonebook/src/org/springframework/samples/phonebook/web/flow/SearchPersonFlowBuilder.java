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

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.support.Mapping;
import org.springframework.samples.phonebook.web.flow.action.ExecuteQueryAction;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.AutowireMode;
import org.springframework.web.flow.config.FlowBuilderException;
import org.springframework.web.flow.support.ParameterizableFlowAttributeMapper;

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

	private static final String DISPLAY_CRITERIA = "displayCriteria";

	private static final String EXECUTE_QUERY = "executeQuery";

	private static final String DISPLAY_RESULTS = "displayResults";

	private static final String SHOW_DETAILS = "showDetails";

	protected String flowId() {
		return "person.Search";
	}

	public void buildStates() throws FlowBuilderException {
		// view search criteria
		addViewState(DISPLAY_CRITERIA, "person.Search.criteria.view", on(submit(), EXECUTE_QUERY,
				beforeExecute(method("bindAndValidate", action("person.Search.criteria.formAction")))));

		// execute query
		addActionState(EXECUTE_QUERY, action(ExecuteQueryAction.class, AutowireMode.BY_TYPE), new Transition[] {
				on(error(), DISPLAY_CRITERIA), on(success(), DISPLAY_RESULTS) });

		// view results
		addViewState(DISPLAY_RESULTS, "person.Search.results.view", new Transition[] { on("newSearch", DISPLAY_CRITERIA),
				on(select(), SHOW_DETAILS) });

		// view details for selected user id
		ParameterizableFlowAttributeMapper idMapper = new ParameterizableFlowAttributeMapper();
		idMapper.setInputMapping(new Mapping("sourceEvent.parameters.id", "flowScope.id", converterFor(Long.class)));
		addSubFlowState(SHOW_DETAILS, flow("person.Detail", PersonDetailFlowBuilder.class), idMapper,
				new Transition[] { on(finish(), EXECUTE_QUERY), on(error(), "error") });

		// end - an error occured
		addEndState(error(), "error.view");
	}
}