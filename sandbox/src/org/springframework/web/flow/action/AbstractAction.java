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
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowModel;
import org.springframework.web.flow.MutableFlowModel;
import org.springframework.web.util.WebUtils;

/**
 * Base action implementation that provides a number of helper methods generally
 * useful to any controller/command action. These include:
 * <ul>
 * <li>Creating common events
 * <li>Accessing request parameters and session attributes
 * <li>Accessing and exporting form objects
 * <li>Inserting action pre and post execution logic (may also be done with an
 * interceptor)
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractAction implements Action, InitializingBean {

	/**
	 * The form object instance is aliased under this attribute name in the flow
	 * model by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ATTRIBUTE = "localFormObject";

	/**
	 * The form object errors instance is aliased under this attribute name in
	 * the flow model by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ERRORS_ATTRIBUTE = "localFormObjectErrors";

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
	
	//creating common events

	/**
	 * Returns the default error event ("error").
	 */
	protected String error() {
		return FlowConstants.ERROR;
	}

	/**
	 * Returns the default success event ("success").
	 */
	protected String success() {
		return FlowConstants.SUCCESS;
	}

	//accessing request parameters and session attributes

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
	 * Get a long request parameter with a <code>-1</code> fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @return The parameter value
	 */
	protected long getLongParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getLongParameter(request, parameterName, -1);
	}

	/**
	 * Get a boolean request parameter with a specified fallback value.
	 * @param request The http request
	 * @param parameterName The parameter name
	 * @param defaultValue the fallback value
	 * @return The parameter value
	 */
	protected boolean getBooleanParameter(HttpServletRequest request, String parameterName, boolean defaultValue) {
		return RequestUtils.getBooleanParameter(request, parameterName, defaultValue);
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
	 * Get an attribute out of the http session, returning null if not found.
	 * @param request The http request with a session accessor
	 * @param name The attribute name
	 * @return The attribute value
	 */
	protected Object getSessionAttribute(HttpServletRequest request, String name) {
		return WebUtils.getSessionAttribute(request, name);
	}

	/**
	 * Get an attribute out of the http session, throwing an exception if not
	 * found.
	 * @param request The http request with a session accessor
	 * @param name The attribute name
	 * @return The attribute value
	 * @throws IllegalStateException the attribute was not present in the session
	 */
	protected Object getRequiredSessionAttribute(HttpServletRequest request, String name) throws IllegalStateException {
		return WebUtils.getRequiredSessionAttribute(request, name);
	}
	
	//accessing and exporting form objects

	/**
	 * Gets the form object from the model, using the well-known attribute name
	 * {@link #FORM_OBJECT_ATTRIBUTE}.
	 * 
	 * @param model the flow model
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         model
	 */
	protected Object getFormObject(FlowModel model) throws IllegalStateException {
		return getRequiredFormObject(model, FORM_OBJECT_ATTRIBUTE);
	}

	/**
	 * Gets the form object from the model, using the well-known attribute name
	 * {@link #FORM_OBJECT_ATTRIBUTE}.
	 * 
	 * @param model the flow model
	 * @param formObjectClass the class of the form object, which will be
	 *        verified
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         model or is not of the required type
	 */
	protected Object getFormObject(FlowModel model, Class formObjectClass) throws IllegalStateException {
		return getRequiredFormObject(model, FORM_OBJECT_ATTRIBUTE, formObjectClass);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the model, using
	 * the name {@link #FORM_OBJECT_ERRORS_ATTRIBUTE}.
	 * 
	 * @param model the flow model
	 * @return the form object Errors tracker
	 * @throws IllegalStateException if the Errors instance is not found in the
	 *         model
	 */
	protected Errors getFormErrors(FlowModel model) throws IllegalStateException {
		return getRequiredFormErrors(model, null);
	}

	/**
	 * Gets the form object from the model, using the specified name.
	 * @param model the flow model
	 * @param formObjectName the name of the form object in the model
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         model
	 */
	protected Object getRequiredFormObject(FlowModel model, String formObjectName) throws IllegalStateException {
		return model.getRequiredAttribute(formObjectName);
	}

	/**
	 * Gets the form object from the model, using the specified name.
	 * @param model the flow model
	 * @param formObjectName the name of the form in the model
	 * @param formObjectClass the class of the form object, which will be
	 *        verified
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         model or is not of the required type
	 */
	protected Object getRequiredFormObject(FlowModel model, String formObjectName, Class formObjectClass)
			throws IllegalStateException {
		return model.getRequiredAttribute(formObjectName, formObjectClass);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the model, using
	 * the specified name.
	 * @param model The flow model
	 * @param formObjectErrorsName The name of the Errors object, which will be
	 *        prefixed with {@link BindException#ERROR_KEY_PREFIX}, may be
	 *        <code>null</code> at which time the value of the
	 *        {@link #FORM_OBJECT_ERRORS_ATTRIBUTE} attribute is returned
	 * @return The form object errors instance
	 * @throws IllegalStateException if the Errors instance is not found in the
	 *         model
	 */
	protected Errors getRequiredFormErrors(FlowModel model, String formObjectErrorsName) throws IllegalStateException {
		if (!StringUtils.hasText(formObjectErrorsName)) {
			return (Errors)model.getRequiredAttribute(FORM_OBJECT_ERRORS_ATTRIBUTE, Errors.class);
		}
		else {
			return (Errors)model.getRequiredAttribute(BindException.ERROR_KEY_PREFIX + formObjectErrorsName, Errors.class);
		}
	}

	/**
	 * Export a <i>new</i> errors instance to the flow model for the form
	 * object using name {@link #FORM_OBJECT_ATTRIBUTE}.
	 * 
	 * @param model The flow model
	 * @param formObject The form object to export an errors instance for
	 */
	protected void exportErrors(MutableFlowModel model, Object formObject) {
		exportErrors(model, formObject, FORM_OBJECT_ATTRIBUTE);
	}

	/**
	 * Export a <i>new</i> errors instance to the flow model for the form
	 * object with the specified form object name.
	 * @param model The flow model
	 * @param formObject The form object
	 * @param formObjectName The name of the form object
	 */
	protected void exportErrors(MutableFlowModel model, Object formObject, String formObjectName) {
		exportBindExceptionErrors(model, new BindException(formObject, formObjectName));
	}

	/**
	 * Internal helper to export form object error information.
	 */
	protected void exportBindExceptionErrors(MutableFlowModel model, BindException errors) {
		model.setAttribute(FORM_OBJECT_ATTRIBUTE, errors.getTarget());
		model.setAttribute(FORM_OBJECT_ERRORS_ATTRIBUTE, errors);
		model.setAttributes(errors.getModel());
	}
	
	//action pre and post execution logic

	public final String execute(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Action '" + getClass().getName() + "' beginning execution");
		}
		String result = onPreExecute(request, response, model);
		if (!StringUtils.hasText(result)) {
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
	 * Pre-action-execution hook, subclasses may override. If this method
	 * returns a non-<code>null</code> value, the
	 * <code>doExecuteAction()</code> method will <b>not</b> be called and
	 * the returned value will be used to select a transition to trigger in the
	 * calling action state. If this method returns <code>null</code>,
	 * <code>doExecuteAction()</code> will be called to obtain an action
	 * result.
	 * 
	 * <p>
	 * This implementation just returns <code>null</code>.
	 * 
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @return The non-<code>null</code> action result, in which case the
	 *         <code>doExecuteAction()</code> will not be called. Or
	 *         <code>null</code> if the <code>doExecuteAction()</code>
	 *         method should be called to obtain the action result.
	 * @throws Exception An <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected String onPreExecute(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		return null;
	}

	/**
	 * Template hook method subclasses should override to encapsulate their
	 * specific action execution logic.
	 * 
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @return The action result
	 * @throws Exception An <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected abstract String doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableFlowModel model) throws Exception;

	/**
	 * Post-action execution hook, subclasses may override.
	 * 
	 * <p>
	 * This implementation does nothing.
	 * 
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @throws Exception An <b>unrecoverable </b> exception occured, either
	 *         checked or unchecked
	 */
	protected void onPostExecute(HttpServletRequest request, HttpServletResponse response, FlowModel model)
			throws Exception {
	}
}