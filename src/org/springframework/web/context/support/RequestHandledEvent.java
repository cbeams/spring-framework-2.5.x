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

package org.springframework.web.context.support;

import org.springframework.context.ApplicationEvent;

/**
 * Event raised when a request is handled within a WebApplicationContext.
 * Supported by Spring's own FrameworkServlet, but can also be raised
 * by any other web component.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 17, 2001
 * @see org.springframework.web.servlet.FrameworkServlet
 */
public class RequestHandledEvent extends ApplicationEvent {

	private final String requestUrl;

	/** Request processing time */
	private final long processingTimeMillis;

	/** IP address that the request came from */
	private final String clientAddress;

	/** Usually GET or POST */
	private final String method;

	private final String servletName;

	private String sessionId;

	/** Usually the UserPrincipal */
	private String userName;

	/** Cause of failure, if any */
	private Throwable failureCause;


	/**
	 * Create a new RequestHandledEvent.
	 * @param source the component that published the event
	 * @param requestUrl the URL of the request
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param clientAddress the IP address that the request came from
	 * @param method the HTTP method of the request (usually GET or POST)
	 * @param servletName the name of the servlet that handled the request
	 */
	public RequestHandledEvent(Object source, String requestUrl, long processingTimeMillis,
														 String clientAddress, String method, String servletName) {
		super(source);
		this.requestUrl = requestUrl;
		this.processingTimeMillis = processingTimeMillis;
		this.clientAddress = clientAddress;
		this.method = method;
		this.servletName = servletName;
	}

	/**
	 * Create a new RequestHandledEvent.
	 * @param source the component that published the event
	 * @param requestUrl the URL of the request
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param clientAddress the IP address that the request came from
	 * @param method the HTTP method of the request (usually GET or POST)
	 * @param servletName the name of the servlet that handled the request
	 * @param failureCause the cause of failure, if any
	 */
	public RequestHandledEvent(Object source, String requestUrl, long processingTimeMillis,
														 String clientAddress, String method, String servletName,
														 Throwable failureCause) {
		this(source, requestUrl, processingTimeMillis, clientAddress, method, servletName);
		this.failureCause = failureCause;
	}

	/**
	 * Create a new RequestHandledEvent with session information.
	 * @param source the component that published the event
	 * @param requestUrl the URL of the request
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param clientAddress the IP address that the request came from
	 * @param method the HTTP method of the request (usually GET or POST)
	 * @param servletName the name of the servlet that handled the request
	 * @param sessionId the id of the HTTP session, if any
	 * @param userName the name of the user that was associated with the
	 * request, if any (usually the UserPrincipal)
	 */
	public RequestHandledEvent(Object source, String requestUrl, long processingTimeMillis,
														 String clientAddress, String method, String servletName,
														 String sessionId, String userName) {
		this(source, requestUrl, processingTimeMillis, clientAddress, method, servletName);
		this.sessionId = sessionId;
		this.userName = userName;
	}

	/**
	 * Create a new RequestHandledEvent with session information.
	 * @param source the component that published the event
	 * @param requestUrl the URL of the request
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param clientAddress the IP address that the request came from
	 * @param method the HTTP method of the request (usually GET or POST)
	 * @param servletName the name of the servlet that handled the request
	 * @param sessionId the id of the HTTP session, if any
	 * @param userName the name of the user that was associated with the
	 * request, if any (usually the UserPrincipal)
	 * @param failureCause the cause of failure, if any
	 */
	public RequestHandledEvent(Object source, String requestUrl, long processingTimeMillis,
														 String clientAddress, String method, String servletName,
														 String sessionId, String userName, Throwable failureCause) {
		this(source, requestUrl, processingTimeMillis, clientAddress, method, servletName, sessionId, userName);
		this.failureCause = failureCause;
	}


	/**
	 * Return the URL of the request.
	 */
	public String getRequestUrl() {
		return requestUrl;
	}

	/**
	 * Return the URL of the request.
	 * @deprecated in favor of {@link #getRequestUrl getRequestUrl}
	 */
	public String getURL() {
		return requestUrl;
	}

	/**
	 * Return the processing time of the request in milliseconds.
	 */
	public long getProcessingTimeMillis() {
		return processingTimeMillis;
	}

	/**
	 * Return the processing time of the request in milliseconds.
	 * @deprecated in favor of {@link #getProcessingTimeMillis getProcessingTimeMillis}
	 */
	public long getTimeMillis() {
		return processingTimeMillis;
	}

	/**
	 * Return the IP address that the request came from.
	 */
	public String getClientAddress() {
		return clientAddress;
	}

	/**
	 * Return the IP address that the request came from.
	 * @deprecated in favor of {@link #getClientAddress getClientAddress}
	 */
	public String getIpAddress() {
		return clientAddress;
	}

	/**
	 * Return the HTTP method of the request (usually GET or POST).
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Return the name of the servlet that handled the request.
	 */
	public String getServletName() {
		return servletName;
	}

	/**
	 * Return the id of the HTTP session, if any.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Return the name of the user that was associated with the request
	 * (usually the UserPrincipal).
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Return whether the request failed.
	 */
	public boolean wasFailure() {
		return failureCause != null;
	}

	/**
	 * Return the cause of failure, if any.
	 */
	public Throwable getFailureCause() {
		return failureCause;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer("RequestHandledEvent: ");
		sb.append("url=[").append(this.requestUrl).append("]; ");
		sb.append("time=[").append(this.processingTimeMillis).append("ms]; ");
		sb.append("client=[").append(this.clientAddress).append("]; ");
		sb.append("method=[").append(this.method).append("]; ");
		sb.append("servlet=[").append(this.servletName).append("]; ");
		sb.append("session=[").append(this.sessionId).append("]; ");
		sb.append("user=[").append(this.userName).append("]; ");
		sb.append("status=[");
		if (!wasFailure()) {
			sb.append("OK");
		}
		else {
			sb.append("failed: ").append(this.failureCause);
		}
		sb.append(']');
		return sb.toString();
	}

}
