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

package org.springframework.web.context.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.WebApplicationContext;

/**
 * Simple HttpServlet that delegates to an HttpRequestHandler bean defined
 * in Spring's root web application context. The bean name must match the
 * HttpRequestHandlerServlet servlet-name as defined in <code>web.xml</code>.
 *
 * <p>This can for example be used to expose a single Spring remote exporter,
 * such as HttpInvokerServiceExporter and HessianServiceExporter, per
 * HttpRequestHandlerServlet definition. This is an alternative to defining
 * remote exporters as beans in a DispatcherServlet context, leveraging
 * the advanced mapping and interception facilities there.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.HttpRequestHandler
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public class HttpRequestHandlerServlet extends HttpServlet {

	private HttpRequestHandler target;


	public void init() throws ServletException {
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		this.target = (HttpRequestHandler) wac.getBean(getServletName(), HttpRequestHandler.class);
	}


	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.target.handleRequest(request, response);
	}

}
