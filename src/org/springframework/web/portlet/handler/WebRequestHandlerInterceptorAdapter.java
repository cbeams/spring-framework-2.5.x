/*
 * Copyright 2002-2006 the original author or authors.
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

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.util.Assert;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.context.PortletWebRequest;

/**
 * Adapter that implements the Portlet HandlerInterceptor interface
 * and wraps an underlying WebRequestInterceptor.
 *
 * <p><b>NOTE:</b> The WebRequestInterceptor is only applied to the Portlet <b>render</b>
 * phase, which is dealing with preparing and rendering a Portlet view.
 * The Portlet action phase <i>cannot</i> be intercepted with the WebRequestInterceptor
 * mechanism; use the Portlet-specific HandlerInterceptor mechanism for such needs.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.context.request.WebRequestInterceptor
 * @see org.springframework.web.portlet.HandlerInterceptor
 */
public class WebRequestHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

	private final WebRequestInterceptor requestInterceptor;


	/**
	 * Create a new WebRequestHandlerInterceptorAdapter for the given WebRequestInterceptor.
	 * @param requestInterceptor the WebRequestInterceptor to wrap
	 */
	public WebRequestHandlerInterceptorAdapter(WebRequestInterceptor requestInterceptor) {
		Assert.notNull(requestInterceptor, "WebRequestInterceptor must not be null");
		this.requestInterceptor = requestInterceptor;
	}


	public boolean preHandleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
		this.requestInterceptor.preHandle(new PortletWebRequest(request));
		return true;
	}

	public void postHandleRender(RenderRequest request, RenderResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		this.requestInterceptor.postHandle(new PortletWebRequest(request),
				(modelAndView != null ? modelAndView.getModelMap() : null));
	}

	public void afterRenderCompletion(RenderRequest request, RenderResponse response, Object handler, Exception ex) throws Exception {
		this.requestInterceptor.afterCompletion(new PortletWebRequest(request), ex);
	}

}
