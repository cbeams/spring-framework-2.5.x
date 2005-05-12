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

/**
 * A view state is a state in which a physical view resource should be rendered
 * to the user, for example, for solicting form input.
 * <p>
 * To accomplish this, a <code>ViewState</code> returns a
 * <code>ViewDescriptor</code>, which contains the logical name of a view
 * template to render and all supporting model data needed to render it
 * correctly. It is expected that some sort of view resolver will map this view
 * name to a physical resource template (like a JSP file.)
 * <p>
 * A view state can also be a <i>marker</i> state with no associated view. In
 * this case it just returns control back to the client. Marker states are
 * useful for situations where an action has already generated the response.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ViewState extends TransitionableState {

	/**
	 * The factory for the view descriptor to return when this state is entered.
	 */
	private ViewDescriptorCreator viewDescriptorCreator;
	
	/**
	 * Default constructor for bean style usage.
	 */
	public ViewState() {
	}

	/**
	 * Create a new marker view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
	}

	/**
	 * Create a new marker view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the logical name of the view to render
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, String viewName, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
		setViewName(viewName);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the logical name of the view to render
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, String viewName, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setViewName(viewName);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the logical name of the view to render
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, String viewName, Transition[] transitions, Map properties) throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		setViewName(viewName);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the logical name of the view to render
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
		setViewDescriptorCreator(creator);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the logical name of the view to render
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setViewDescriptorCreator(creator);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param viewName the logical name of the view to render
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition[] transitions, Map properties) throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		setViewDescriptorCreator(creator);
	}
	
	/**
	 * Set the logical name of the view to render in this view state.
	 */
	public void setViewName(String viewName) {
		this.viewDescriptorCreator = new SimpleViewDescriptorCreator(viewName);
	}

	/**
	 * Set the factory that will produce a descriptor about the view to render in this view state.
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
	 * Returns a view descriptor pointing callers to a logical view resource to
	 * be displayed. The descriptor also contains a model map needed when the
	 * view is rendered, for populating dynamic content.
	 * @param context the state context for the executing flow
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state execution
	 */
	protected ViewDescriptor doEnter(StateContext context) {
		if (isMarker()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning a view descriptor null object; no view to render");
			}
			return null;
		}
		else {
			return viewDescriptorCreator.createViewDescriptor(context);
		}
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("viewDescriptorCreator", this.viewDescriptorCreator);
		super.createToString(creator);
	}
}