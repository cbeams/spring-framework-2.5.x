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
package org.springframework.web.flow.execution.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.execution.ExternalEvent;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

/**
 * A flow event that originated from an incoming HTTP servlet request.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ServletEvent extends ExternalEvent {

	/**
	 * The event to be signaled can also be sent using a request
	 * attribute set by an intercepting filter, with this name
	 * ("_mapped_eventId"). Use this when you can't use the "_eventId" parameter
	 * to pass in the event -- for example, when using image buttons with
	 * javascript restrictions.
	 */
	public static final String EVENT_ID_REQUEST_ATTRIBUTE = "_mapped_eventId";
	
	/**
	 * The response associated with the request that originated this event.
	 */
	private HttpServletResponse response;

	/**
	 * Construct a flow event for the specified HTTP servlet request. The default
	 * request parameter and attribute names will be used.
	 * @param request the HTTP servlet request
	 * @param response the HTTP servlet response associated with the request
	 */
	public ServletEvent(HttpServletRequest request, HttpServletResponse response) {
		this(request, response, EVENT_ID_PARAMETER, EVENT_ID_REQUEST_ATTRIBUTE, CURRENT_STATE_ID_PARAMETER, PARAMETER_VALUE_DELIMITER);
	}

	/**
	 * Construct a flow event for the specified HTTP servlet request.
	 * @param request the HTTP servlet request
	 * @param response the HTTP servlet response associated with the request
	 * @param eventIdParameterName name of the event id parameter in the request
	 * @param eventIdAttributeName name of the event id attribute in the request
	 * @param currentStateIdParameterName name of the current state id parameter
	 *        in the request
	 * @param parameterValueDelimiter delimiter used when a parameter value is
	 *        sent as part of the name of a request parameter
	 *        (e.g. "_eventId_value=bar")
	 */
	public ServletEvent(HttpServletRequest request, HttpServletResponse response,
			String eventIdParameterName, String eventIdAttributeName, String currentStateIdParameterName,
			String parameterValueDelimiter) {
		super(request);
		this.response = response;
		// initialize parameters
		setParameters(WebUtils.getParametersStartingWith(request, null));
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)getRequest();
			addParameters(multipartRequest.getFileMap());
		}
		String eventId = (String)searchForParameter(eventIdParameterName, parameterValueDelimiter);
		if (!StringUtils.hasText(eventId)) {
			// see if the eventId is set as a request attribute (put there by a
			// servlet filter)
			eventId = (String)getRequest().getAttribute(eventIdAttributeName);
		}
		setId(eventId);
		setStateId((String)getParameter(currentStateIdParameterName));
	}

	/**
	 * Returns the HTTP servlet request that originated this event.
	 */
	public HttpServletRequest getRequest() {
		return (HttpServletRequest)getSource();
	}

	/**
	 * Returns the HTTP servlet response associated with the HTTP
	 * servlet request that originated this event.
	 */
	public HttpServletResponse getResponse() {
		return response;
	}
}