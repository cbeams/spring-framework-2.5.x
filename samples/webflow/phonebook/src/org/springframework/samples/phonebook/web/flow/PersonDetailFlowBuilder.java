/*
 * Copyright 2004-2005 the original author or authors.
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
		String setCollegueId = set("collegueId");
		addViewState(flowId(), new Transition[] { onBack("finish"), onSelect(setCollegueId) });
		addActionState(setCollegueId, qualify(setCollegueId), onSuccess("collegue.Detail"));
		addSubFlowState("collegue.Detail", PersonDetailFlowBuilder.class, useModelMapper("collegueId"),
				new Transition[] { onFinishGet(flowId()), onError("error") });
		addEndState("finish");
	}
}