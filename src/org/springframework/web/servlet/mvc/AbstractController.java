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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * <p>Convenient superclass for controller implementations, using the Template
 * Method design pattern.</p>
 * 
 * <p>As stated in the {@link org.springframework.web.servlet.mvc.Controller Controller}
 * interface, a lot of functionality is already provided by certain abstract
 * base controllers. The AbstractController is one of the most important
 * abstract base controller providing basic features such as the generation
 * of caching headers and the enabling or disabling of
 * supported methods (GET/POST).</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="Controller.html#workflow">and that defined by interface</a>):</b><br>
 * <ol>
 *  <li>{@link #handleRequest(HttpServletRequest,HttpServletResponse) handleRequest()}
 *      will be called by the DispatcherServlet</li>
 *  <li>Inspection of supported methods (ServletException if request method
 *      is not support)</li>
 *  <li>If session is required, try to get it (ServletException if not found)</li>
 *  <li>Set caching headers if needed according to cacheSeconds propery</li>
 *  <li>Call abstract method {@link #handleRequestInternal(HttpServletRequest,HttpServletResponse) handleRequestInternal()},
 *      which should be implemented by extending classes to provide actual
 *      functionality to return {@link org.springframework.web.servlet.ModelAndView ModelAndView} objects.</li>
 * </ol>
 * </p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="Controller.html#config">and those defined by interface</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></th>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>supportedMethods</td>
 *      <td>GET,POST</td>
 *      <td>comma-separated (CSV) list of methods supported by this controller,
 *          such as GET, POST and PUT</td>
 *  </tr>
 *  <tr>
 *      <td>requireSession</td>
 *      <td>false</td>
 *      <td>whether a session should be required for requests to be able to
 *          be handled by this controller. This ensures, derived controller
 *          can - without fear of Nullpointers - call request.getSession() to
 *          retrieve a session. If no session can be found while processing
 *          the request, a ServletException will be thrown</td>
 *  </tr>
 *  <tr>
 *      <td>cacheSeconds</td>
 *      <td>-1</td>
 *      <td>indicates the amount of seconds to include in the cache header
 *          for the response following on this request. 0 (zero) will include
 *          headers for no caching at all, -1 (the default) will not generate
 *          <i>any headers</i> and any positive number will generate headers
 *          that state the amount indicated as seconds to cache the content</td>
 *  </tr>
 * </table>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see WebContentInterceptor
 */
public abstract class AbstractController extends WebContentGenerator implements Controller {

	private boolean synchronizeOnSession = false;

	/**
	 * Set if controller execution should be synchronized on the session,
	 * to serialize parallel invocations from the same client.
	 * <p>More specifically, the execution of the handleRequestInternal
	 * method will get synchronized if this flag is true.
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
	 */
	public final void setSynchronizeOnSession(boolean synchronizeOnSession) {
		this.synchronizeOnSession = synchronizeOnSession;
	}

	/**
	 * Return whether controller execution should be synchronized on the session.
	 */
	public final boolean isSynchronizeOnSession() {
		return synchronizeOnSession;
	}

	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// delegate to WebContentGenerator for checking and preparing
		checkAndPrepare(request, response, this instanceof LastModified);

		// execute in synchronized block if required
		if (this.synchronizeOnSession) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				synchronized (session) {
					return handleRequestInternal(request, response);
				}
			}
		}
		
		return handleRequestInternal(request, response);
	}

	/**
	 * Template method. Subclasses must implement this.
	 * The contract is the same as for handleRequest.
	 * @see #handleRequest
	 */
	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	    throws Exception;

}
