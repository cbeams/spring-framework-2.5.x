/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.util.Map;


public interface StateContext extends FlowExecutionContext {
	public State getCurrentState();

	public void setCurrentState(State state);

	public void setEvent(Event lastEvent);

	public FlowSession getActiveFlowSession();

	public FlowSession endActiveFlowSession();

	public ViewDescriptor spawn(Flow subFlow, Map subFlowInput);
}
