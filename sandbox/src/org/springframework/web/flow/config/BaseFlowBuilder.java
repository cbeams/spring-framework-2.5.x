package org.springframework.web.flow.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecutionListener;

/**
 * Abstract base implementation of a flow builder defining base functionality
 * needed by most concrete flow builder implementations.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class BaseFlowBuilder extends FlowConstants implements FlowBuilder {

	protected final Log logger = LogFactory.getLog(getClass());

	private FlowServiceLocator flowServiceLocator;

	private Collection flowExecutionListeners = new ArrayList(3);

	private Flow flow;

	public FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	public void setFlowServiceLocator(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.add(listener);
	}

	public void setFlowExecutionListeners(FlowExecutionListener[] listeners) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.addAll(Arrays.asList(listeners));
	}

	/**
	 * Creates and/or links applicable flow execution listeners up to the flow
	 * built by this builder.
	 */
	public void buildExecutionListeners() throws FlowBuilderException {
		getFlow().getFlowExecutionListenerList().add(
				(FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[0]));
	}

	/**
	 * Get the flow being built by this builder.
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow being built by this builder.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Flow getResult() {
		return getFlow();
	}

	// hooks for subclassing

	/**
	 * Create the instance of the Flow built by this builder. Subclasses may
	 * override to return a custom Flow implementation.
	 * 
	 * @param id The flow identifier.
	 * @return The flow built by this builder.
	 */
	protected Flow createFlow(String id) {
		return new Flow(id);
	}
}