/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import org.springframework.web.flow.Flow;

/**
 * @author Keith Donald
 */
public interface FlowBuilder {
	public void initFlow();
	public void buildStates();
	public void buildExecutionListeners();
	public Flow getResult();
}
