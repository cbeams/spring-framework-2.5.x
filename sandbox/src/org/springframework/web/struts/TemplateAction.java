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
package org.springframework.web.struts;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionServlet;
import org.springframework.util.Assert;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.WebUtils;

/**
 * Base Struts action for use in Voca web applications. Includes convenience
 * methods that subclasses can use to extract request parrameters, and provides
 * access to the middle tier via a Spring WebApplicationContext.
 * @author Keith Donald
 */
public abstract class TemplateAction extends Action {

	protected static final String FAILURE_VIEW = "failure";

	protected static final String SUCCESS_VIEW = "success";

	/** Commons Logging logger for use by subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private WebApplicationContext webApplicationContext;

	protected ActionForward getFormView(ActionMapping mapping) {
		ActionForward formForward = mapping.getInputForward();
		Assert.notNull(formForward, "The input forward is not set for this action - programmer error?");
		Assert.hasText(formForward.getPath(), "The form view forward must have a valid path - programmer error?");
		if (logger.isDebugEnabled()) {
			logger.debug("Returning form view for action '" + getClass().getName() + "', path='"
					+ formForward.getPath() + "'");
		}
		return mapping.getInputForward();
	}

	protected ActionForward getSuccessView(ActionMapping mapping) {
		if (logger.isDebugEnabled()) {
			logger.debug("Returning success view for action " + getClass());
		}
		ActionForward successForward = mapping.findForward(SUCCESS_VIEW);
		Assert.hasText(successForward.getPath(), "The form view forward must have a valid path - programmer error?");
		if (logger.isDebugEnabled()) {
			logger.debug("Returning success view for action '" + getClass().getName() + "', path='"
					+ successForward.getPath() + "'");
		}
		return successForward;
	}

	protected void addGlobalError(HttpServletRequest request, ActionMessage errorMessage) {
		ActionErrors errors = new ActionErrors();
		errors.add(ActionMessages.GLOBAL_MESSAGE, errorMessage);
		addErrors(request, errors);
	}

	protected void addError(HttpServletRequest request, String propertyName, ActionMessage errorMessage) {
		ActionErrors errors = new ActionErrors();
		errors.add("number", errorMessage);
		addErrors(request, errors);
	}

	public void debugPrintParameters(HttpServletRequest request) {
		if (!logger.isDebugEnabled()) {
			throw new IllegalStateException("Debug log level is not allowed - enable it before calling this method");
		}
		logger.debug("[Action request parameter dump]");
		Enumeration it = request.getParameterNames();
		String parameterName = null;
		while (it.hasMoreElements()) {
			parameterName = (String)it.nextElement();
			logger.debug("    -> " + parameterName + "=" + request.getParameter(parameterName));
		}
		logger.debug("[Action request parameter dump]");
	}

	public final static String getImageButtonCommand(HttpServletRequest request) {
		Enumeration it = request.getParameterNames();
		while (it.hasMoreElements()) {
			String parameterName = (String)it.nextElement();
			if (parameterName.endsWith(".x")) {
				return parameterName.substring(0, parameterName.indexOf('.'));
			}
		}
		return null;
	}

	protected boolean isFormGet(HttpServletRequest request) {
		return "GET".equals(request.getMethod());
	}

	protected boolean isFormSubmission(HttpServletRequest request) {
		return "POST".equals(request.getMethod());
	}

	protected String getStringParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getStringParameter(request, parameterName, null);
	}

	protected int getIntParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getIntParameter(request, parameterName, -1);
	}

	protected long getLongParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getLongParameter(request, parameterName, -1);
	}

	protected String getRequiredStringParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredStringParameter(request, parameterName);
	}

	protected int getRequiredIntParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredIntParameter(request, parameterName);
	}

	protected long getRequiredLongParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredLongParameter(request, parameterName);
	}

	protected Object getRequiredSessionAttribute(HttpServletRequest request, String name) {
		return WebUtils.getRequiredSessionAttribute(request, name);
	}

	protected Object getSessionAttribute(HttpServletRequest request, String name) {
		return WebUtils.getSessionAttribute(request, name);
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Client request received for action '" + getClass().getName() + "@" + mapping.getPath() + "'");
		}
		if (logger.isDebugEnabled()) {
			if (form != null) {
				logger.debug("Action form IS present in request '" + form + "'");
			}
			else {
				logger.debug("Action form IS NOT present in request -- it is null");
			}
			debugPrintParameters(request);
		}
		if (onPreExecute(mapping, form, request, response)) {
			ActionForward forward = doExecuteAction(mapping, form, request, response);
			if (logger.isDebugEnabled()) {
				logger.debug("Execution completed successfully for action '" + mapping.getPath());
				if (forward != null) {
					logger.debug("Returning forward name='" + forward.getName() + "', path = '" + forward.getPath()
							+ "'");
				}
				else {
					logger.debug("Returning a [null] action forward");
				}
			}
			onPostExecute(forward, mapping, form, request, response);
			return forward;
		}
		else {
			throw new IllegalStateException("Action '" + getClass().getName()
					+ "' execution not allowed; a pre-execution condition failed");
		}
	}

	protected boolean onPreExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return true;
	}

	protected abstract ActionForward doExecuteAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception;

	protected void onPostExecute(ActionForward forward, ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
	}

	/**
	 * Return the WebApplicationContext used by this application. It's not
	 * normally necessary for subclasses to use this method, as they will be
	 * configured by Dependency Injection.
	 * @return the WebApplicationContext used by this application.
	 */
	protected WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}

	/**
	 * Convenient method to obtain a service layer object from the IoC container
	 * @param beanName name of the object to obtain
	 * @param requiredType type the object must conform to
	 * @return the requested object
	 */
	protected Object getBean(String beanName, Class requiredType) {
		return this.webApplicationContext.getBean(beanName, requiredType);
	}

	public void setServlet(ActionServlet actionServlet) {
		super.setServlet(actionServlet);

		// ActionServlet may be null when an application is closed
		// down before reload, especially in WebLogic
		if (actionServlet != null) {
			ServletContext servletContext = actionServlet.getServletContext();
			this.webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		}
	}
}