/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.samples.phonebook.web.flow;

import org.springframework.samples.phonebook.web.flow.action.QueryAction;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;

/**
 * @author Keith Donald
 */
public class SearchPersonFlowBuilder extends AbstractFlowBuilder {

	private static final String RESULTS = "results";

	private static final String CRITERIA = "criteria";

	protected String flowId() {
		return "person.Search";
	}

	public void buildStates() throws FlowBuilderException {
		addViewState(CRITERIA, onSubmitBindAndValidate(CRITERIA));
		addBindAndValidateState(CRITERIA, new Transition[] { onErrorView(CRITERIA), onSuccess("query") });
		addActionState("query", executeAction(QueryAction.class), new Transition[] { onErrorView(CRITERIA),
				onSuccessView(RESULTS) });
		String setUserId = qualify(set("userId"));
		addViewState(RESULTS, new Transition[] { onEvent("newSearch", view(CRITERIA)), onSelect(setUserId) });
		addActionState(setUserId, new Transition[] { onError("error"), onSuccess("person.Detail") });
		addSubFlowState("person.Detail", PersonDetailFlowBuilder.class, useModelMapper("userId"), new Transition[] {
				onFinish(view(RESULTS)), onError("error") });
		addEndState("error", "error.view");
	}
}