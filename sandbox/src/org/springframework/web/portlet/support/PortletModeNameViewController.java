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

package org.springframework.web.portlet.support;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * PortletController that transforms the PortletMode to a view name. 
 * Example: PortletMode.VIEW -> "view"
 * 
 * @author William G. Thompson, Jr.
 */
public class PortletModeNameViewController implements PortletController {

    public ModelAndView handleRequest(RenderRequest request, RenderResponse response) throws Exception {
        PortletMode portletMode = request.getPortletMode();
        return new ModelAndView(portletMode.toString());
    }

    public void handleRequest(ActionRequest request, ActionResponse response) throws Exception {

    }

}
