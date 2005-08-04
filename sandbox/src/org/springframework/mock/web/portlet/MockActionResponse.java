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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;

/**
 * Mock implementation of the ActionResponse interface.
 * 
 * @author John A. Lewis
 */
public class MockActionResponse extends MockPortletResponse implements ActionResponse {

    private WindowState windowState;
    
    private PortletMode portletMode;
    
    private String redirectedUrl;

	private final Map renderParameters = CollectionFactory.createLinkedMapIfPossible(16);

	//---------------------------------------------------------------------
	// ActionResponse methods
	//---------------------------------------------------------------------

    public void setWindowState(WindowState windowState) throws WindowStateException {
        if (this.redirectedUrl != null)
            throw new IllegalStateException ("cannot set windowState after sendRedirect has been called");
        this.windowState = windowState;
    }

    public void setPortletMode(PortletMode portletMode) throws PortletModeException {
        if (this.redirectedUrl != null)
            throw new IllegalStateException ("cannot set portletMode after sendRedirect has been called");
        this.portletMode = portletMode;
    }
    
	public void sendRedirect(String location) throws IOException {
        if (this.windowState != null || this.portletMode != null || ! this.renderParameters.isEmpty())
            throw new IllegalStateException ("cannot call sendRedirect after windowState, portletMode, or renderParameters have been set");
        Assert.notNull(location);
        try {
            new URL(location);
        } catch (MalformedURLException ex) {
            if (! location.startsWith("/"))
                throw new IllegalArgumentException("redirect URL must be valid and absolute");
        }
        this.redirectedUrl = location;
	}

    public void setRenderParameters(Map parameters) {
        if (this.redirectedUrl != null)
            throw new IllegalStateException ("cannot set renderParameters after sendRedirect has been called");
        Assert.notNull(parameters);
        for (Iterator it = parameters.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            Assert.notNull(entry.getKey());
            Assert.notNull(entry.getValue());
        }
        this.renderParameters.clear();
        for (Iterator it = parameters.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            this.renderParameters.put(entry.getKey(), entry.getValue());
        }
    }
    
    public void setRenderParameter(String key, String value) {
        if (this.redirectedUrl != null)
            throw new IllegalStateException ("cannot set renderParameters after sendRedirect has been called");
        Assert.notNull(key);
        Assert.notNull(value);
        this.renderParameters.put(key, new String[] {value});
    }
    
    public void setRenderParameter(String key, String[] values) {
        if (this.redirectedUrl != null)
            throw new IllegalStateException ("cannot set renderParameters after sendRedirect has been called");
        Assert.notNull(key);
        Assert.notNull(values);
        this.renderParameters.put(key, values);
    }

    
	//---------------------------------------------------------------------
	// MockActionResponse methods
	//---------------------------------------------------------------------

    public WindowState getWindowState() {
        return windowState;
    }
    
    public PortletMode getPortletMode() {
        return portletMode;
    }

    public String getRedirectedUrl() {
		return redirectedUrl;
	}

	public String getRenderParameter(String name) {
		String[] arr = (String[]) this.renderParameters.get(name);
		return (arr != null && arr.length > 0 ? arr[0] : null);
	}

	public String[] getRenderParameterValues(String name) {
		return (String[]) this.renderParameters.get(name);
	}

	public Enumeration getRenderParameterNames() {
		return Collections.enumeration(this.renderParameters.keySet());
	}

	public Map getRenderParameterMap() {
		return Collections.unmodifiableMap(this.renderParameters);
	}

}
