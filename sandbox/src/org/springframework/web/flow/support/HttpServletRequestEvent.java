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
package org.springframework.web.flow.support;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowConstants;

/**
 * A flow event that orginated from an incoming HTTP servlet request.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class HttpServletRequestEvent extends Event {

	/**
	 * The event timestamp.
	 */
	private long timestamp = new Date().getTime();

	/**
	 * Construct a flow event for the specified servlet request.
	 * @param request the HTTP servlet request
	 */
	public HttpServletRequestEvent(HttpServletRequest request) {
		super(request);
	}

	public HttpServletRequest getRequest() {
		return (HttpServletRequest)getSource();
	}

	public String getId() {
		try {
			//TODO remove dependency on spring mvc
			return RequestUtils.getRequiredStringParameter(getRequest(), getEventIdParameterName());
		}
		catch (ServletRequestBindingException e) {
			throw new IllegalArgumentException("The event id is not present in the request: " + e.getMessage());
		}
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getStateId() {
		return getRequest().getParameter(getCurrentStateIdParameterName());
	}

	public Object getParameter(String parameterName) {
		return getRequest().getParameter(parameterName);
	}

	public Map getParameters() {
		return getRequest().getParameterMap();
	}

	// subclassing hooks

	/**
	 * Returns the name of the event id parameter in the request ("_eventId").
	 */
	protected String getEventIdParameterName() {
		return FlowConstants.EVENT_ID_PARAMETER;
	}

	/**
	 * Returns the name of the current state id parameter in the request
	 * ("_currentStateId").
	 */
	protected String getCurrentStateIdParameterName() {
		return FlowConstants.CURRENT_STATE_ID_PARAMETER;
	}

}