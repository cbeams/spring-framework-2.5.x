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

import org.springframework.web.flow.Transition;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;

/**
 * @author Keith Donald
 */
public class PersonDetailFlowBuilder extends AbstractFlowBuilder {

	protected String flowId() {
		return "person.Detail";
	}

	public void buildStates() throws FlowBuilderException {
		addGetState(flowId());
		String setCollegueId = qualify(set("collegueId"));
		addViewState(flowId(), new Transition[] { onBack("finish"), onSelect(setCollegueId) });
		addActionState(setCollegueId, onSuccess("collegue.Detail"));
		addSubFlowState("collegue.Detail", PersonDetailFlowBuilder.class, useModelMapper("collegueId"),
				new Transition[] { onFinishGet(flowId()), onError("error") });
		addEndState("finish");
	}
}