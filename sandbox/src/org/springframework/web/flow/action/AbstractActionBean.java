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
import org.springframework.web.flow.ActionBean;
import org.springframework.web.flow.ActionBeanEvent;
import org.springframework.web.flow.AttributesAccessor;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.MutableAttributesAccessor;
import org.springframework.web.util.WebUtils;

/**
 * @author Keith Donald
 */
public abstract class AbstractActionBean implements ActionBean, InitializingBean {

	public static final String SUCCESS_EVENT_ID = "success";

	public static final String ERROR_EVENT_ID = "error";

	public static final String SEARCH_EVENT_ID = "search";

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

	/**
	 *  
	 */
	public void afterPropertiesSet() {
		initAction();
	}

	/**
	 *  
	 */
	protected void initAction() {

	}

	protected ActionBeanEvent success() {
		return new ActionBeanEvent(this, Flow.SUCCESS);
	}

	protected ActionBeanEvent error() {
		return new ActionBeanEvent(this, Flow.ERROR);
	}

	protected ActionBeanEvent add() {
		return new ActionBeanEvent(this, Flow.ADD);
	}

	protected ActionBeanEvent search() {
		return new ActionBeanEvent(this, Flow.SEARCH);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 */
	protected String getStringParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getStringParameter(request, parameterName, null);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 */
	protected short getShortParameter(HttpServletRequest request, String parameterName) {
		return (short)RequestUtils.getIntParameter(request, parameterName, -1);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 */
	protected int getIntParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getIntParameter(request, parameterName, -1);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 */
	protected long getLongParameter(HttpServletRequest request, String parameterName) {
		return RequestUtils.getLongParameter(request, parameterName, -1);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 * @throws ServletRequestBindingException
	 */
	protected String getRequiredStringParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredStringParameter(request, parameterName);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 * @throws ServletRequestBindingException
	 */
	protected short getRequiredShortParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return (short)RequestUtils.getRequiredIntParameter(request, parameterName);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 * @throws ServletRequestBindingException
	 */
	protected int getRequiredIntParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredIntParameter(request, parameterName);
	}

	/**
	 * @param request
	 * @param parameterName
	 * @return
	 * @throws ServletRequestBindingException
	 */
	protected long getRequiredLongParameter(HttpServletRequest request, String parameterName)
			throws ServletRequestBindingException {
		return RequestUtils.getRequiredLongParameter(request, parameterName);
	}

	/**
	 * @param request
	 * @param name
	 * @return
	 */
	protected Object getRequiredSessionAttribute(HttpServletRequest request, String name) {
		return WebUtils.getRequiredSessionAttribute(request, name);
	}

	/**
	 * @param request
	 * @param name
	 * @return
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
	protected Object getLocalFormObject(AttributesAccessor model, Class formObjectClass) {
		return model.getRequiredAttribute(LOCAL_FORM_OBJECT_NAME, formObjectClass);
	}

	/**
	 * @param model
	 * @return
	 */
	protected Object getLocalFormObject(AttributesAccessor model) {
		return model.getRequiredAttribute(LOCAL_FORM_OBJECT_NAME);
	}

	/**
	 * @param model
	 * @return
	 */
	protected Errors getLocalFormErrors(AttributesAccessor model) {
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
	 * @param model
	 * @param formObjectName
	 * @return
	 */
	protected Object getRequiredFormObject(AttributesAccessor model, String formObjectName) {
		return model.getRequiredAttribute(formObjectName);
	}

	/**
	 * @param model
	 * @param formObjectName
	 * @return
	 */
	protected Errors getRequiredFormErrors(AttributesAccessor model, String formObjectName) {
		return (Errors)model.getRequiredAttribute(BindException.ERROR_KEY_PREFIX + formObjectName, Errors.class);
	}

	/**
	 * @param model
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	protected MutableAttributesAccessor export(MutableAttributesAccessor model, String attributeName, Object attributeValue) {
		model.setAttribute(attributeName, attributeValue);
		return model;
	}

	/*
	 *
	 */
	public final ActionBeanEvent execute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException {
		if (logger.isDebugEnabled()) {
			logger.debug("Action bean '" + getClass().getName() + "' beginning execution");
		}
		try {
			ActionBeanEvent event = onPreExecute(request, response, model);
			if (event == null) {
				event = doExecuteAction(request, response, model);
				if (logger.isDebugEnabled()) {
					logger.debug("Action bean '" + getClass().getName() + "' completed execution; event result is "
							+ event);
				}
				onPostExecute(request, response, model);
				if (logger.isInfoEnabled()) {
					if (event == null) {
						logger
								.info("Retured action bean event is [null]; that's ok so long as another action associated "
										+ "with the currently executing flow state returns a valid event");
					}
				}
			}
			else {
				if (logger.isInfoEnabled()) {
					logger.info("Action execution disallowed; event is " + event);
				}
			}
			return event;
		}
		catch (ServletRequestBindingException e) {
			throw new RuntimeException("Unexpected exception occured binding well-known parameters from request", e);
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws RuntimeException
	 * @throws ServletRequestBindingException
	 */
	protected ActionBeanEvent onPreExecute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException, ServletRequestBindingException {
		return null;
	}

	/**
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws RuntimeException
	 * @throws ServletRequestBindingException
	 */
	protected abstract ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException, ServletRequestBindingException;

	/**
	 * @param request
	 * @param response
	 * @param model
	 * @throws RuntimeException
	 * @throws ServletRequestBindingException
	 */
	protected void onPostExecute(HttpServletRequest request, HttpServletResponse response, AttributesAccessor model)
			throws RuntimeException, ServletRequestBindingException {
	}
}