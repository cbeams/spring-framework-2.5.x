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

package org.springframework.web.servlet.mvc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * Interceptor that checks and prepares request and response. Checks for supported
 * methods and a required session, and applies the specified number of cache seconds.
 * See superclass bean properties for configuration options.
 *
 * <p>All the settings supported by this interceptor can also be set on AbstractController.
 * This interceptor is mainly intended for applying checks and preparations to a set of
 * controllers mapped by a HandlerMapping.
 *
 * @author Juergen Hoeller
 * @since 27.11.2003
 * @see AbstractController
 */
public class WebContentInterceptor extends WebContentGenerator implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws ServletException {
		checkAndPrepare(request, response, handler instanceof LastModified);
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
												 ModelAndView modelAndView) {
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
															Object handler, Exception ex) {
	}

}
