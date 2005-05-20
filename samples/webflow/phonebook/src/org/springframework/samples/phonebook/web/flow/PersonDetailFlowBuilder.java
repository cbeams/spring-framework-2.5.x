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
import org.springframework.samples.phonebook.web.flow.action.GetPersonAction;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.action.EventParameterMapperAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.AutowireMode;
import org.springframework.web.flow.config.FlowBuilderException;
import org.springframework.web.flow.support.ParameterizableFlowAttributeMapper;

/**
 * Java-based flow builder that builds the person details flow, exactly like it
 * is defined in the "detail-flow.xml" XML flow definition.
 * <p>
 * This encapsulates the page flow of viewing a person's details and their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class PersonDetailFlowBuilder extends AbstractFlowBuilder {

	private static final String PERSON_DETAIL = "person.Detail";

	private ConversionService conversionService;

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	protected String flowId() {
		return PERSON_DETAIL;
	}

	protected ConversionExecutor getConversionExecutor(Class targetClass) {
		return conversionService.getConversionExecutor(String.class, targetClass);
	}

	public void buildStates() throws FlowBuilderException {
		// get the person given a userid as input
		addActionState("getPerson", action(GetPersonAction.class, AutowireMode.BY_TYPE), on(success(), "viewDetails"));

		// view the person details
		addViewState("viewDetails", "person.Detail.view", new Transition[] { on(back(), "finish"),
				on(select(), "setCollegueId") });

		// set the selected colleague (chosen from the person's colleague list)
		EventParameterMapperAction setAction = new EventParameterMapperAction(new Mapping("id", "colleagueId",
				getConversionExecutor(Long.class)));
		setAction.setTargetScope(ScopeType.FLOW);
		addActionState("setCollegueId", setAction, on(success(), "person.Detail"));

		// view details for selected collegue
		ParameterizableFlowAttributeMapper idMapper = new ParameterizableFlowAttributeMapper();
		idMapper.setInputMapping(new Mapping("colleagueId", "id"));
		addSubFlowState("person.Detail", flow("person.Detail", PersonDetailFlowBuilder.class),
				idMapper, new Transition[] { on(finish(), "getPerson"),
						on(error(), "error") });

		// end
		addEndState("finish");

		// end error
		addEndState("error");
	}
}