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
package org.springframework.web.flow;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;
import org.springframework.web.servlet.ModelAndView;

/**
 * A base super class for a state definition, associatable with any number of
 * Flow definitions. Types of states include action states, view states, subflow
 * states, and end states.
 * 
 * Subclasses of this class capture all the configuration information needed for
 * a specific type of state associatable with a single Flow definition.
 * Subclasses should override the <code>doEnterState</code> method to execute
 * the action that should occur when this state is entered, acting on its
 * configuration information.
 * 
 * @author Keith Donald
 */
public abstract class AbstractState implements Serializable {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The state's owning flow.
	 */
	private Flow flow;

	/**
	 * The state identifier, unique to the owning flow.
	 */
	private String id;

	/**
	 * Creates a state for the provided <code>flow</code> identified by the
	 * provided <code>id</code>.
	 * 
	 * The id must be locally unique to the owning flow.
	 * 
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow);
	 */
	public AbstractState(Flow flow, String id) {
		setId(id);
		setFlow(flow);
		flow.add(this);
	}

	/**
	 * Returns the state identifier, unique to the owning flow.
	 * @return The state identifier.
	 */
	public String getId() {
		return id;
	}

	private void setId(String id) {
		Assert.hasText(id, "The state must have a valid identifier");
		this.id = id;
	}

	public Flow getFlow() {
		return flow;
	}

	private void setFlow(Flow flow) {
		Assert.notNull(flow, "The owning flow is required");
		this.flow = flow;
	}

	/**
	 * Is this state transitionable? Meaning, is it capable of executing a
	 * transition to another state on the occurence of an event? All subclasses
	 * of <code>TransitionableState</code> are transitionable.
	 * @return true or false
	 */
	public boolean isTransitionable() {
		return false;
	}

	/**
	 * Is this state a view state? View states return
	 * <code>View Descriptors</code> that request rendering of a view resource
	 * with model data.
	 * @return true or false
	 */
	public boolean isViewState() {
		return false;
	}

	/**
	 * Is this state an action state? Action states execute action beans when
	 * entered.
	 * @return true or false
	 */
	public boolean isActionState() {
		return false;
	}

	/**
	 * Is this state a sub flow state? Sub flow states spawn flows as sub flows
	 * within a session execution when entered.
	 * @return true or false
	 */
	public boolean isSubFlowState() {
		return false;
	}

	/**
	 * Is this state an end state? End states terminate a flow session when
	 * entered.
	 * @return
	 */
	public boolean isEndState() {
		return false;
	}

	public boolean equals(Object o) {
		if (!(o instanceof AbstractState)) {
			return false;
		}
		AbstractState s = (AbstractState)o;
		return flow.equals(s.flow) && id.equals(s.id);
	}

	public int hashCode() {
		return flow.hashCode() + id.hashCode();
	}

	/**
	 * Requesting entering of this state for the active (currently executing)
	 * flow session.
	 * @param flowExecution The flow execution stack, tracking the current
	 *        active flow session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected final ModelAndView enter(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("Entering state '" + getId() + "' in flow '" + getFlow() + "'");
		}
		flowExecution.setCurrentState(this);
		return doEnterState(flowExecution, request, response);
	}

	/**
	 * Hook method to do any processing as a result of entering this state.
	 * @param flowExecution The session execution stack, tracking the
	 *        current active flow session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected abstract ModelAndView doEnterState(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response);

	public String toString() {
		ToStringCreator creator = new ToStringCreator(this).append("id", getId());
		createToString(creator);
		return creator.toString();
	}

	/**
	 * Method subclasses may override to insert toStringification of their
	 * properties.
	 * @param creator
	 */
	protected void createToString(ToStringCreator creator) {
	}
}