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
 * A action that executes controller, command-like behavior. Actions
 * typically delegate down to the service-layer to perform business operations,
 * and/or prep views with dynamic model data for rendering.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface Action {

	/**
	 * Execute this action.
	 * 
	 * @param request The current http request, enabling access to request
	 *        attributes/parameters if neccessary
	 * @param response The http response, enabling direct response writing by
	 *        the action if neccessary
	 * @param model The data model for the current flow session
	 * @return A logical result outcome, used as grounds for a transition in the
	 *         current state
	 * @throws Exception An unrecoverable exception
	 */
	public ActionResult execute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws Exception;
}