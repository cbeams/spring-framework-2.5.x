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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.validation.BindException;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Abstract base class for custom command controllers. Autopopulates a
 * command bean from the request. For command validation, a validator
 * (property inherited from BaseCommandController) can be used.</p>
 *
 * <p>This command controller should preferrable not be used to handle form
 * submission, because functionality for forms is more offered in more
 * detail by the {@link org.springframework.web.servlet.mvc.AbstractFormController
 * AbstractFormController} and its corresponding implementations.</p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="AbstractController.html#config">and those defined by superclass</a>):</b><br>
 * <i>none</i> (so only those available in superclass).</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a name="BaseCommandController.html#workflow">and that defined by superclass</a>):</b><br>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @see #setCommandClass
 * @see #setCommandName
 * @see #setValidator
 */
public abstract class AbstractCommandController extends BaseCommandController {

	/**
	 * Unlike the Servlet version of this class, we have to deal with the
	 * two-phase nature of the porlet request.  To do this, we need to pass
	 * forward the command object and the bind/validation errors that occured
	 * on the command object from the action phase to the render phase.
	 * The only direct way to pass things forward and preserve them for each
	 * render request is through render parameters, but these are limited to
	 * String objects and we need to pass more complicated objects.  The only
	 * other way to do this is in the session.  The bad thing about using the
	 * session is that we have no way of knowing when we are done re-rendering
	 * the request and so we don't know when we can remove the objects from
	 * the session.  So we will end up polluting the session with old objects
	 * when we finally leave the render of this controller and move on to 
	 * somthing else.  To minimize the pollution, we will use a static string
	 * value as the session attribute name.  At least this way we are only ever 
	 * leaving one orphaned set behind.  The methods that return these names
	 * can be overridden if you want to use a different method, but be aware
	 * of the session pollution that may occur.
	 */
	private static final String RENDER_COMMAND_SESSION_ATTRIBUTE = 
			"org.springframework.web.portlet.mvc.RenderCommand";
	private static final String RENDER_ERRORS_SESSION_ATTRIBUTE = 
			"org.springframework.web.portlet.mvc.RenderErrors";

	/**
	 * This render parameter is used to indicate forward to the render phase
	 * that a valid command (and errors) object is in the session.
	 */
	private static final String COMMAND_IN_SESSION_PARAMETER = 
			"command-in-session";

	private static final String TRUE = Boolean.TRUE.toString();
	
	/**
	 * Create a new AbstractCommandController.
	 */
	public AbstractCommandController() {
	}

	/**
	 * Create a new AbstractCommandController.
	 * @param commandClass class of the command bean
	 */
	public AbstractCommandController(Class commandClass) {
		setCommandClass(commandClass);
	}

	/**
	 * Create a new AbstractCommandController.
	 * @param commandClass class of the command bean
	 * @param commandName name of the command bean
	 */
	public AbstractCommandController(Class commandClass, String commandName) {
		setCommandClass(commandClass);
		setCommandName(commandName);
	}
	
	protected final ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response)
			throws Exception {

	    Object command = null;
		BindException errors = null;

	    // get the command and errors objects from the session, if they exist
		if (isCommandInSession(request)) {
			if (logger.isDebugEnabled())
			    logger.debug("render phase obtaining command and errors objects from session");
		    command = getRenderCommand(request);
			errors = getRenderErrors(request);
		} else {
			if (logger.isDebugEnabled())
			    logger.debug("render phase creating new command and errors objects");
		}

		// if no command object was in the session, create a new one
		if (command == null)
		    command = getCommand(request);

		// if no errors object was in the session, compute a new one
	    if (errors == null) {
	        PortletRequestDataBinder binder = bindAndValidate(request, command);
	        errors = binder.getErrors();
	    }

	    return handleRender(request, response, command, errors);
	}

	protected final void handleActionRequestInternal(ActionRequest request, ActionResponse response)
	throws Exception {

	    // create the command object
	    Object command = getCommand(request);

	    // compute the errors object
	    PortletRequestDataBinder binder = bindAndValidate(request, command);
	    BindException errors = binder.getErrors();

	    handleAction(request, response, command, errors);

	    // pass the command and errors forward to the render phase
		setRenderCommandAndErrors(request, response, command, errors);
	}

	/**
	 * Template method for render request handling, providing a populated and validated instance
	 * of the command class, and an Errors object containing binding and validation errors.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param request current render request
	 * @param response current render response
	 * @param command the populated command object
	 * @param errors validation errors holder
	 * @return a ModelAndView to render, or null if handled directly
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 */
	protected abstract ModelAndView handleRender(
			RenderRequest request, RenderResponse response, Object command, BindException errors)
			throws Exception;

	/**
	 * Template method for request handling, providing a populated and validated instance
	 * of the command class, and an Errors object containing binding and validation errors.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param request current action request
	 * @param response current action response
	 * @param command the populated command object
	 * @param errors validation errors holder
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 */
	protected abstract void handleAction(
			ActionRequest request, ActionResponse response, Object command, BindException errors)
			throws Exception;

	/** 
	 * Return the name of the session attribute that holds
	 * the render phase command object for this form controller.
	 * @return the name of the render phase command object session attribute
	 * @see javax.portlet.PortletSession#getAttribute
	 */
	protected String getRenderCommandSessionAttributeName() {
		return RENDER_COMMAND_SESSION_ATTRIBUTE;
	}

	/** 
	 * Return the name of the session attribute that holds
	 * the render phase command object for this form controller.
	 * @return the name of the render phase command object session attribute
	 * @see javax.portlet.PortletSession#getAttribute
	 */
	protected String getRenderErrorsSessionAttributeName() {
		return RENDER_ERRORS_SESSION_ATTRIBUTE;
	}

	/**
	 * Get the command object cached for the render phase
	 * @see #getRenderErrors
	 * @see #getRenderCommandSessionAttributeName
	 * @see #setRenderCommandAndErrors
	 */
	protected final Object getRenderCommand(RenderRequest request) {
		PortletSession session = request.getPortletSession(false);
		if (session == null) return null;
		return session.getAttribute(getRenderCommandSessionAttributeName());
	}

	/**
	 * Get the bind and validation errors cached for the render phase
	 * @see #getRenderCommand
	 * @see #getRenderErrorsSessionAttributeName
	 * @see #setRenderCommandAndErrors
	 */
	protected final BindException getRenderErrors(RenderRequest request) {
		PortletSession session = request.getPortletSession(false);
		if (session == null) return null;
		return (BindException)session.getAttribute(getRenderErrorsSessionAttributeName());
	}

	/**
	 * Set the command object and errors object for the render phase.
	 * @param request the current action request
	 * @param command the command object to preserve for the render phase
	 * @param errors the errors from binding and validation to preserve for the render phase
	 * @see #getRenderCommand
	 * @see #getRenderErrors
	 * @see #getRenderCommandSessionAttributeName
	 * @see #getRenderErrorsSessionAttributeName
	 */
	protected final void setRenderCommandAndErrors(ActionRequest request, ActionResponse response,
			Object command, BindException errors) throws Exception {
		PortletSession session = request.getPortletSession();
		session.setAttribute(getRenderCommandSessionAttributeName(), command);
		session.setAttribute(getRenderErrorsSessionAttributeName(), errors);
		setCommandInSession(response);
	}

	/** 
	 * Return the name of the render parameter that indicates there
	 * is a valid command (and errors) object in the session.
	 * @return the name of the render parameter
	 * @see javax.portlet.RenderRequest#getParameter
	 */
	protected String getCommandInSessionParameterName() {
		return COMMAND_IN_SESSION_PARAMETER;
	}

	/**
	 * Set the action response parameter that indicates there is a
	 * command (and errors) object in the session for the render phase.
	 * @param response the current action response
	 * @see #getCommandInSessionParameterName
	 * @see #isCommandInSession
	 */
	protected final void setCommandInSession(ActionResponse response) {
		if (logger.isDebugEnabled())
			logger.debug("Setting render parameter [" + getCommandInSessionParameterName() + "] to indicate a valid command (and errors) object are in the session");
		try {
		    response.setRenderParameter(getCommandInSessionParameterName(), TRUE);
		} catch (IllegalStateException ex) {
		    // ignore in case sendRedirect was already set
		}
	}

	/**
	 * Determine if there is a valid command (and errors) object in the
	 * session for this render request.
	 * @param request current render request
	 * @return if there is a valid command object in the session
	 * @see #getCommandInSessionParameterName
	 * @see #setCommandInSession
	 */
	protected final boolean isCommandInSession(RenderRequest request) {
	    return TRUE.equals(request.getParameter(getCommandInSessionParameterName()));
	}

}
