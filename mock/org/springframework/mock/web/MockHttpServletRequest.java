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

package org.springframework.mock.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Mock implementation of the HttpServletRequest interface.
 *
 * <p>Used for testing the web framework; also useful
 * for testing application controllers.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class MockHttpServletRequest implements HttpServletRequest, Serializable {

	public static final String DEFAULT_PROTOCOL = "http";

	public static final String DEFAULT_SERVER_NAME = "localhost";

	public static final int DEFAULT_SERVER_PORT = 80;

	public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

	public static final String DEFAULT_REMOTE_HOST = "localhost";


	//---------------------------------------------------------------------
	// ServletRequest properties
	//---------------------------------------------------------------------

	private final Hashtable attributes = new Hashtable();

	private String characterEncoding;

	private byte[] content;

	private String contentType;

	private final Hashtable	parameters = new Hashtable();

	private String protocol = DEFAULT_PROTOCOL;

	private String scheme = DEFAULT_PROTOCOL;

	private String serverName = DEFAULT_SERVER_NAME;

	private int serverPort = DEFAULT_SERVER_PORT;

	private String remoteAddr = DEFAULT_REMOTE_ADDR;

	private String remoteHost = DEFAULT_REMOTE_HOST;

	/** List of locales in descending order */
	private final Vector locales = new Vector();

	private boolean secure = false;

	private final ServletContext servletContext;


	//---------------------------------------------------------------------
	// HttpServletRequest properties
	//---------------------------------------------------------------------

	private String authType;

	private Cookie[] cookies;

	private final Hashtable headers = new Hashtable();

	private String method;

	private String pathInfo;

	private String contextPath = "";

	private String queryString;

	private String remoteUser;

	private final Set	roles = new HashSet();

	private Principal userPrincipal;

	private String requestURI = "";

	private String servletPath = "";

	private HttpSession session;

	private boolean requestedSessionIdValid = true;

	private boolean requestedSessionIdFromCookie = true;

	private boolean requestedSessionIdFromURL = false;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new MockHttpServletRequest.
	 * @param servletContext the ServletContext that the request runs in
	 */
	public MockHttpServletRequest(ServletContext servletContext) {
		this.locales.add(Locale.ENGLISH);
		this.servletContext = servletContext;
	}

	/**
	 * Create a new MockHttpServletRequest.
	 * @param servletContext the ServletContext that the request runs in
	 * @param method the request method
	 * @param requestURI the request URI
	 * @see #setMethod
	 * @see #setRequestURI
	 */
	public MockHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
		this(servletContext);
		this.method = method;
		this.requestURI = requestURI;
	}

	/**
	 * Create a new MockHttpServletRequest with a MockServletContext.
	 * @see MockServletContext
	 */
	public MockHttpServletRequest() {
		this(new MockServletContext());
	}

	/**
	 * Create a new MockHttpServletRequest with a MockServletContext.
	 * @param method the request method
	 * @param requestURI the request URI
	 * @see #setMethod
	 * @see #setRequestURI
	 * @see MockServletContext
	 */
	public MockHttpServletRequest(String method, String requestURI) {
		this(new MockServletContext(), method, requestURI);
	}


	//---------------------------------------------------------------------
	// ServletRequest interface
	//---------------------------------------------------------------------

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		return this.attributes.keys();
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getContentLength() {
		return (this.content != null ? content.length : -1);
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public ServletInputStream getInputStream() {
		if (this.content != null) {
			return new DelegatingServletInputStream(new ByteArrayInputStream(this.content));
		}
		else {
			return null;
		}
	}

	public void addParameter(String name, String value) {
		this.parameters.put(name, value);
	}

	public void addParameter(String name, String[] values) {
		this.parameters.put(name, values);
	}

	public String getParameter(String name) {
		Object obj = this.parameters.get(name);
		if (obj instanceof String[]) {
			String[] arr = ((String[]) obj);
			return (arr.length > 0 ? arr[0] : null);
		}
		else {
			return (obj != null ? obj.toString() : null);
		}
	}

	public Enumeration getParameterNames() {
		return this.parameters.keys();
	}

	public String[] getParameterValues(String name) {
		Object obj = this.parameters.get(name);
		if (obj instanceof String[]) {
			return (String[]) obj;
		}
		else {
			return (obj != null ? new String[] {obj.toString()} : null);
		}
	}

	public Map getParameterMap() {
		return new HashMap(this.parameters);
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getScheme() {
		return scheme;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getServerPort() {
		return serverPort;
	}

	public BufferedReader getReader() throws UnsupportedEncodingException {
		if (this.content != null) {
			InputStream sourceStream = new ByteArrayInputStream(this.content);
			Reader sourceReader = (this.characterEncoding != null) ?
			    new InputStreamReader(sourceStream, this.characterEncoding) : new InputStreamReader(sourceStream);
			return new BufferedReader(sourceReader);
		}
		else {
			return null;
		}
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	/**
	 * Add a new preferred locale, before any existing locales.
	 */
	public void addPreferredLocale(Locale locale) {
		this.locales.add(0, locale);
	}

	public Locale getLocale() {
		return (Locale) this.locales.get(0);
	}

	public Enumeration getLocales() {
		return this.locales.elements();
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isSecure() {
		return secure;
	}

	public RequestDispatcher getRequestDispatcher(String url) {
		return this.servletContext.getRequestDispatcher(url);
	}

	public String getRealPath(String path) {
		return this.servletContext.getRealPath(path);
	}


	//---------------------------------------------------------------------
	// HttpServletRequest interface
	//---------------------------------------------------------------------

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthType() {
		return authType;
	}

	public void setCookies(Cookie[] cookies) {
		this.cookies = cookies;
	}

	public Cookie[] getCookies() {
		return cookies;
	}

	public void addHeader(String name, Object value) {
		this.headers.put(name, value);
	}

	public long getDateHeader(String name) {
		Object value = this.headers.get(name);
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		else {
			return -1L;
		}
	}

	public String getHeader(String name) {
		Object value = this.headers.get(name);
		return (value != null ? value.toString() : null);
	}

	public Enumeration getHeaders(String name) {
		Object obj = this.headers.get(name);
		if (obj instanceof String[]) {
			return Collections.enumeration(Arrays.asList((String[]) obj));
		}
		else {
			Vector vector = new Vector();
			vector.add(obj.toString());
			return vector.elements();
		}
	}

	public Enumeration getHeaderNames() {
		return this.headers.keys();
	}

	public int getIntHeader(String name) {
		Object value = this.headers.get(name);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		else {
			return -1;
		}
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public String getPathTranslated() {
		return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void addRole(String role) {
		this.roles.add(role);
	}

	public boolean isUserInRole(String role) {
		return this.roles.contains(role);
	}

	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	public Principal getUserPrincipal() {
		return userPrincipal;
	}

	public String getRequestedSessionId() {
		HttpSession session = this.getSession();
		return (session != null ? session.getId() : null);
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer(this.scheme);
		url.append("://").append(this.serverName).append(':').append(this.serverPort);
		url.append(getRequestURI());
		return url;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setSession(HttpSession session) {
		this.session = session;
		if (session instanceof MockHttpSession) {
			MockHttpSession mockSession = ((MockHttpSession) session);
			mockSession.access();
		}
	}

	public HttpSession getSession(boolean create) {
		if (this.session == null && create) {
			this.session = new MockHttpSession(this.servletContext);
		}
		return this.session;
	}

	public HttpSession getSession() {
		return getSession(true);
	}

	public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
		this.requestedSessionIdValid = requestedSessionIdValid;
	}

	public boolean isRequestedSessionIdValid() {
		return this.requestedSessionIdValid;
	}

	public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
		this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return this.requestedSessionIdFromCookie;
	}

	public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
		this.requestedSessionIdFromURL = requestedSessionIdFromURL;
	}

	public boolean isRequestedSessionIdFromURL() {
		return this.requestedSessionIdFromURL;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

}
