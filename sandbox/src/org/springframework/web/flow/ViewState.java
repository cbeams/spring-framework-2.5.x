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

import org.springframework.util.StringUtils;
import org.springframework.util.ToStringCreator;

/**
 * A view state is a state in which a physical view resource should be rendered
 * to the user, for example, for solicting form input.
 * <p>
 * To accomplish this, a <code>ViewState</code> returns a
 * <code>ViewDescriptor</code>, which contains the name of the view template
 * to render and all supporting model data needed to render it correctly. It is
 * expected that some sort of view resolver will map this view name to a
 * physical resource template (like a jsp file.)
 * <p>
 * A view state can also be a <i>marker</i> state with no associated view. In
 * this case it just returns control back to the HTTP client. Marker states are
 * useful for situations where an action has already generated the response.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ViewState extends TransitionableState {

	/**
	 * The logical name of the view.
	 */
	private String viewName;

	/**
	 * Create a new marker view state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param transition The sole transition of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public ViewState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
	}

	/**
	 * Create a new marker view state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param transitions The transitions of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public ViewState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
	}

	/**
	 * Create a new view state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param viewName The logical name of the view to render
	 * @param transition The sole transition of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public ViewState(Flow flow, String id, String viewName, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
		setViewName(viewName);
	}

	/**
	 * Create a new view state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param viewName The logical name of the view to render
	 * @param transitions The transitions of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public ViewState(Flow flow, String id, String viewName, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setViewName(viewName);
	}

	/**
	 * Returns the logical name of the view to render in this view state.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Set the logical name of the view to render in this view state.
	 */
	protected void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * Returns true if this view state has no associated view, false otherwise.
	 */
	public boolean isMarker() {
		return !StringUtils.hasText(getViewName());
	}

	/**
	 * Return a view descriptor pointing requesting front controllers to a
	 * logical view resource to be displayed. The descriptor also contains a
	 * model map needed when the view is rendered, for populating dynamic
	 * content.
	 * 
	 * @param context The flow execution stack, tracking the current active flow
	 *        session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected ViewDescriptor doEnterState(StateContext context) {
		if (isMarker()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning a view descriptor null object; no view to render");
			}
			return null;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning view name '" + viewName + "' to render");
			}
			return new ViewDescriptor(viewName, context.getModel());
		}
	}

	protected void createToString(ToStringCreator creator) {
		super.createToString(creator);
		creator.append("viewName", viewName);
	}
}