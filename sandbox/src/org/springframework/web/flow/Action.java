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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An action that executes controller, mediator, and/or command-like behavior.
 * Actions typically delegate down to the service-layer to perform business
 * operations, and/or prep views with dynamic model data for rendering. They act
 * the bridge between the web-tier and the middle-tier.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface Action {

	/**
	 * Execute this action. Action execution will occur in the context of the
	 * current request. Execution is typically triggered when an
	 * <code>ActionState</code> is entered in an ongoing
	 * <code>FlowExecution</code>.
	 * <p>
	 * Note: The <code>MutableAttributesAccessor</code> model argument
	 * provides access to the <b>data model </b> of the active flow session. All
	 * attributes in the flow model are considered in "flow scope"; that is,
	 * they exist for the life of the flow session and will be cleaned up when
	 * the flow session ends. All attributes in the flow model are automatically
	 * exported for convenient access by the views.
	 * <p>
	 * Note: The flow model should not be used as a general purpose cache; but
	 * rather as a context for data needed locally by the flows this action
	 * partcipates in. For example, it would be inappropriate to stuff large
	 * collections of objects (like those returned to support a search results
	 * view) into the flow model. Instead, put such result collections in the
	 * request, and ensure you execute the action again each time you wish to
	 * view those results. 2nd level caches are much better cache solutions.
	 * 
	 * @param request The current http request, enabling access to request
	 *        parameters and/or attributes if neccessary
	 * @param response The http response, enabling direct response writing by
	 *        the action if neccessary
	 * @param model The data model for the current flow session ("flow scope")
	 * @return A logical result outcome, used as grounds for a transition in the
	 *         current state
	 * @throws Exception An <b>unrecoverable </b> exception occured; note: any
	 *         recoverable exceptions should be caught and an appropriate result
	 *         outcome returned.
	 */
	public ActionResult execute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws Exception;
}