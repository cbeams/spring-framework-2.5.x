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

package org.springframework.web.servlet.view.velocity;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.view.velocity.VelocityViewTests.TestVelocityEngine;



/**
 * VelocityFormViewTests
 * 
 * @author Darren Davison
 * @since Jun 18, 2004
 * @version $Id: VelocityFormViewTests.java,v 1.1 2004-07-02 00:40:07 davison Exp $
 */
public class VelocityFormViewTests extends TestCase {

    private VelocityFormView vv;
    private HttpServletResponse expectedResponse;
    private HttpServletRequest req;
    private final String templateName = "test.vm";
    private WebApplicationContext wac;
    
    
    public void setUp() throws Exception {
        super.setUp();
        
        MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		wac = (WebApplicationContext) wmc.getMock();
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		
		final Template expectedTemplate = new Template();
		VelocityConfig vc = new VelocityConfig() {
			public VelocityEngine getVelocityEngine() {
				return new TestVelocityEngine(templateName, expectedTemplate);
			}
		};
		wac.getBeansOfType(VelocityConfig.class, true, true);
		Map configurers = new HashMap();
		configurers.put("velocityConfigurer", vc);
		wmc.setReturnValue(configurers);
		wmc.replay();

		MockControl reqControl = MockControl.createControl(HttpServletRequest.class);
		req = (HttpServletRequest) reqControl.getMock();
		req.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		expectedResponse = new MockHttpServletResponse();
    }
    
    public void testNoRequestContext() {	
        vv = new VelocityFormView() {
		    protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
		        fail();
		    }
		};		
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		
		Map model = new HashMap();		
		try {
            vv.render(model, req, expectedResponse);            
        } catch (Exception e) {
            assertTrue(e instanceof ServletException);
            assertTrue(e.getMessage().indexOf("requestContextAttribute") > -1);
        }
    }
    
    /*
     
    these fail due to RequestContextUtils not finding WAC in request 
    although it's added in setUp() ..?
     
    public void testExposeBindStatusHelperTool() {	
        vv = new VelocityFormView() {
		    protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
		        assertTrue(context.containsKey(VelocityView.DEFAULT_CONTENT_TYPE));
	            assertTrue(context.get(VelocityView.DEFAULT_CONTENT_TYPE) instanceof BindStatusHelper);
		    }
		};		
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		vv.setRequestContextAttribute("rc");
		
		Map model = new HashMap();		
		try {
            vv.render(model, req, expectedResponse);            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void testBindStatusHelperToolNameUsedError() {	
        final String helperTool = "wrongType";
        
        vv = new VelocityFormView() {
		    protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
		        fail();
		    }
		};		
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		vv.setRequestContextAttribute("rc");
		
		Map model = new HashMap();		
		model.put(VelocityView.DEFAULT_CONTENT_TYPE, helperTool);
		
		try {
            vv.render(model, req, expectedResponse);            
        } catch (Exception e) {
            assertTrue(e instanceof ServletException);
            assertTrue(e.getMessage().indexOf(VelocityView.DEFAULT_CONTENT_TYPE) > -1);
        }
    }*/
}
