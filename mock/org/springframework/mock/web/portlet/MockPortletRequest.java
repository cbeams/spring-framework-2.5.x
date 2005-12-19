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

package org.springframework.mock.web.portlet;

import java.lang.reflect.Array;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.portlet.PortalContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;

/**
 * Mock implementation of the PortletRequest interface.
 *
 * @author John A. Lewis
 * @since 2.0
 */
public class MockPortletRequest implements PortletRequest {

	private final PortalContext portalContext;

	private final PortletContext portletContext;

	private PortletSession session = null;

	private WindowState windowState = WindowState.NORMAL;

	private PortletMode portletMode = PortletMode.VIEW;

	private PortletPreferences portletPreferences = new MockPortletPreferences();

	private final Hashtable properties = new Hashtable();

	private final Hashtable attributes = new Hashtable();

	private final Map parameters = CollectionFactory.createLinkedMapIfPossible(16);

	private String authType = null;

	private String contextPath = "";

	private String remoteUser = null;

	private Principal userPrincipal = null;

	private final Set userRoles = new HashSet();

	private boolean secure = false;

	private boolean requestedSessionIdValid = true;

	private final Vector responseContentTypes = new Vector();

	private final Vector locales = new Vector();

	private String scheme = "http";

	private String serverName = "localhost";

	private int serverPort = 80;


	/**
	 * Create a new MockPortletRequest.
	 *
	 * @param portalContext the PortalContext that the request runs in
	 * @param portletContext the PortletContext that the request runs in
	 */
	public MockPortletRequest(PortalContext portalContext, PortletContext portletContext) {
		this.portalContext = portalContext;
		this.portletContext = portletContext;
		this.responseContentTypes.add("text/html");
		this.locales.add(Locale.ENGLISH);
	}

	/**
	 * Create a new MockPortletRequest.
	 *
	 * @param portletContext the PortletContext that the request runs in
	 */
	public MockPortletRequest(PortletContext portletContext) {
		this(new MockPortalContext(), portletContext);
	}

	/**
	 * Create a new MockPortletRequest with a MockPortletContext.
	 *
	 * @see MockPortletContext
	 */
	public MockPortletRequest() {
		this(new MockPortletContext());
	}

	
	//---------------------------------------------------------------------
	// PortletRequest methods
	//---------------------------------------------------------------------

	public boolean isWindowStateAllowed(WindowState state) {
		if (state == null) {
			return false;
		}
		Enumeration states = this.portalContext.getSupportedWindowStates();
		while (states.hasMoreElements()) {
			if (state.equals(states.nextElement())) {
				return true;
			}
		}
		return false;
	}

	public boolean isPortletModeAllowed(PortletMode mode) {
		if (mode == null) {
			return false;
		}
		Enumeration modes = this.portalContext.getSupportedPortletModes();
		while (modes.hasMoreElements()) {
			if (mode.equals(modes.nextElement())) {
				return true;
			}
		}
		return false;
	}

	public PortletMode getPortletMode() {
		return this.portletMode;
	}

	public WindowState getWindowState() {
		return this.windowState;
	}

	public PortletPreferences getPreferences() {
		return this.portletPreferences;
	}

	public PortletSession getPortletSession() {
		return getPortletSession(true);
	}

	public PortletSession getPortletSession(boolean create) {
		// Reset session if invalidated.
		if (this.session instanceof MockPortletSession && ((MockPortletSession) this.session).isInvalid()) {
			this.session = null;
		}
		// Create new session if necessary.
		if (this.session == null && create) {
			this.session = new MockPortletSession(this.portletContext);
		}
		return this.session;
	}

	public String getProperty(String name) {
		Assert.notNull(name, "name must not be null");
		Object value = this.properties.get(name);
		if (value instanceof List) {
			List list = (List) value;
			if (list.size() < 1) {
				return null;
			}
			Object element = list.get(0);
			return (element != null ? element.toString() : null);
		}
		return (value != null ? value.toString() : null);
	}

	public Enumeration getProperties(String name) {
		Assert.notNull(name, "name must not be null");
		Object value = this.properties.get(name);
		if (value instanceof List) {
			return Collections.enumeration((List) value);
		}
		else if (value != null) {
			Vector vector = new Vector(1);
			vector.add(value.toString());
			return vector.elements();
		}
		else {
			return Collections.enumeration(Collections.EMPTY_SET);
		}
	}

	public Enumeration getPropertyNames() {
		return this.properties.keys();
	}

	public PortalContext getPortalContext() {
		return this.portalContext;
	}

	public String getAuthType() {
		return this.authType;
	}

	public String getContextPath() {
		return this.contextPath;
	}

	public String getRemoteUser() {
		return this.remoteUser;
	}

	public Principal getUserPrincipal() {
		return this.userPrincipal;
	}

	public boolean isUserInRole(String role) {
		return this.userRoles.contains(role);
	}

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		return this.attributes.keys();
	}

	public String getParameter(String name) {
		String[] arr = (String[]) this.parameters.get(name);
		return (arr != null && arr.length > 0 ? arr[0] : null);
	}

	public Enumeration getParameterNames() {
		return Collections.enumeration(this.parameters.keySet());
	}

	public String[] getParameterValues(String name) {
		return (String[]) this.parameters.get(name);
	}

	public Map getParameterMap() {
		return Collections.unmodifiableMap(this.parameters);
	}

	public boolean isSecure() {
		return this.secure;
	}

	public void setAttribute(String name, Object value) {
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public String getRequestedSessionId() {
		PortletSession session = this.getPortletSession();
		return (session != null ? session.getId() : null);
	}

	public boolean isRequestedSessionIdValid() {
		return this.requestedSessionIdValid;
	}

	public String getResponseContentType() {
		return (String) this.responseContentTypes.get(0);
	}

	public Enumeration getResponseContentTypes() {
		return this.responseContentTypes.elements();
	}

	public Locale getLocale() {
		return (Locale) this.locales.get(0);
	}

	public Enumeration getLocales() {
		return this.locales.elements();
	}

	public String getScheme() {
		return scheme;
	}

	public String getServerName() {
		return serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	
	//---------------------------------------------------------------------
	// MockPortletRequest methods
	//---------------------------------------------------------------------

	public void setSession(PortletSession session) {
		this.session = session;
		if (session instanceof MockPortletSession) {
			MockPortletSession mockSession = ((MockPortletSession) session);
			mockSession.access();
		}
	}

	public void setPortletMode(PortletMode portletMode) {
		this.portletMode = portletMode;
	}

	public void setWindowState(WindowState windowState) {
		this.windowState = windowState;
	}

	public void setPreferences(PortletPreferences preferences) {
		this.portletPreferences = preferences;
	}

	/**
	 * Add a property entry for the given name.
	 * <p>If there was no entry for that property name before,
	 * the value will be used as-is. In case of an existing entry,
	 * a List will be created, adding the given value (more
	 * specifically, its toString representation) as further element.
	 * <p>Multiple values can only be stored as list of Strings,
	 * following the Servlet spec (see <code>getHeaders</code> accessor).
	 * As alternative to repeated <code>addHeader</code> calls for
	 * individual elements, you can use a single call with an entire
	 * array or Collection of values as parameter.
	 *
	 * @see #getProperty
	 * @see #getProperties
	 * @see #getPropertyNames
	 */
	public void addProperty(String name, Object value) {
		Assert.notNull(name, "name must not be null");
		Assert.notNull(value, "value must not be null");
		Object oldValue = this.properties.get(name);
		if (oldValue instanceof List) {
			List list = (List) oldValue;
			addPropertyValue(list, value);
		}
		else if (oldValue != null) {
			List list = new LinkedList();
			list.add(oldValue);
			addPropertyValue(list, value);
			this.properties.put(name, list);
		}
		else if (value instanceof Collection || value.getClass().isArray()) {
			List list = new LinkedList();
			addPropertyValue(list, value);
			this.properties.put(name, list);
		}
		else {
			this.properties.put(name, value);
		}
	}

	private void addPropertyValue(List list, Object value) {
		if (value instanceof Collection) {
			Collection valueColl = (Collection) value;
			for (Iterator it = valueColl.iterator(); it.hasNext();) {
				Object element = it.next();
				Assert.notNull("Value collection must not contain null elements");
				list.add(element.toString());
			}
		}
		else if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				Assert.notNull("Value collection must not contain null elements");
				list.add(element.toString());
			}
		}
		else {
			list.add(value);
		}
	}

	public void setParameters(Map parameters) {
		Assert.notNull(parameters);
		for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Assert.notNull(entry.getKey());
			Assert.notNull(entry.getValue());
		}
		this.parameters.clear();
		for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			this.parameters.put(entry.getKey(), entry.getValue());
		}
	}

	public void setParameter(String key, String value) {
		Assert.notNull(key);
		Assert.notNull(value);
		this.parameters.put(key, new String[]{value});
	}

	public void setParameter(String key, String[] values) {
		Assert.notNull(key);
		Assert.notNull(values);
		this.parameters.put(key, values);
	}

	public void addParameter(String name, String value) {
		addParameter(name, new String[]{value});
	}

	public void addParameter(String name, String[] values) {
		String[] oldArr = (String[]) this.parameters.get(name);
		if (oldArr != null) {
			String[] newArr = new String[oldArr.length + values.length];
			System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
			System.arraycopy(values, 0, newArr, oldArr.length, values.length);
			this.parameters.put(name, newArr);
		}
		else {
			this.parameters.put(name, values);
		}
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	public void addUserRole(String role) {
		this.userRoles.add(role);
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
		this.requestedSessionIdValid = requestedSessionIdValid;
	}

	public void addResponseContentType(String responseContentType) {
		this.responseContentTypes.add(responseContentType);
	}

	public void addPResponseContentType(String responseContentType) {
		this.responseContentTypes.add(0, responseContentType);
	}

	public void addLocale(Locale locale) {
		this.locales.add(locale);
	}

	public void addPreferredLocale(Locale locale) {
		this.locales.add(0, locale);
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

}
