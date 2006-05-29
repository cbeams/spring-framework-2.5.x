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

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.ModelAndView;

/**
 * Abstract adapter class for the HandlerInterceptor interface,
 * for simplified implementation of pre-only/post-only interceptors.
 *
 * @author John A. Lewis
 * @since 2.0
 */
public abstract class HandlerInterceptorAdapter extends ApplicationObjectSupport
	implements HandlerInterceptor {

	/**
	 * This implementation always returns true.
	 */
	public boolean preHandle(PortletRequest request, PortletResponse response, Object handler) throws Exception {
		return true;
	}

	/**
	 * This implementation is empty.
	 */
	public void postHandle(RenderRequest request, RenderResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {
	}
	
	/**
	 * This implementation is empty.
	 */
	public void afterCompletion(PortletRequest request, PortletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
