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

package org.springframework.web.portlet.context.support;

import org.springframework.context.ApplicationEvent;

/**
 * Event raised when a portlet request is handled within a PortletApplicationContext.
 * Supported by Spring's own FrameworkPortlet, but can also be raised
 * by any other web component.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @see org.springframework.web.servlet.FrameworkPortlet
 */
public class RequestHandledEvent extends ApplicationEvent {

	/** Request processing time */
	private final long processingTimeMillis;

	private final String portletName;

	private final String portletMode;

	private final String requestType;
	
	private String sessionId;

	/** Usually the UserPrincipal */
	private String userName;

	/** Cause of failure, if any */
	private Throwable failureCause;


	/**
	 * Create a new RequestHandledEvent.
	 * @param source the component that published the event
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param portletName the name of the portlet that handled the request
	 * @param portletMode the PortletMode of the request (usually 'view', 'edit', or 'help')
	 * @param requestType the type of Portlet Request ('action' or 'render')
	 */
	public RequestHandledEvent(Object source, long processingTimeMillis,
			String portletName, String portletMode, String requestType) {
		super(source);
		this.processingTimeMillis = processingTimeMillis;
		this.portletName = portletName;
		this.portletMode = portletMode;
		this.requestType = requestType;
	}

	/**
	 * Create a new RequestHandledEvent.
	 * @param source the component that published the event
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param portletName the name of the portlet that handled the request
	 * @param portletMode the PortletMode of the request (usually 'view', 'edit', or 'help')
	 * @param requestType the type of Portlet Request ('action' or 'render')
	 * @param failureCause the cause of failure, if any
	 */
	public RequestHandledEvent(Object source, long processingTimeMillis,
	        String portletName, String portletMode, String requestType,
	        Throwable failureCause) {
		this(source, processingTimeMillis, portletName, portletMode, requestType);
		this.failureCause = failureCause;
	}

	/**
	 * Create a new RequestHandledEvent with session information.
	 * @param source the component that published the event
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param portletName the name of the portlet that handled the request
	 * @param portletMode the PortletMode of the request (usually 'view', 'edit', or 'help')
	 * @param requestType the type of Portlet Request ('action' or 'render')
	 * @param sessionId the id of the HTTP session, if any
	 * @param userName the name of the user that was associated with the
	 * request, if any (usually the UserPrincipal)
	 */
	public RequestHandledEvent(Object source, long processingTimeMillis,
	        String portletName, String portletMode, String requestType,
			String sessionId, String userName) {
		this(source, processingTimeMillis, portletName, portletMode, requestType);
		this.sessionId = sessionId;
		this.userName = userName;
	}

	/**
	 * Create a new RequestHandledEvent with session information.
	 * @param source the component that published the event
	 * @param processingTimeMillis the processing time of the request in milliseconds
	 * @param portletName the name of the portlet that handled the request
	 * @param portletMode the PortletMode of the request (usually 'view', 'edit', or 'help')
	 * @param requestType the type of Portlet Request ('action' or 'render')
	 * @param sessionId the id of the HTTP session, if any
	 * @param userName the name of the user that was associated with the
	 * request, if any (usually the UserPrincipal)
	 * @param failureCause the cause of failure, if any
	 */
	public RequestHandledEvent(Object source, long processingTimeMillis,
	        String portletName, String portletMode, String requestType,
			String sessionId, String userName, Throwable failureCause) {
		this(source, processingTimeMillis, portletName, portletMode, requestType,
		        sessionId, userName);
		this.failureCause = failureCause;
	}


	/**
	 * Return the processing time of the request in milliseconds.
	 */
	public long getProcessingTimeMillis() {
		return processingTimeMillis;
	}

	/**
	 * Return the name of the portlet that handled the request.
	 */
	public String getPortletName() {
		return portletName;
	}

	/**
	 * Return the mode of the portlet request (usually 'view', 'edit', or 'help')
	 */
	public String getPortletMode() {
		return portletMode;
	}

	/**
	 * Return the the type of Portlet Request ('action' or 'render')
	 */
	public String getRequestType() {
		return requestType;
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
		sb.append("time=[").append(this.processingTimeMillis).append("ms]; ");
		sb.append("portlet=[").append(this.portletName).append("]; ");
		sb.append("mode=[").append(this.portletMode).append("]; ");
		sb.append("type=[").append(this.requestType).append("]; ");
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
