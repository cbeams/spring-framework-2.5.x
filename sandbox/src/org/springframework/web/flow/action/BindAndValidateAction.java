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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.flow.ActionBean;
import org.springframework.web.flow.ActionBeanEvent;
import org.springframework.web.flow.AttributesAccessor;
import org.springframework.web.flow.MutableAttributesAccessor;

/**
 * Base binding and validation action, which may be used as is, or specialized
 * as needed.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class BindAndValidateAction extends AbstractActionBean implements InitializingBean {

	private String formObjectName = DEFAULT_FORM_OBJECT_NAME;

	private Class formObjectClass;

	private PropertyEditorRegistrar propertyEditorRegistrar;

	private Validator[] validators;

	private boolean validateOnBinding = true;

	private boolean createFormObjectPerRequest;

	private MessageCodesResolver messageCodesResolver;

	private static final ActionBean ACTION_BEAN_NULL_OBJECT = new ActionBean() {
		public ActionBeanEvent execute(HttpServletRequest request, HttpServletResponse response,
				MutableAttributesAccessor model) {
			return null;
		}
	};

	protected static final ActionBeanEvent USE_DEFAULT_EVENT = new ActionBeanEvent(ACTION_BEAN_NULL_OBJECT, null);

	/**
	 * Set the name of the formObject in the model. The formObject object will
	 * be included in the model under this name.
	 */
	public final void setFormObjectName(String formObjectName) {
		this.formObjectName = formObjectName;
	}

	/**
	 * Return the name of the formObject in the model.
	 */
	public final String getFormObjectName() {
		return this.formObjectName;
	}

	/**
	 * Set the formObject class for this controller. An instance of this class
	 * gets populated and validated on each request.
	 */
	public final void setFormObjectClass(Class formObjectClass) {
		this.formObjectClass = formObjectClass;
	}

	/**
	 * Return the formObject class for this controller.
	 */
	public final Class getFormObjectClass() {
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
	public final void setValidator(Validator validator) {
		setValidators(new Validator[] { validator });
	}

	/**
	 * @return the Validators for this controller.
	 */
	public final Validator getValidator() {
		return (validators != null && validators.length > 0 ? validators[0] : null);
	}

	/**
	 * Set the Validators for this controller. The Validator must support the
	 * specified formObject class.
	 */
	public final void setValidators(Validator[] validators) {
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
	public final void setValidateOnBinding(boolean validateOnBinding) {
		this.validateOnBinding = validateOnBinding;
	}

	/**
	 * Return if the Validator should get applied when binding.
	 */
	public final boolean isValidateOnBinding() {
		return validateOnBinding;
	}

	public final boolean isCreateFormObjectPerRequest() {
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

	protected ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws ObjectRetrievalFailureException, IllegalStateException {
		Object formObject = loadRequiredFormObject(request, model);
		ServletRequestDataBinder binder = createBinder(request, formObject, model);
		ActionBeanEvent event = bindAndValidate(request, model, binder);
		exportErrors(binder.getErrors(), model);
		if (event != null && event != USE_DEFAULT_EVENT) {
			return event;
		}
		else {
			return getDefaultResultEvent(request, model, formObject, binder.getErrors());
		}
	}

	protected ActionBeanEvent getDefaultResultEvent(HttpServletRequest request, MutableAttributesAccessor model,
			Object formObject, BindException errors) {
		return errors.hasErrors() ? error() : success();
	}

	public static void exportErrors(BindException errors, MutableAttributesAccessor model) {
		// and also bind it under the local (to flow) alias, so other
		// actions can find it easily
		model.setAttribute(LOCAL_FORM_OBJECT_NAME, errors.getTarget());
		model.setAttribute(LOCAL_FORM_OBJECT_ERRORS_NAME, errors);
		model.setAttributes(errors.getModel());
	}

	protected final Object loadRequiredFormObject(HttpServletRequest request, AttributesAccessor model) {
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
	 * Retrieve a form object for the given request.
	 * @param request current HTTP request
	 * @return object formObject to bind onto
	 * @see #createFormObject
	 */
	protected Object loadFormObject(HttpServletRequest request, AttributesAccessor model)
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
	protected Object createFormObject(HttpServletRequest request, AttributesAccessor model)
			throws InstantiationException, IllegalAccessException, ServletRequestBindingException {
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
	 * Bind the parameters of the given request to the given formObject object.
	 * @param request current HTTP request
	 * @param formObject the formObject to bind onto
	 * @return the ServletRequestDataBinder instance for additional custom
	 *         validation
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ActionBeanEvent bindAndValidate(HttpServletRequest request, MutableAttributesAccessor model,
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
	protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object formObject,
			AttributesAccessor model) {
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
	 * @param binder new binder instance
	 * @param model the flow model
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 * @see org.springframework.beans.propertyeditors.CustomDateEditor
	 */
	protected void initBinder(HttpServletRequest request, AttributesAccessor model, ServletRequestDataBinder binder) {
		if (propertyEditorRegistrar != null) {
			propertyEditorRegistrar.registerCustomEditors(binder, model);
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("No property editor registrar set, no custom editors to register");
			}
		}
	}

	/**
	 * Callback for custom post-processing in terms of binding. Called on each
	 * submit, after standard binding but before validation.
	 * <p>
	 * Default implementation delegates to onBind(request, formObject).
	 * @param request current HTTP request
	 * @param the flow model
	 * @param formObject the formObject object to perform further binding on
	 * @param errors validation errors holder, allowing for additional custom
	 *        registration of binding errors
	 * @throws Exception in case of invalid state or arguments
	 * @see #bindAndValidate
	 * @see #onBind(HttpServletRequest, Object)
	 */
	protected void onBind(HttpServletRequest request, MutableAttributesAccessor model, Object formObject,
			BindException errors) {
		onBind(request, model, formObject);
	}

	/**
	 * Callback for custom post-processing in terms of binding. Called by the
	 * default implementation of the onBind version with all parameters, after
	 * standard binding but before validation.
	 * <p>
	 * Default implementation is empty.
	 * @param request current HTTP request
	 * @param the flow model
	 * @param formObject the formObject object to perform further binding on
	 * @throws Exception in case of invalid state or arguments
	 * @see #onBind(HttpServletRequest, Object, BindException)
	 */
	protected void onBind(HttpServletRequest request, MutableAttributesAccessor model, Object formObject) {
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
	 * @throws Exception in case of invalid state or arguments
	 * @see #bindAndValidate
	 * @see org.springframework.validation.Errors
	 */
	protected ActionBeanEvent onBindAndValidate(HttpServletRequest request, MutableAttributesAccessor model,
			Object formObject, BindException errors) {
		if (!errors.hasErrors()) {
			return onBindAndValidateSuccess(request, model, formObject, errors);
		}
		return USE_DEFAULT_EVENT;
	}

	protected ActionBeanEvent onBindAndValidateSuccess(HttpServletRequest request, MutableAttributesAccessor model,
			Object formObject, BindException errors) {
		return onBindAndValidateSuccess(request, model, formObject, errors);
	}

	protected ActionBeanEvent onBindAndValidateSuccess(HttpServletRequest request, MutableAttributesAccessor model,
			Object formObject) {
		return USE_DEFAULT_EVENT;
	}

}