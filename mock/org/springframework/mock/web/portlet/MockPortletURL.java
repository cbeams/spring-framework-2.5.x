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

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;

/**
 * Mock implementation of the PortletURL interface.
 *
 * @author John A. Lewis
 * @since 2.0
 */
public class MockPortletURL implements PortletURL {

	private static final String ENCODING = "UTF-8";


	private final String urlType;

	private WindowState windowState;

	private PortletMode portletMode;

	private final Map parameters = CollectionFactory.createLinkedMapIfPossible(16);

	private boolean secure = false;


	public MockPortletURL(String urlType) {
		this.urlType = urlType;
	}
    
    
	//---------------------------------------------------------------------
	// PortletURL methods
	//---------------------------------------------------------------------

	public void setWindowState(WindowState windowState) throws WindowStateException {
		this.windowState = windowState;
	}

	public void setPortletMode(PortletMode portletMode) throws PortletModeException {
		this.portletMode = portletMode;
	}

	public void setParameter(String name, String value) {
		Assert.notNull(name);
		Assert.notNull(value);
		this.parameters.put(name, new String[]{value});
	}

	public void setParameter(String name, String[] values) {
		Assert.notNull(name);
		Assert.notNull(values);
		this.parameters.put(name, values);
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

	public void setSecure(boolean secure) throws PortletSecurityException {
		this.secure = secure;
	}

	public String toString() {
		StringBuffer query = new StringBuffer();
		query.append(encodeParameter("urlType", this.urlType));
		if (this.windowState != null) {
			query.append(";" + encodeParameter("windowState", this.windowState.toString()));
		}
		if (this.portletMode != null) {
			query.append(";" + encodeParameter("portletMode", this.portletMode.toString()));
		}
		for (Iterator it = this.parameters.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String name = (String) entry.getKey();
			String[] values = (String[]) entry.getValue();
			query.append(";" + encodeParameter("param_" + name, values));
		}
		return (this.secure ? "https:" : "http:") +
				"//localhost/mockportlet?" + query.toString();
	}


	//---------------------------------------------------------------------
	// MockPortletURL methods
	//---------------------------------------------------------------------

	private String encodeParameter(String name, String value) {
		try {
			return URLEncoder.encode(name, ENCODING) + "=" +
					URLEncoder.encode(value, ENCODING);
		}
		catch (Exception ex) {
			return null;
		}
	}

	private String encodeParameter(String name, String[] values) {
		try {
			StringBuffer buf = new StringBuffer();
			for (int i = 0, n = values.length; i < n; i++) {
				buf.append((i > 0 ? ";" : "") +
						URLEncoder.encode(name, ENCODING) + "=" +
						URLEncoder.encode(values[i], ENCODING));
			}
			return buf.toString();
		}
		catch (Exception ex) {
			return null;
		}
	}
}
