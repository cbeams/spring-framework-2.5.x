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

package org.springframework.web.servlet.mvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Plain handler interface that does not expose any ModelAndView capability
 * and only allows to throw ServletException and IOException.
 * Essentially the direct equivalent of a Servlet, reduced to a handle method.
 *
 * <p>Typically implemented to generate binary responses directly,
 * with not separate view resource involved. The lack of a ModelAndView
 * return value gives a clearer signature to callers other than the
 * DispatcherServlet, indicating there will never be a view to render.
 *
 * <p>RequestHandlers can optionally implement the LastModified interface.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see LastModified
 * @see SimpleRequestHandlerAdapter
 * @see Controller
 * @see org.springframework.web.servlet.ModelAndView
 * @see org.springframework.mock.web.MockHttpServletRequest
 * @see org.springframework.mock.web.MockHttpServletResponse
 */
public interface RequestHandler {

	/**
	 * Handle the given request, generating a response.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws ServletException in case of general errors
	 * @throws IOException in case of I/O errors
	 */
	void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException;

}
