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

package org.springframework.web.servlet.mvc.throwaway;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adapter to use the ThrowawayController workflow interface with the
 * generic DispatcherServlet. Does not support last-modified checks.
 *
 * <p>This is an SPI class, not used directly by application code.
 *
 * @author Juergen Hoeller
 * @since 08.12.2003
 */
public class ThrowawayControllerHandlerAdapter implements HandlerAdapter {

	public static final String THROWAWAY_CONTROLLER_NAME = "throwawayController";

	public boolean supports(Object handler) {
		return (handler instanceof ThrowawayController);
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		ThrowawayController throwaway = (ThrowawayController) handler;
		ServletRequestDataBinder binder = new ServletRequestDataBinder(throwaway, THROWAWAY_CONTROLLER_NAME);
		binder.bind(request);
		binder.closeNoCatch();
		return throwaway.execute();
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

}
