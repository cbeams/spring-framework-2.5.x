package org.springframework.web.flow.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.flow.Flow;

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

	private Flow flow;

	public FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	public void setFlowServiceLocator(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
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