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
package org.springframework.web.flow.execution.portlet;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.execution.ExternalEvent;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * A flow event that originated from an incoming portlet request.
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 * @author Erwin Vervaet
 */
public class PortletRequestEvent extends ExternalEvent {

	/**
	 * The response associated with the request that originated this event.
	 */
	private PortletResponse response;

	/**
	 * Construct a flow event for the specified portlet request. This will use
	 * the default request parameter and attribute names.
	 * @param request the portlet request
	 * @param response the portlet response associated with the request
	 */
	public PortletRequestEvent(PortletRequest request, PortletResponse response) {
		this(request, response,
				EVENT_ID_PARAMETER, HttpServletRequestEvent.EVENT_ID_REQUEST_ATTRIBUTE, CURRENT_STATE_ID_PARAMETER, PARAMETER_VALUE_DELIMITER);
	}

	/**
	 * Construct a flow event for the specified portlet request.
	 * @param request the portlet request
	 * @param response the portlet response associated with the request
	 * @param eventIdParameterName name of the event id parameter in the request
	 * @param eventIdAttributeName name of the event id attribute in the request
	 * @param currentStateIdParameterName name of the current state id parameter
	 *        in the request
	 * @param parameterValueDelimiter delimiter used when a parameter value is
	 *        sent as part of the name of a request parameter (e.g. "_eventId_value=bar")
	 */
	public PortletRequestEvent(PortletRequest request, PortletResponse response,
			String eventIdParameterName, String eventIdAttributeName, String currentStateIdParameterName,
			String parameterValueDelimiter) {
		super(request);
		this.response = response;
		// initialize parameters
		setParameters(PortletUtils.getParametersStartingWith(request, null));
		// TODO multipart portlet request support -- not yet supported by Spring Portlet MVC
		String eventId = (String)searchForParameter(eventIdParameterName, parameterValueDelimiter);
		if (!StringUtils.hasText(eventId)) {
			// see if the eventId is set as a request attribute (put there by a
			// servlet filter)
			eventId = (String) getRequest().getAttribute(eventIdAttributeName);
		}
		setId(eventId);
		setStateId((String)getParameter(currentStateIdParameterName));
	}

	/**
	 * Returns the portlet request that originated this event.
	 */
	public PortletRequest getRequest() {
		return (PortletRequest) getSource();
	}

	/**
	 * Returns the portlet response associated with the portlet request that
	 * originated this event.
	 */
	public PortletResponse getResponse() {
		return response;
	}
}