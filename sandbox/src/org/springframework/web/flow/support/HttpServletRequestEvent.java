/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;

public class HttpServletRequestEvent extends AbstractEvent {

	private HttpServletRequest request;

	public HttpServletRequestEvent(HttpServletRequest request) {
		Assert.notNull(request);
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	public String getId() {
		return request.getParameter("eventId");
	}

	public String getStateId() {
		return request.getParameter("stateId");
	}

	public Object getParameter(String parameterName) {
		return request.getParameter(parameterName);
	}

	public Map getParameters() {
		return request.getParameterMap();
	}
}