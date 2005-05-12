/*
 * Copyright 2002-2005 the original author or authors.
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

import java.util.Map;

import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Terminates an active web flow session when entered. If the terminated session
 * is the root flow session, the entire flow execution ends. If the terminated
 * session is a subflow session, control returns to the parent flow session, and
 * this state is used as grounds for the transition in that resuming parent.
 * <p>
 * An end state may optionally be configured with the name of a view. This view
 * will be rendered if the end state terminates the entire flow execution.
 * <p>
 * Note: if no <code>viewName</code> property is specified <b>and</b> this
 * EndState terminates the entire flow execution, it is expected that some
 * action has already written the response (or else a blank response will
 * result). On the other hand, if no <code>viewName</code> is specified <b>and</b>
 * this EndState reliniquishes control back to a parent flow, view rendering
 * responsibility falls on the parent flow.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public class EndState extends State {

	/**
	 * An optional view descriptor creator that will produce a view to render if this
	 * end state terminates an executing root flow.
	 */
	private ViewDescriptorCreator viewDescriptorCreator;
	
	/**
	 * Default constructor for bean style usage.
	 */
	public EndState() {
	}

	/**
	 * Create a new end state with no associated view.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Create a new end state with specified associated view.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the name of the view that should be rendered if this end
	 *        state terminates flow execution
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id, String viewName) throws IllegalArgumentException {
		super(flow, id);
		setViewName(viewName);
	}

	/**
	 * Create a new end state with specified associated view.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the name of the view that should be rendered if this end
	 *        state terminates flow execution
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id, String viewName, Map properties) throws IllegalArgumentException {
		super(flow, id, properties);
		setViewName(viewName);
	}

	/**
	 * Create a new end state with specified associated view.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the name of the view that should be rendered if this end
	 *        state terminates flow execution
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id, ViewDescriptorCreator creator) throws IllegalArgumentException {
		super(flow, id);
		setViewDescriptorCreator(creator);
	}

	/**
	 * Create a new end state with specified associated view.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the name of the view that should be rendered if this end
	 *        state terminates flow execution
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id, ViewDescriptorCreator creator, Map properties) throws IllegalArgumentException {
		super(flow, id, properties);
		setViewDescriptorCreator(creator);
	}

	/**
	 * Set the logical name of the view to render when this end state is entered and terminates a
	 * root flow.
	 */
	public void setViewName(String viewName) {
		this.viewDescriptorCreator = new SimpleViewDescriptorCreator(viewName);
	}

	/**
	 * Set the factory to produce a view descriptor to render when this end state is entered and
	 * terminates a root flow.
	 */
	public void setViewDescriptorCreator(ViewDescriptorCreator creator) {
		this.viewDescriptorCreator = creator;
	}

	protected ViewDescriptorCreator getViewDescriptorCreator() {
		return viewDescriptorCreator;
	}
	
	/**
	 * Returns true if this view state has no associated view, false otherwise.
	 */
	public boolean isMarker() {
		return viewDescriptorCreator == null;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method
	 * that executes behaviour specific to this state type in polymorphic
	 * fashion.
	 * <p>
	 * This implementation pops the top (active) flow session off the execution
	 * stack, ending it, and resumes control in the parent flow (if neccessary).
	 * If the ended session is the root flow, a ViewDescriptor is
	 * returned (when viewName is not null, else null is returned).
	 * @param context the state context for the executing flow
	 * @return ViewDescriptor a view descriptor signaling that control should be
	 *         returned to the client and a view rendered
	 */
	protected ViewDescriptor doEnter(StateContext context) {
		if (context.getFlowContext().getActiveSession().isRoot()) {
			// entire flow execution is ending, return ending view if applicable
			if (logger.isDebugEnabled()) {
				logger.debug("Executing flow '" + getFlow().getId() + "' has ended");
			}
			ViewDescriptor viewDescriptor;
			if (isMarker()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning a view descriptor null object; no view to render");
				}
				viewDescriptor = null;
			}
			else {
				viewDescriptor = viewDescriptorCreator.createViewDescriptor(context);
			}
			// end the flow
			// note that we do this here to make sure we can call context.getModel()
			// above without any problems
			context.endActiveSession();
			return viewDescriptor;
		}
		else {
			// there is a parent flow that will resume, so map attributes from the
			// ending sub flow up to the resuming parent flow
			FlowSession parentSession = context.getFlowContext().getActiveSession().getParent();
			if (logger.isDebugEnabled()) {
				logger.debug("Resuming parent flow '" + parentSession.getFlow() + "' in state '"
						+ parentSession.getCurrentState() + "'");
			}
			Assert.isInstanceOf(FlowAttributeMapper.class, parentSession.getCurrentState());
			FlowAttributeMapper resumingState = (FlowAttributeMapper)parentSession.getCurrentState();
			resumingState.mapSubflowOutput(context);
			Assert.isInstanceOf(TransitionableState.class, resumingState);
			// actually end the subflow
			context.endActiveSession();
			return ((TransitionableState)resumingState).onEvent(subflowResult(context), context);
		}		
	}

	/**
	 * Hook method to create the subflow result event. Subclasses can override
	 * this if necessary.
	 */
	protected Event subflowResult(RequestContext context) {
		// treat this end state id as a transitional event in the
		// resuming state, this is so cool!
		return new Event(this, getId());
	}
	
	protected void createToString(ToStringCreator creator) {
		creator.append("viewDescriptorCreator", viewDescriptorCreator);
	}
}