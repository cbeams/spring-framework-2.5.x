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
 * Event raised when a request is handled by our web framework.
 * @author Rod Johnson
 * @since January 17, 2001
 */
public class RequestHandledEvent extends ApplicationEvent {

	private String url;

	private long timeMillis;

	private String ipAddress;

	/** Usually GET or POST */
	private String method;

	/** Name of the servlet that handled this request is available */
	private String servletName;

	private Throwable failureCause;

	public RequestHandledEvent(Object source, String url, long timeMillis, String ip, String method, String servletName) {
		super(source);
		this.url = url;
		this.timeMillis = timeMillis;
		this.ipAddress = ip;
		this.method = method;
		this.servletName = servletName;
	}

	public RequestHandledEvent(Object source, String url, long timeMillis, String ip, String method, String servletName, Throwable ex) {
		this(source, url, timeMillis, ip, method, servletName);
		this.failureCause = ex;
	}

	public String getURL() {
		return url;
	}

	public long getTimeMillis() {
		return timeMillis;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getMethod() {
		return method;
	}

	public String getServletName() {
		return servletName;
	}

	public boolean wasFailure() {
		return failureCause != null;
	}

	public Throwable getFailureCause() {
		return failureCause;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RequestHandledEvent: url=[" + getURL() + "] time=" + getTimeMillis() + "ms");
		sb.append(" client=" + getIpAddress() + " method='" + getMethod() + "' servlet='" + getServletName() + "'");
		sb.append(" status=" + (sb.append(!wasFailure() ? "OK" : "failed: " + getFailureCause())));
		return sb.toString();
	}

}
