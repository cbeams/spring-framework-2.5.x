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
package org.springframework.web.flow.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionResult;
import org.springframework.web.flow.AttributesAccessor;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.MutableAttributesAccessor;
import org.springframework.web.util.WebUtils;

/**
 * Base action implementation that provides a number of helper methods generally
 * useful to any controller/command action. These include:
 * <ul>
 * <li>Creating common <code>ActionResult</code> objects
 * <li>Accessing request parameters
 * <li>Accessing and export form objects
 * <li>Inserting action pre and post execution logic (may also be done with an
 * interceptor)
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractAction implements Action, InitializingBean {

	/**
	 * Default name used for a form object in flow scope.
	 */
	public static final String DEFAULT_FORM_OBJECT_NAME = "formObject";

	/**
	 * The form object is then aliased under this name by the default populate
	 * and bind actions
	 */
	public static final String LOCAL_FORM_OBJECT_NAME = "localFormObject";

	/**
	 * The form object errors instance is aliased under this name by the default
	 * populate and bind actions
	 */
	public static final String LOCAL_FORM_OBJECT_ERRORS_NAME = "localFormObjectErrors";

	public static final String ERRORS_SUFFIX = "Errors";

	protected final Log logger = LogFactory.getLog(getClass());

	public void afterPropertiesSet() {
		initAction();
	}

	/**
	 * Action initializing callback, may be overriden by subclasses to perform
	 * custom initialization.
	 */
	protected void initAction() {

	}

	/**
	 * Factory method that produces the common 'success' result parameter
	 * object.
	 * @return a success result object
	 */
	protected ActionResult success() {
		return new ActionResult(FlowConstants.SUCCESS);
	}

	/**
	 * Factory method that produces the common 'error' result parameter object.
	 * @return a error result object
	 */
	protected ActionResult error() {
		return new ActionResult(FlowConstants.ERROR);
	}

	/**
	 * Factory method that produces the common 'add' result parameter object.
	 * @return a add result object
	 */
	protected ActionResult add() {
		return new ActionResult(FlowConstants.ADD);
	}

	/**
	 * Factory method that produces the common 'search' result parameter object.
	 * @return a search result object
	 */
	protected ActionResult search() {
		return new ActionResult(FlowConstants.SEARCH);
	}

	/**
	 * Factory method that produces a <code>ActionResult</code> parameter
	 * object given a string identifier
	 * @param resultId The result id
	 * @return The action result
	 */
	protected ActionResult actionResult(String resultId) {
		return new ActionResult(resultId);
	}

	/**
	 * Get a string request parameter with a <code>null</code> fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value, or null if not found
	 */
	protected String getStringParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getStringParameter(request, parameterName, null);
	}

	/**
	 * Get a short request parameter with a <code>-1</code> fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 */
	protected short getShortParameter(HttpServletRequest request, String parameterName) {
		return (short)RequestUtils.getIntParameter(request, parameterName, -1);
	}

	/**
	 * Get a int request parameter with a <code>-1</code> fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 */
	protected int getIntParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getIntParameter(request, parameterName, -1);
	}

	/**
	 * Get a boolean request parameter with a <code>defaultValue</code>
	 * fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @param defaultValue the fallback value
	 * @return The parameter value
	 */
	protected boolean getBooleanParameter(HttpServletRequest request, String parameterName, boolean defaultValue) {
		return RequestUtils.getBooleanParameter(request, parameterName, defaultValue);
	}

	/**
	 * Get a long request parameter with a <code>-1</code> fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 */
	protected long getLongParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getLongParameter(request, parameterName, -1);
	}

	/**
	 * Get a string request parameter throwing an exception if not found.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 * @throws ServletRequestBindingException The parameter was not present in
	 *         the request
	 */
	protected String getRequiredStringParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredStringParameter(request, parameterName);
	}

	/**
	 * Get a short request parameter throwing an exception if not found.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 * @throws ServletRequestBindingException The parameter was not present in
	 *         the request
	 */
	protected short getRequiredShortParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return (short)RequestUtils.getRequiredIntParameter(request, parameterName);
	}

	/**
	 * Get a integer request parameter throwing an exception if not found.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 * @throws ServletRequestBindingException The parameter was not present in
	 *         the request
	 */
	protected int getRequiredIntParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredIntParameter(request, parameterName);
	}

	/**
	 * Get a long request parameter throwing an exception if not found.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 * @throws ServletRequestBindingException The parameter was not present in
	 *         the request
	 */
	protected long getRequiredLongParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredLongParameter(request, parameterName);
	}

	/**
	 * Get a boolean request parameter throwing an exception if not found.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 * @throws ServletRequestBindingException The parameter was not present in
	 *         the request
	 */
	protected boolean getRequiredBooleanParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredBooleanParameter(request, parameterName);
	}

	/**
	 * Get a attribute out of the http session, throwing an exception if not
	 * found.
	 * @param request The http request with a session accessor
	 * @param name The attribute name
	 * @return The attribute value
	 * @throws IllegalStateException the attribute was not present in session
	 */
	protected Object getRequiredSessionAttribute(HttpServletRequest request, String name) throws IllegalStateException {
		return WebUtils.getRequiredSessionAttribute(request, name);
	}

	/**
	 * Get a attribute out of the http session, returning null if not found.
	 * @param request The http request with a session accessor
	 * @param name The attribute name
	 * @return The attribute value
	 */
	protected Object getSessionAttribute(HttpServletRequest request, String name) {
		return WebUtils.getSessionAttribute(request, name);
	}

	/**
	 * Gets the form object from the model, using the local (to flow) name of
	 * {@link #LOCAL_FORM_OBJECT_NAME}
	 * 
	 * @param model the flow model
	 * @param formObjectClass the class of the form object, which will be
	 *        verified
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         model
	 */
	protected Object getLocalFormObject(AttributesAccessor model, Class formObjectClass) throws IllegalStateException {
		return model.getRequiredAttribute(LOCAL_FORM_OBJECT_NAME, formObjectClass);
	}

	/**
	 * Gets the form object from the model, using the local (to flow) name of
	 * {@link #LOCAL_FORM_OBJECT_NAME}
	 * 
	 * @param model the flow model
	 * @throws IllegalStateException if the form object is not found in the
	 *         model
	 */
	protected Object getLocalFormObject(AttributesAccessor model) throws IllegalStateException {
		return model.getRequiredAttribute(LOCAL_FORM_OBJECT_NAME);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the model, using
	 * the local (to flow) name of {@link #LOCAL_FORM_OBJECT_NAME}
	 * 
	 * @param model the flow model
	 * @throws IllegalStateException if the Errors instance is not found in the
	 *         model
	 */
	protected Errors getLocalFormErrors(AttributesAccessor model) throws IllegalStateException {
		return (Errors)model.getRequiredAttribute(LOCAL_FORM_OBJECT_ERRORS_NAME, Errors.class);
	}

	/**
	 * Gets the form object from the model, using the specified name
	 * @param model the flow model
	 * @param formName the name of the form in the model
	 * @param formObjectClass the class of the form object, which will be
	 *        verified
	 * @return the form object
	 */
	protected Object getRequiredFormObject(AttributesAccessor model, String formObjectName, Class formObjectClass) {
		return model.getRequiredAttribute(formObjectName, formObjectClass);
	}

	/**
	 * Gets the form object from the model, using the specified name
	 * @param model the flow model
	 * @param formName the name of the form in the model
	 * @return the form object
	 */
	protected Object getRequiredFormObject(AttributesAccessor model, String formObjectName) {
		return model.getRequiredAttribute(formObjectName);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the model, using
	 * the specified name
	 * @param model The flow model
	 * @param formObjectName The name of the form object
	 * @return The form object errors instance
	 */
	protected Errors getRequiredFormErrors(AttributesAccessor model, String formObjectName) {
		return (Errors)model.getRequiredAttribute(BindException.ERROR_KEY_PREFIX + formObjectName, Errors.class);
	}

	/**
	 * Export a <i>new </i> errors instance to the flow model for the form
	 * object under the local (to flow) form object name,
	 * {@link #LOCAL_FORM_OBJECT_NAME}.
	 * 
	 * @param model The flow model
	 * @param formObject The form object to export an errors instance under
	 */
	protected void exportErrors(MutableAttributesAccessor model, Object formObject) {
		exportErrors(model, formObject, LOCAL_FORM_OBJECT_NAME);
	}

	/**
	 * Export a <i>new </i> errors instance to the flow model for the form
	 * object with the specified form object name.
	 * @param model The flow model
	 * @param formObject The form object
	 * @param formObjectName The name of the form object
	 */
	protected void exportErrors(MutableAttributesAccessor model, Object formObject, String formObjectName) {
		exportErrors(model, new BindException(formObject, formObjectName));
	}

	private void exportErrors(MutableAttributesAccessor model, BindException errors) {
		// also bind it under the local (to flow) alias, so other actions can
		// find it easily
		model.setAttribute(LOCAL_FORM_OBJECT_NAME, errors.getTarget());
		model.setAttribute(LOCAL_FORM_OBJECT_ERRORS_NAME, errors);
		model.setAttributes(errors.getModel());
	}

	public final ActionResult execute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Action '" + getClass().getName() + "' beginning execution");
		}
		ActionResult result = onPreExecute(request, response, model);
		if (result == null) {
			result = doExecuteAction(request, response, model);
			if (logger.isDebugEnabled()) {
				logger.debug("Action '" + getClass().getName() + "' completed execution; event result is " + result);
			}
			onPostExecute(request, response, model);
			if (logger.isInfoEnabled()) {
				if (result == null) {
					logger.info("Retured action event is [null]; that's ok so long as another action associated "
							+ "with the currently executing flow state returns a valid event");
				}
			}
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("Action execution disallowed; event is " + result);
			}
		}
		return result;
	}

	/**
	 * Pre-action-execution hook, subclasses may override.
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws Exception
	 */
	protected ActionResult onPreExecute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws Exception {
		return null;
	}

	/**
	 * Template hook method subclasses should override to encapsulate their
	 * specific action execution logic.
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @return The action result
	 * @throws Exception A unrecoverable exception occured
	 */
	protected abstract ActionResult doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws Exception;

	/**
	 * Post-action execution hook, subclasses may override.
	 * @param request
	 * @param response
	 * @param model
	 * @throws Exception
	 */
	protected void onPostExecute(HttpServletRequest request, HttpServletResponse response, AttributesAccessor model)
			throws Exception {
	}
}