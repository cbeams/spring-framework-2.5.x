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

package org.springframework.web.servlet.view.freemarker;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.BindStatusHelper;


/**
 * FreeMarkerFormView is a simple extension of <code>FreeMarkerView</code> that 
 * enables the implicit use of Spring's form based FreeMarker macros.  Note 
 * that this class is <b>not</b> required for templates that use html forms 
 * <b>unless</b> you wish to take advantage of the Spring helper macros.
 * 
 * @author Darren Davison
 * @since 1.1
 * @version $Id: FreeMarkerFormView.java,v 1.2 2004-07-02 00:49:09 davison Exp $
 */
public class FreeMarkerFormView extends FreeMarkerView {

    public static final String HELPER_VARIABLE_NAME = "springBindStatusHelper";

	private String path;
	
	private BindStatusHelper helper;

    /**
     * This method will expose an object to the FreeMarker model of type 
     * BindStatusHelper and be accessible in macros as $springBindStatusHelper.  
     * Principally this is used by the Spring FreeMarker form helper macros.
     * 
     * @see BindStatusHelper
     * @see org.springframework.web.servlet.view.freemarker.FreeMarkerView#exposeHelpers
     */
    protected void exposeHelpers(Map model, HttpServletRequest request)
            throws Exception {
        if (model.containsKey(HELPER_VARIABLE_NAME)) {            
            String msg = "Attempt to use [" + getClass().getName() + 
	                "] with a context object named [" + 
	                HELPER_VARIABLE_NAME + "].";
            logger.error(msg);
            
            // the error will cause any calls to spring macros to throw exceptions - we
            // may as well fail now.
            throw new ServletException(msg);
            
        } else if (getRequestContextAttribute() == null) {
            throw new ServletException("RequestContext must be exposed to use [" + getClass().getName() +
            		"].  Ensure you set the bean property [requestContextAttribute] on this view");
            		
        } else {
            helper = new BindStatusHelper(
                    (RequestContext) model.get(getRequestContextAttribute()));
            // expose status object for Spring macros
            model.put(HELPER_VARIABLE_NAME, helper);
        }
    }
}
