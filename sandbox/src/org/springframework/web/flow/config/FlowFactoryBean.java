/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.flow.Flow;

public class FlowFactoryBean implements FactoryBean {

	private String flowId;

	private Collection flowExecutionListeners = new ArrayList(6);

	private FlowBuilder flowBuilder;

	private Flow flow;

	public void setFlowBuilder(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}
	
	public Object getObject() throws Exception {
		if (this.flow == null) {
			new FlowAssembler(this.flowBuilder).assemble();
			this.flow = this.flowBuilder.getFlow();
		}
		return this.flow;
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		return false;
	}

	public static class FlowAssembler {
		private FlowBuilder builder;

		public FlowAssembler(FlowBuilder builder) {
			this.builder = builder;
		}

		public void assemble() {
			this.builder.buildStates();
			this.builder.buildExecutionListeners();
		}
	}

}