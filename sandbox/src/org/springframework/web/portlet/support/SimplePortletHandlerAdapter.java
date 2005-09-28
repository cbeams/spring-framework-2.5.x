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

package org.springframework.web.portlet.support;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adapter to delegate out to standard JSR-168 Portlets from the generic DispatcherPortlet.
 * This may be useful so that pre-existing portlets can be used with an overall Spring
 * configuration.  This will allow HandlerInteceptors and HandlerExceptionResovlers to be 
 * applied.  
 *
 * <p>This is an SPI class, not used directly by application code.
 *
 * @author Rod Johnson
 * @author John Lewis
 * @see org.springframework.web.portlet.DispatcherPortlet
 * @see Portlet
 */
public class SimplePortletHandlerAdapter implements HandlerAdapter {
	
	public boolean supports(Object handler) {
		return (handler instanceof Portlet);
	}
	
	public ModelAndView handleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
		((Portlet) handler).render(request, response);
		return null;
	}
	
	public void handleAction(ActionRequest request, ActionResponse response, Object handler) throws Exception {
		((Portlet) handler).processAction(request, response);
	}
	

}
