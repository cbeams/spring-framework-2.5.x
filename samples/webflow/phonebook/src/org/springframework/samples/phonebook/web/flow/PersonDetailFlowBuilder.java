/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.samples.phonebook.web.flow;

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
	}

}