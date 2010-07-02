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

package org.springframework.web.servlet.support;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.TestCase;

import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/**
 * Unit test for RequestParamNameToRequestAttributeFilter
 *  
 * @author Colin Sampaleanu
 */
public class RequestParamNameToRequestAttributeFilterTest extends TestCase {
    
    public void testBasicOperation() throws Exception {
        
        MockServletContext context = new MockServletContext();
        
        MockFilterConfig config = new MockFilterConfig(context);
        config.addInitParameter("inputNamePrefix", "_pname_");
        config.addInitParameter("inputValuePrefix", "_pvalue_");
        
        RequestParamNameToRequestAttributeFilter filter = new RequestParamNameToRequestAttributeFilter();
        filter.init(config);
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        // match an image button
        req.addParameter("_pname_eventid_pvalue_submit.x", "whatever");
        req.addParameter("_pname_eventid_pvalue_submit.y", "whatever");
        req.addParameter("_pname_action_pvalue_reset.x", "whatever");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter.doFilter(req, resp, new MockFilterChain());
        
        assertTrue(enumSize(req.getAttributeNames()) == 2);
        assertTrue("submit".equals(req.getAttribute("eventid")));
        assertTrue("reset".equals(req.getAttribute("action")));
        
        config = new MockFilterConfig(context);
        config.addInitParameter("inputNamePrefix", "_pname_");
        config.addInitParameter("inputValuePrefix", "_pvalue_");
        config.addInitParameter("inputSuffixToStrip", "");
        config.addInitParameter("outputAttributeNamePrefix", "_mapped_");
        
        filter = new RequestParamNameToRequestAttributeFilter();
        filter.init(config);

        req = new MockHttpServletRequest();
        // match an image button
        req.addParameter("_pname_eventid_pvalue_submit", "whatever");
        req.addParameter("_pname_action_pvalue_reset", "whatever");
        resp = new MockHttpServletResponse();

        filter.doFilter(req, resp, new MockFilterChain());
        
        assertTrue(enumSize(req.getAttributeNames()) == 2);
        assertTrue("submit".equals(req.getAttribute("_mapped_eventid")));
        assertTrue("reset".equals(req.getAttribute("_mapped_action")));
    }
    
    public static int enumSize(Enumeration e) {
        int size = 0;
        while (e.hasMoreElements()) {
            size++;
            e.nextElement();
        }
        return size;
    }
    
    public static class MockFilterChain implements FilterChain {
        public void doFilter(ServletRequest arg0, ServletResponse arg1) throws IOException, ServletException {
            return;
        }
    }
    
}

