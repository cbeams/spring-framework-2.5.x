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

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.PropertyEditorRegistrar;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.flow.FlowModel;
import org.springframework.web.flow.MutableFlowModel;

/**
 * Base binding and validation action, which may be used as is, or specialized
 * as needed.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public class BindAndValidateAction extends AbstractAction {

	/**
	 * Default name used for a form object in flow scope.
	 */
	public static final String DEFAULT_FORM_OBJECT_NAME = "formObject";

	/**
	 * Constant result marker that indicates to the base BindAndValidate action
	 * that it should attempt to return a default <code>success</code> or
	 * <code>error</code> event. What event to return is calculated based on
	 * whether any errors were generated during the bind and validate process.
	 */
	protected static final String USE_DEFAULT_EVENT = null;

	private String formObjectName = DEFAULT_FORM_OBJECT_NAME;

	private Class formObjectClass;

	private PropertyEditorRegistrar propertyEditorRegistrar;

	private Validator[] validators;

	private boolean validateOnBinding = true;

	private boolean createFormObjectPerRequest;

	private MessageCodesResolver messageCodesResolver;

	/**
	 * Set the name of the formObject in the model. The formObject object will
	 * be included in the model under this name.
	 */
	public void setFormObjectName(String formObjectName) {
		this.formObjectName = formObjectName;
	}

	/**
	 * Return the name of the formObject in the model.
	 */
	public String getFormObjectName() {
		return this.formObjectName;
	}

	/**
	 * Set the formObject class for this controller. An instance of this class
	 * gets populated and validated on each request.
	 */
	public void setFormObjectClass(Class formObjectClass) {
		this.formObjectClass = formObjectClass;
	}

	/**
	 * Return the formObject class for this controller.
	 */
	public Class getFormObjectClass() {
		return this.formObjectClass;
	}

	/**
	 * Set a property editor registration strategy for this action's data
	 * binders.
	 * @param propertyEditorRegistrar
	 */
	public void setPropertyEditorRegistrar(PropertyEditorRegistrar propertyEditorRegistrar) {
		this.propertyEditorRegistrar = propertyEditorRegistrar;
	}

	/**
	 * Set the primary Validator for this action. The Validator must support the
	 * specified formObject class. If there are one or more existing validators
	 * set already when this method is called, only the specified validator will
	 * be kept. Use {@link #setValidators(Validator[])}to set multiple
	 * validators.
	 */
	public void setValidator(Validator validator) {
		setValidators(new Validator[] { validator });
	}

	/**
	 * @return the Validators for this controller.
	 */
	public Validator getValidator() {
		return (validators != null && validators.length > 0 ? validators[0] : null);
	}

	/**
	 * Set the Validators for this controller. The Validator must support the
	 * specified formObject class.
	 */
	public void setValidators(Validator[] validators) {
		this.validators = validators;
	}

	/**
	 * @return the primary Validator for this controller.
	 */
	public final Validator[] getValidators() {
		return validators;
	}

	/**
	 * Set if the Validator should get applied when binding.
	 */
	public void setValidateOnBinding(boolean validateOnBinding) {
		this.validateOnBinding = validateOnBinding;
	}

	/**
	 * Return if the Validator should get applied when binding.
	 */
	public boolean isValidateOnBinding() {
		return validateOnBinding;
	}

	/**
	 * Returns true if a new form object instance should be created per action
	 * execution request, false if the form object should be created once and
	 * then cached in flow-scope afterwards.
	 * @return true or false
	 */
	public boolean isCreateFormObjectPerRequest() {
		return createFormObjectPerRequest;
	}

	/**
	 * Set if we create a new form object instance everytime this action is
	 * invoked.
	 * @param createNewFormObjectPerRequest
	 */
	public final void setCreateFormObjectPerRequest(boolean createNewFormObjectPerRequest) {
		this.createFormObjectPerRequest = createNewFormObjectPerRequest;
	}

	/**
	 * Set the strategy to use for resolving errors into message codes. Applies
	 * the given strategy to all data binders used by this controller.
	 * <p>
	 * Default is null, i.e. using the default strategy of the data binder.
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#setMessageCodesResolver
	 */
	public final void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
	}

	/**
	 * Return the strategy to use for resolving errors into message codes.
	 */
	public final MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	public void afterPropertiesSet() {
		if (this.validators != null) {
			for (int i = 0; i < this.validators.length; i++) {
				if (this.formObjectClass != null && !this.validators[i].supports(this.formObjectClass))
					throw new IllegalArgumentException("Validator [" + this.validators[i]
							+ "] does not support formObject class [" + this.formObjectClass.getName() + "]");
			}
		}
	}

	protected String doExecuteAction(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		Object formObject = loadRequiredFormObject(request, model);
		ServletRequestDataBinder binder = createBinder(request, formObject, model);
		String result = bindAndValidate(request, model, binder);
		exportErrorsInternal(model, binder.getErrors());
		if (StringUtils.hasText(result)) {
			return result;
		}
		else {
			return getDefaultActionResult(request, model, formObject, binder.getErrors());
		}
	}

	/**
	 * Get the default action result for this bind and validate action; this
	 * implementation returns error() if the binder has errors, success()
	 * otherwise. Subclasses may overrride.
	 * @param request the http request
	 * @param model the flow data model
	 * @param formObject the form object
	 * @param errors possible binding errors
	 * @return the action result
	 */
	protected String getDefaultActionResult(HttpServletRequest request, MutableFlowModel model, Object formObject,
			BindException errors) {
		return errors.hasErrors() ? error() : success();
	}

	/**
	 * Load the backing form object that should be updated from incoming request
	 * parameters and validated. Throws an exception if the object could not be
	 * loaded.
	 * @param request The http request, allowing access to input
	 *        parameters/attributes needed to retrieve the form object.
	 * @param model The flow data model, allowing access to attributes needed to
	 *        retrieve the form object.
	 * @return The form object
	 * @throws IllegalStateException the form object loaded was null
	 * @throws ObjectRetrievalFailureException the form object could not be
	 *         loaded
	 */
	protected final Object loadRequiredFormObject(HttpServletRequest request, FlowModel model)
			throws IllegalStateException, ObjectRetrievalFailureException {
		try {
			// get the form object
			Object formObject = loadFormObject(request, model);
			Assert.state(formObject != null, "The loaded form object cannot be null");
			return formObject;
		}
		catch (ServletRequestBindingException e) {
			throw new ObjectRetrievalFailureException(getFormObjectClass(), getFormObjectName(),
					"Unable to obtain object identifier info from request", e);
		}
	}

	/**
	 * Load the backing form object that should be updated from incoming request
	 * input and validated. By default, will attempt to instantiate a new form
	 * object instance transiently in memory if not already present in the flow
	 * model (and the crateFormObjectPerRequest parameter is marked as false,
	 * the default.)
	 * <p>
	 * Subclasses should override if they need to load the form object from a
	 * specific location or resource such as a database or filesystem.
	 * @param request The http request, allowing access to input
	 *        parameters/attributes needed to retrieve the form object.
	 * @param model The flow data model, allowing access to attributes needed to
	 *        retrieve the form object.
	 * @return The form object
	 * @throws ObjectRetrievalFailureException the form object could not be
	 *         loaded
	 * @throws ServletRequestBindingException the form object could not be
	 *         loaded because valid input was not provided in the request
	 */
	protected Object loadFormObject(HttpServletRequest request, FlowModel model)
			throws ObjectRetrievalFailureException, ServletRequestBindingException {
		if (!isCreateFormObjectPerRequest() && model.containsAttribute(getFormObjectName())) {
			Object formObject = model.getAttribute(getFormObjectName(), getFormObjectClass());
			if (logger.isDebugEnabled()) {
				logger.debug("Binding to existing form object '" + getFormObjectName() + "' in flow scope by name");
			}
			return formObject;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new form object '" + getFormObjectName() + "'");
			}
			try {
				return createFormObject(request, model);
			}
			catch (InstantiationException e) {
				throw new ObjectRetrievalFailureException(getFormObjectClass(), getFormObjectName(),
						"Unable to instantiate form object", e);
			}
			catch (IllegalAccessException e) {
				throw new ObjectRetrievalFailureException(getFormObjectClass(), getFormObjectName(),
						"Unable to access form object class constructor", e);
			}
		}
	}

	/**
	 * Create a new formObject instance for the formObject class of this
	 * controller.
	 * @return the new formObject instance
	 * @throws InstantiationException if the formObject class could not be
	 *         instantiated
	 * @throws IllegalAccessException if the class or its constructor is not
	 *         accessible
	 */
	protected Object createFormObject(HttpServletRequest request, FlowModel model) throws InstantiationException,
			IllegalAccessException, ServletRequestBindingException {
		if (this.formObjectClass == null) {
			throw new IllegalStateException("Cannot create formObject without formObjectClass being set - "
					+ "either set formObjectClass, override loadFormObject, or override this method");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new formObject of class [" + this.formObjectClass.getName() + "]");
		}
		return this.formObjectClass.newInstance();
	}

	/**
	 * Bind the parameters of the given request to the form object in the model.
	 * @param request current HTTP request
	 * @param model the flow data model
	 * @param binder the binder to use for binding
	 * @return the action result outcome
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final String bindAndValidate(HttpServletRequest request, MutableFlowModel model,
			ServletRequestDataBinder binder) {
		if (logger.isDebugEnabled()) {
			logger.debug("Binding allowed matching request parameters to object '" + binder.getObjectName()
					+ "', details='" + binder.getTarget() + "'");
		}
		binder.bind(request);
		onBind(request, model, binder.getTarget(), binder.getErrors());
		if (logger.isDebugEnabled()) {
			logger.debug("After bind of object '" + binder.getObjectName() + "', details='" + binder.getTarget() + "'");
		}
		if (this.validators != null && isValidateOnBinding() && !suppressValidation(request)) {
			for (int i = 0; i < this.validators.length; i++) {
				ValidationUtils.invokeValidator(this.validators[i], binder.getTarget(), binder.getErrors());
			}
		}
		return onBindAndValidate(request, model, binder.getTarget(), binder.getErrors());
	}

	/**
	 * Create a new binder instance for the given form object and request.
	 * Called by doExecuteAction. Can be overridden to plug in custom
	 * ServletRequestDataBinder subclasses.
	 * <p>
	 * Default implementation creates a standard ServletRequestDataBinder, and
	 * invokes initBinder. Note that initBinder will not be invoked if you
	 * override this method!
	 * @param request current HTTP request
	 * @param formObject the command to bind onto
	 * @param model the flow model
	 * @return the new binder instance
	 * @see #initBinder
	 */
	protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object formObject, FlowModel model) {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(formObject, getFormObjectName());
		if (this.messageCodesResolver != null) {
			binder.setMessageCodesResolver(this.messageCodesResolver);
		}
		initBinder(request, model, binder);
		return binder;
	}

	/**
	 * <p>
	 * Initialize the given binder instance, for example with custom editors.
	 * Called by createBinder.
	 * </p>
	 * <p>
	 * This method allows you to register custom editors for certain fields of
	 * your form object. For instance, you will be able to transform Date
	 * objects into a String pattern and back, in order to allow your JavaBeans
	 * to have Date properties and still be able to set and display them in an
	 * HTML interface.
	 * </p>
	 * <p>
	 * Default implementation will simply call registerCustomEditors on any
	 * propertyEditorRegistrar object that has been set for the action.
	 * <p>
	 * The flow model may be used to feed reference data to any property
	 * editors, although it may be better (in the interest of not bloating the
	 * session, to put have the editors get this from somewhere else
	 * </p>
	 * @param request current HTTP request
	 * @param model the flow model
	 * @param binder new binder instance
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 */
	protected void initBinder(HttpServletRequest request, FlowModel model, ServletRequestDataBinder binder) {
		if (propertyEditorRegistrar != null) {
			propertyEditorRegistrar.registerCustomEditors(binder);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No property editor registrar set, no custom editors to register");
			}
		}
	}

	/**
	 * Callback for custom post-processing in terms of binding. Called on each
	 * submit, after standard binding but before validation.
	 * <p>
	 * Default implementation delegates to onBind(request, formObject).
	 * @param request current HTTP request
	 * @param model the flow model
	 * @param formObject the formObject object to perform further binding on
	 * @param errors validation errors holder, allowing for additional custom
	 *        registration of binding errors
	 * @see #bindAndValidate
	 */
	protected void onBind(HttpServletRequest request, MutableFlowModel model, Object formObject, BindException errors) {
		onBind(request, model, formObject);
	}

	/**
	 * Callback for custom post-processing in terms of binding. Called by the
	 * default implementation of the onBind version with all parameters, after
	 * standard binding but before validation.
	 * <p>
	 * Default implementation is empty.
	 * @param request current HTTP request
	 * @param model the flow model
	 * @param formObject the formObject object to perform further binding on
	 */
	protected void onBind(HttpServletRequest request, MutableFlowModel model, Object formObject) {
	}

	/**
	 * Return whether to suppress validation for the given request. Default
	 * implementations always returns false.
	 * @param request current HTTP request
	 * @return whether to suppress validation for the given request
	 */
	protected boolean suppressValidation(HttpServletRequest request) {
		return false;
	}

	/**
	 * Callback for custom post-processing in terms of binding and validation.
	 * Called on each submit, after standard binding and validation, but before
	 * error evaluation.
	 * <p>
	 * Default implementation is empty.
	 * @param request current HTTP request
	 * @param formObject the formObject object, still allowing for further
	 *        binding
	 * @param errors validation errors holder, allowing for additional custom
	 *        validation
	 * @see #bindAndValidate
	 * @see org.springframework.validation.Errors
	 */
	protected String onBindAndValidate(HttpServletRequest request, MutableFlowModel model, Object formObject,
			BindException errors) {
		if (!errors.hasErrors()) {
			return onBindAndValidateSuccess(request, model, formObject, errors);
		}
		return null;
	}

	/**
	 * Hook called when binding and validation completes successfully;
	 * subclasses may optionally return a ActionBeanEvent to supercede the
	 * default result event, which will be success().
	 * @param request the http request
	 * @param model the flow data model
	 * @param formObject the form object
	 * @param errors the possible binding errors
	 * @return the action result
	 */
	protected String onBindAndValidateSuccess(HttpServletRequest request, MutableFlowModel model, Object formObject,
			BindException errors) {
		return onBindAndValidateSuccess(request, model, formObject);
	}

	/**
	 * Hook called when binding and validation completes successfully;
	 * subclasses may optionally return a ActionBeanEvent to supercede the
	 * default result event, which will be success().
	 * 
	 * @param request the http request
	 * @param model the flow data model
	 * @param formObject the form object
	 * @return the action result
	 */
	protected String onBindAndValidateSuccess(HttpServletRequest request, MutableFlowModel model, Object formObject) {
		return null;
	}

}