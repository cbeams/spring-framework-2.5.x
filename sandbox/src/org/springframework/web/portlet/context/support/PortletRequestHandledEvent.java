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

package org.springframework.web.portlet.context.support;

import org.springframework.context.ApplicationEvent;

/**
 * Event raised when a request is handled within a PortletApplicationContext.
 * Supported by Spring's own FrameworkPortlet, but can also be raised
 * by any other web component.
 * @author Rod Johnson
 * @see org.springframework.web.portlet.FrameworkPortlet
 */
public class PortletRequestHandledEvent extends ApplicationEvent {

	private long timeMillis;

	/** Usually GET or POST */
	// TODO could get PortletMode or ActionRequest vs RenderRequest
	//private String method;

	/** User associated with this request **/
	private String remoteUser;
	
	/** Name of the portlet that handled this request is available */
	private String portletName;

	/** Cause of failure, if any */
	private Throwable failureCause;

	public PortletRequestHandledEvent(Object source, String remoteUser, long timeMillis, String portletName) {
		super(source);
		this.remoteUser = remoteUser;
		this.timeMillis = timeMillis;
		this.portletName = portletName;
	}

	public PortletRequestHandledEvent(Object source, String remoteUser, long timeMillis, String portletName, Throwable ex) {
		this(source, remoteUser, timeMillis, portletName);
		this.failureCause = ex;
	}

	public long getTimeMillis() {
		return timeMillis;
	}

	// TODO could get PortletMode for a RenderRequest
//	public String getMethod() {
//		return method;
//	}

	public String getPortletName() {
		return portletName;
	}

	public String getRemoteUser() {
	    return remoteUser;
	}
	
	public boolean wasFailure() {
		return failureCause != null;
	}

	public Throwable getFailureCause() {
		return failureCause;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("PortletRequestHandledEvent: remoteUser=[");
		sb.append(getRemoteUser()).append("] time=").append(getTimeMillis()).append("ms");
		sb.append(" portlet='").append(getPortletName()).append("'");
		sb.append(" status=");
		if (!wasFailure()) {
			sb.append("OK");
		}
		else {
			sb.append("failed: ").append(getFailureCause());
		}
		return sb.toString();
	}

}
