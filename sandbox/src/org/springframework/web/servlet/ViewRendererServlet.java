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
package org.springframework.web.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.portlet.context.PortletApplicationContext;


/**
 * ViewRendererServlet is a bridge servlet for Portlet MVC support.  It is here just 
 * to force the portlet container to convert the PortletRequest to a ServletRequest,
 * which it has to do when including a resource via the PortletRequestDispatcher.
 * <p>
 * The actual mapping of the bridge servlet is configurable in the DispatcherPortlet,
 * via a "viewRendererUrl" property.  The default is "/WEB-INF/view"; which is just
 * available for internal resource dispatching.
 * 
 * @author William G. Thompson, Jr.
 * TODO: Throw appropriate exceptions, not just Exception. PortletException?
 */
public class ViewRendererServlet extends HttpServletBean {
    
    /** Handle to View in request attributes **/
    public static final String VIEW_ATTRIBUTE = ViewRendererServlet.class.getName() + ".VIEW";
    
    /** Handle to Model in request attributes **/
    public static final String MODEL_ATTRIBUTE = ViewRendererServlet.class.getName() + ".MODEL";
    
    /** Handle to DispatcherPortlet PortletApplicationContext in request attributes **/
    public final static String DISPATCHER_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = 
        ViewRendererServlet.class.getName() + ".PORTLET_CONTEXT";

	/**
	 * It's up to each subclass to decide whether or not it supports a request method.
	 * It should throw a Servlet exception if it doesn't support a particular request type.
	 * This might commonly be done with GET for forms, for example
	 */
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		Exception failureCause = null;
		PortletApplicationContext pc = (PortletApplicationContext) request.getAttribute(DISPATCHER_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);

		try {
			if (pc == null) {
			    throw new Exception("Could not complete render request, DispatcherPortlet ApplicationContext is null.");			    			    
			}
			Map model = (Map) request.getAttribute(MODEL_ATTRIBUTE);
            View view = (View) request.getAttribute(VIEW_ATTRIBUTE);
            if (view == null) {
                throw new Exception("Could not complete render request, view is null.");
            } else {
                view.render(model, request, response);                
            }
		}
		catch (ServletException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (RuntimeException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Exception ex) {
			failureCause = ex;
			throw new ServletException(ex.getMessage(), ex);
		}
		finally {
			long processingTime = System.currentTimeMillis() - startTime;
			// whether or not we succeeded, publish an event
			if (failureCause != null) {
				logger.error("Could not complete request", failureCause);
				pc.publishEvent(new RequestHandledEvent(this, request.getRequestURI(), processingTime, request.getRemoteAddr(),
				                            request.getMethod(), getServletConfig().getServletName(), failureCause));
			}
			else {
				logger.debug("Successfully completed request");
				pc.publishEvent(new RequestHandledEvent(this, request.getRequestURI(), processingTime, request.getRemoteAddr(),
				                            request.getMethod(), getServletConfig().getServletName()));
			}
		}
	}


}
