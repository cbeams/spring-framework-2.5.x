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
package org.springframework.web.portlet.handler;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to foward mapping request parameters to the render action.
 *
 * @author Rainer Schmitz
 * @author John Lewis
 * @see ParameterHandlerMapping
 */
public class ParameterMappingInterceptor implements HandlerInterceptor {

    ParameterHandlerMapping handlerMapping;

    /**
     * Return the ParameterHandlerMapping containing the mapping request
     * parameter name.
     *
     * @return Returns the handlerMapping.
     */
    public ParameterHandlerMapping getHandlerMapping() {
        return handlerMapping;
    }

    /**
     * Set the ParameterHandlerMapping containing the mapping request
     * parameter name. If no value is provided, the default parameter mapping
     * name {@link ParameterHandlerMapping#DEFAULT_PARAMETER_NAME} is used.
     *
     * @param handlerMapping The ParameterHandlerMapping to set.
     */
    public void setHandlerMapping(ParameterHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * If request is an {@link javax.portlet.ActionRequest ActionRequest} get
     * handler mapping parameter and add it to the ActionResponse.
     *
     * @see org.springframework.web.portlet.HandlerInterceptor#preHandle
     */
    public boolean preHandle(PortletRequest request, PortletResponse response,
            Object handler) throws Exception {
        if (request instanceof ActionRequest) {
            String parameterName = (handlerMapping == null) ? ParameterHandlerMapping.DEFAULT_PARAMETER_NAME
                    : handlerMapping.getParameterName();
            String mappingParameter = request.getParameter(parameterName);
            if (mappingParameter != null) {
                ((ActionResponse)response).setRenderParameter(parameterName, mappingParameter);
            }

        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.web.portlet.HandlerInterceptor#postHandle
     */
    public void postHandle(RenderRequest request, RenderResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.web.portlet.HandlerInterceptor#afterCompletion
     */
    public void afterCompletion(PortletRequest request, PortletResponse response,
            Object handler, Exception ex) throws Exception {
    }

}