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

package org.springframework.web.servlet.tags;

import org.springframework.web.servlet.support.RequestContext;


/**
 * Helper class for view technologies unable to create their own instances
 * of BindStatus.  Initially created for Velocity and FreeMarker support
 * but can be used or extended by other view types.
 * 
 * @author Darren Davison
 * @since 1.1
 * @version $Id: BindStatusHelper.java,v 1.1 2004-07-02 00:40:05 davison Exp $
 */
public class BindStatusHelper {
    
    protected final RequestContext requestContext;
    
    public BindStatusHelper(RequestContext requestContext) {
        this.requestContext = requestContext;
    }
    
    /**
     * creates a BindStatus instance based on the path supplied.
     * 
     * @param path
     * @param htmlEscape
     * @return
     */
    public BindStatus createBindStatus(String path, boolean htmlEscape) {
        BindStatus status;
        try {
            status = new BindStatus(requestContext, path, htmlEscape);
        } catch (IllegalStateException e) {
            status = null;
        }
                
        exposeStatusToModel(status);
        return status;
    }
    
    /**
     * optional method that a subclass can override in order to expose the BindStatus
     * object in a technology-specific manner.  The default implementation does
     * nothing.
     * 
     * @param status the BindStatus object to expose
     */
    protected void exposeStatusToModel(BindStatus status) {        
    }
    
}
