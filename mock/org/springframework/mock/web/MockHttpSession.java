/*
 * Copyright 2002-2006 the original author or authors.
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.springframework.util.Assert;

/**
 * Mock implementation of the {@link javax.servlet.http.HttpSession} interface.
 *
 * <p>Used for testing the web framework; also useful for testing
 * application controllers.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public class MockHttpSession implements HttpSession {

	public static final String SESSION_COOKIE_NAME = "JSESSION";

	private static int nextId = 1;


	private final String id = Integer.toString(nextId++);

	private final long creationTime = System.currentTimeMillis();

	private int maxInactiveInterval;

	private long lastAccessedTime = System.currentTimeMillis();

	private final ServletContext servletContext;

	private final Hashtable attributes = new Hashtable();

	private boolean invalid = false;

	private boolean isNew = true;


	/**
	 * Create a new MockHttpSession with a default {@link MockServletContext}.
	 * @see MockServletContext
	 */
	public MockHttpSession() {
		this(null);
	}

	/**
	 * Create a new MockHttpSession.
	 * @param servletContext the ServletContext that the session runs in
	 */
	public MockHttpSession(ServletContext servletContext) {
		this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
	}


	public long getCreationTime() {
		return creationTime;
	}

	public String getId() {
		return id;
	}

	public void access() {
		this.lastAccessedTime = System.currentTimeMillis();
		this.isNew = false;
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setMaxInactiveInterval(int interval) {
		maxInactiveInterval = interval;
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException("getSessionContext");
	}

	public Object getAttribute(String name) {
		Assert.notNull(name, "Attribute name must not be null");
		return this.attributes.get(name);
	}

	public Object getValue(String name) {
		return getAttribute(name);
	}

	public Enumeration getAttributeNames() {
		return this.attributes.keys();
	}

	public String[] getValueNames() {
		return (String[]) this.attributes.keySet().toArray(new String[this.attributes.size()]);
	}

	public void setAttribute(String name, Object value) {
		Assert.notNull(name, "Attribute name must not be null");
		if (value != null) {
			this.attributes.put(name, value);
			if (value instanceof HttpSessionBindingListener) {
				((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
			}
		}
		else {
			removeAttribute(name);
		}
	}

	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	public void removeAttribute(String name) {
		Assert.notNull(name, "Attribute name must not be null");
		Object value = this.attributes.remove(name);
		if (value instanceof HttpSessionBindingListener) {
			((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
		}
	}

	public void removeValue(String name) {
		removeAttribute(name);
	}

	public void invalidate() {
		this.invalid = true;
		for (Iterator it = this.attributes.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String name = (String) entry.getKey();
			Object value = entry.getValue();
			it.remove();
			if (value instanceof HttpSessionBindingListener) {
				((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
			}
		}
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setNew(boolean value) {
		this.isNew = value;
	}

	public boolean isNew() {
		return isNew;
	}

}
