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

package org.springframework.web.portlet.mvc;

import java.util.Enumeration;
import java.util.HashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.handler.PortletContentGenerator;

/**
 * <p>Convenient superclass for controller implementations, using the Template
 * Method design pattern.</p>
 * 
 * <p>As stated in the {@link Controller Controller}
 * interface, a lot of functionality is already provided by certain abstract
 * base controllers. The AbstractController is one of the most important
 * abstract base controller providing basic features such controlling if a
 * session is required and render caching.</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="Controller.html#workflow">and that defined by interface</a>):</b><br>
 * <ol>
 *  <li>If this is an action request, {@link #handleActionRequest handleActionRequest}
 *      will be called by the DispatcherPortlet once to perform the action defined by this
 *      controller.</li>
 *  <li>If a session is required, try to get it (PortletException if not found).</li>
 *  <li>Call method {@link #handleActionRequestInternal handleActionRequestInternal},
 *      (optionally synchronizing around the call on the PortletSession),
 *      which should be overridden by extending classes to provide actual functionality to 
 *      perform the desired action of the controller.  This will be executed only once.</li>
 *  <li>For a straight render request, or the render phase of an action request (assuming the
 *      same controller is called for the render phase -- see tip below), 
 *      {@link #handleRenderRequest handleRenderRequest} will be called by the DispatcherPortlet
 *      repeatedly to render the display defined by this controller.</li>
 *  <li>If a session is required, try to get it (PortletException if none found).</li>
 *  <li>It will control caching as defined by the cacheSeconds property.</li>
 *  <li>Call method {@link #handleRenderRequestInternal handleRenderRequestInternal},
 *      (optionally synchronizing around the call on the PortletSession),
 *      which should be overridden by extending classes to provide actual functionality to 
 *      return {@link org.springframework.web.portlet.ModelAndView ModelAndView} objects.
 *      This will be executed repeatedly as the portal updates the current displayed page.</li>
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
 *      <td>requiresSession</td>
 *      <td>false</td>
 *      <td>whether a session should be required for requests to be able to
 *          be handled by this controller. This ensures, derived controller
 *          can - without fear of Nullpointers - call request.getSession() to
 *          retrieve a session. If no session can be found while processing
 *          the request, a PortletException will be thrown</td>
 *  </tr>
 *  <tr>
 *      <td>synchronizeOnSession</td>
 *      <td>false</td>
 *      <td>whether the calls to <code>handleRenderRequestInternal</code> and
 *          <code>handleRenderRequestInternal</code> should be
 *          synchronized around the PortletSession, to serialize invocations
 *          from the same client. No effect if there is no PortletSession.
 *      </td>
 *  </tr>
 *  <tr>
 *      <td>cacheSeconds</td>
 *      <td>-1</td>
 *      <td>indicates the amount of seconds to specify caching is allowed in 
 *          the render response generatedby  this request. 0 (zero) will indicate
 *          no caching is allowed at all, -1 (the default) will not override the
 *          portlet configuration and any positive number will cause the render
 *          reponse to declare the amount indicated as seconds to cache the content</td>
 *  </tr>
 *  <tr>
 *      <td>renderWhenMinimized</td>
 *      <td>false</td>
 *      <td>whether should be rendered when the portlet is in a minimized state -- 
 *          will return null for the ModelandView when the portlet is minimized 
 *          and this is false</td>
 *  </tr>
 * </table>
 *
 * <p><b>TIP:</b> The controller mapping will be run twice by the PortletDispatcher for 
 * action requests -- once for the action phase and again for the render phase.  You can
 * reach the render phase of a different controller by simply changing the values for the 
 * criteria your mapping is using, such as portlet mode or a request parameter, during the
 * action phase of your controller.  This is very handy since redirects within the portlet
 * are apparently impossible.  Before doing this, it is usually wise to call 
 * <code>clearAllRenderParameters</code> and then explicitly set all the parameters that
 * you want the new controller to see.  This avoids unexpected parameters from being passed
 * to the render phase of the second controller, such as the parameter indicating a form 
 * submit ocurred in an <code>AbstractFormController</code>.
 * </p>  
 * 
 * @author John A. Lewis
 * @author Rainer Schmitz
 * @author Juergen Hoeller
 * @since 1.3
 */
public abstract class AbstractController extends PortletContentGenerator implements Controller {

	private boolean synchronizeOnSession = false;
	
	private boolean renderWhenMinimized = false;

	
	/**
	 * Set if controller execution should be synchronized on the session,
	 * to serialize parallel invocations from the same client.
	 * <p>More specifically, the execution of the handleRenderRequestInternal
	 * and handleActionRequestInternal methods will get synchronized if this 
	 * flag is true.
	 * @see #handleRenderRequestInternal
	 * @see #handleActionRequestInternal
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

	/**
	 * Set if the controller should render an view when the portlet is in
	 * a minimized window.  The default is false.
	 * @see javax.portlet.RenderRequest#getWindowState
	 * @see javax.portlet.WindowState#MINIMIZED
	 */
    public void setRenderWhenMinimized(boolean renderWhenMinimized) {
        this.renderWhenMinimized = renderWhenMinimized;
    }

	/**
	 * Return whether controller will render when portlet is minimized.
	 */
    public boolean isRenderWhenMinimized() {
        return renderWhenMinimized;
    }

    
    /* (non-Javadoc)
	 * @see Controller#handleRenderRequest
	 */
	public final ModelAndView handleRenderRequest(RenderRequest request, RenderResponse response)
			throws Exception {

	    // if the portlet is minimized and we don't want to render then return null
	    if (WindowState.MINIMIZED.equals(request.getWindowState()) &&
	            ! renderWhenMinimized)
	        return null;
	    
		// delegate to PortletContentGenerator for checking and preparing
		checkAndPrepare(request, response);

		// execute in synchronized block if required
		if (this.synchronizeOnSession) {
			PortletSession session = request.getPortletSession(false);
			if (session != null) {
				synchronized (session) {
					return handleRenderRequestInternal(request, response);
				}
			}
		}

		return handleRenderRequestInternal(request, response);
	}
	
	/* (non-Javadoc)
	 * @see Controller#handleActionRequest
	 */
	public final void handleActionRequest(ActionRequest request, ActionResponse response)
		throws Exception {
		
		// delegate to PortletContentGenerator for checking and preparing
		checkAndPrepare(request, response);
		
		// execute in synchronized block if required
		if (this.synchronizeOnSession) {
			PortletSession session = request.getPortletSession(false);
			if (session != null) {
				synchronized (session) {
					handleActionRequestInternal(request, response);
					return;
				}
			}
		}

		handleActionRequestInternal(request, response);
	}

	/**
	 * <p>Subclasses are meant to override this method if the controller 
	 * is expected to handle render requests.</p>
	 * <p>Default implementation throws a PortletException.</p>
	 * <p>The contract is the same as for <code>handleRenderRequest</code>.</p>
	 * @see #handleRenderRequest
	 * @see #handleActionRequestInternal
	 */
	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response)
		throws Exception {
	    throw new PortletException(this.getClass().getName() + " does not handle render requests");
	}

	/**
	 * <p>Subclasses are meant to override this method if the controller 
	 * is expected to handle action requests.</p>
	 * <p>Default implementation throws a PortletException.</p>
	 * <p>The contract is the same as for <code>handleActionRequest</code>.</p>
	 * @see #handleActionRequest
	 * @see #handleRenderRequestInternal
	 */
	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response)
		throws Exception {
	    throw new PortletException(this.getClass().getName() + " does not handle action requests");
	}
	
    /**
     * Pass all the action request parameters to the render phase by putting them into
     * the action response object. This may not be called when the action will call
     * {@link ActionResponse#sendRedirect sendRedirect}.
     * @param request the current action request
     * @param response the current action response
     * @see ActionResponse#setRenderParameter
     */
    protected void passAllParametersToRenderPhase(ActionRequest request, ActionResponse response) {
		if (logger.isDebugEnabled())
			logger.debug("Passing all action request parameters to render phase");
		try {
		    Enumeration en = request.getParameterNames();
		    while (en.hasMoreElements()) {
		        String param = (String)en.nextElement();
		        String values[] = request.getParameterValues(param);
		        response.setRenderParameter(param, values);
		    }	        
		} catch (IllegalStateException ex) {
		    // ignore in case sendRedirect was already set
		}
    }

    /**
     * Clears all the render parameters from the ActionResponse.
     * This may not be called when the action will call
     * {@link ActionResponse#sendRedirect sendRedirect}.
     * @param response the current action response
     * @see ActionResponse#setRenderParameters
     */
    protected void clearAllRenderParameters(ActionResponse response) {
		if (logger.isDebugEnabled())
			logger.debug("Clearing all the parameters from being passed to the render phase");
		try {
	        response.setRenderParameters(new HashMap());
		} catch (IllegalStateException ex) {
		    // ignore in case sendRedirect was already set
		}
    }

}
