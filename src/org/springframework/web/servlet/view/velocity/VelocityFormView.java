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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.context.Context;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.BindStatusHelper;



/**
 * VelocityFormView is a simple extension of <code>VelocityView</code> that 
 * enables the implicit use of Spring's form based Velocity macros.  Note 
 * that this class is <b>not</b> required for templates that use html forms 
 * <b>unless</b> you wish to take advantage of the Spring helper macros.
 * 
 * @author Darren Davison
 * @since 1.1
 * @version $Id: VelocityFormView.java,v 1.1 2004-07-02 00:40:04 davison Exp $
 */
public class VelocityFormView extends VelocityView {

    public static final String HELPER_VARIABLE_NAME = "springBindStatusHelper";

	private String path;
	
	private BindStatusHelper helper;

    /**
     * This method will expose an object to the VelocityContext of type 
     * BindStatusHelper and be accessible in macros as $springBindStatusHelper.  
     * Principally this is used by the Spring Velocity form helper macros.
     * 
     * @see org.springframework.web.servlet.view.velocity.VelocityView#exposeHelpers
     * @see BindStatusHelper
     */
    protected void exposeHelpers(Context velocityContext, HttpServletRequest request) 
    throws Exception {
        
        if (velocityContext.containsKey(HELPER_VARIABLE_NAME)) {
            // it is an error to use this class and a pre-defined object called 'status'
            
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
                    (RequestContext) velocityContext.get(getRequestContextAttribute()));
            // expose status object for Spring macros
            velocityContext.put(HELPER_VARIABLE_NAME, helper);
        }
    }
}
