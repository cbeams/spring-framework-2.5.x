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
import org.springframework.web.portlet.ModelAndView;

/**
 * Interceptor to foward mapping request parameters to the render action.
 *
 * @author Rainer Schmitz
 * @author John Lewis
 * @since 1.3
 * @see ParameterHandlerMapping
 */
public class ParameterMappingInterceptor implements HandlerInterceptor {

	private ParameterHandlerMapping handlerMapping;


	/**
	 * Set the ParameterHandlerMapping containing the mapping request
	 * parameter name. If no value is provided, the default parameter mapping
	 * name {@link ParameterHandlerMapping#DEFAULT_PARAMETER_NAME} is used.
	 * @param handlerMapping The ParameterHandlerMapping to set.
	 */
	public void setHandlerMapping(ParameterHandlerMapping handlerMapping) {
		this.handlerMapping = handlerMapping;
	}


	/**
	 * If request is an {@link javax.portlet.ActionRequest ActionRequest},
	 * get handler mapping parameter and add it to the ActionResponse.
	 */
	public boolean preHandle(PortletRequest request, PortletResponse response, Object handler)
			throws Exception {

		if (request instanceof ActionRequest) {
			String parameterName = (this.handlerMapping != null ? handlerMapping.getParameterName() :
					ParameterHandlerMapping.DEFAULT_PARAMETER_NAME);
			String mappingParameter = request.getParameter(parameterName);
			if (mappingParameter != null) {
				((ActionResponse) response).setRenderParameter(parameterName, mappingParameter);
			}
		}
		return true;
	}

	public void postHandle(
			RenderRequest request, RenderResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {
	}

	public void afterCompletion(
			PortletRequest request, PortletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
