/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
