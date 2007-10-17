/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.servlet.mvc.generic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Juergen Hoeller
 */
public abstract class GenericFormController<T> implements Controller {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private WebBindingInitializer webBindingInitializer;


	/**
	 * Specify a WebBindingInitializer which will apply pre-configured
	 * configuration to every DataBinder that this controller uses.
	 * <p>Allows for factoring out the entire binder configuration
	 * to separate objects, as an alternative to {@link #initBinder}.
	 */
	public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}


	/**
	 * Handles two cases: form submissions and showing a new form.
	 * Delegates the decision between the two to {@link #isFormSubmission},
	 * always treating requests without existing form session attribute
	 * as new form when using session form mode.
	 * @see #isFormSubmission
	 * @see #showNewForm
	 * @see #processFormSubmission
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// Form submission or new form to show?
		if (isFormSubmission(request)) {
			// Fetch form object from HTTP session, bind, validate, process submission.
			try {
				return processFormSubmission(request);
			}
			catch (HttpSessionRequiredException ex) {
				// Cannot submit a session form if no form object is in the session.
				if (logger.isDebugEnabled()) {
					logger.debug("Invalid submit detected: " + ex.getMessage());
				}
				return handleInvalidSubmit(request);
			}
		}

		else {
			// New form to show: render form view.
			return showNewForm(request);
		}
	}

	/**
	 * Determine if the given request represents a form submission.
	 * <p>The default implementation treats a POST request as form submission.
	 * Note: If the form session attribute doesn't exist when using session form
	 * mode, the request is always treated as new form by handleRequestInternal.
	 * <p>Subclasses can override this to use a custom strategy, e.g. a specific
	 * request parameter (assumably a hidden field or submit button name).
	 * @param request current HTTP request
	 * @return if the request represents a form submission
	 */
	protected boolean isFormSubmission(HttpServletRequest request) {
		return "POST".equals(request.getMethod());
	}

	/**
	 * Return the name of the HttpSession attribute that holds the form object
	 * for this form controller.
	 * <p>Default is an internal name, of no relevance to applications, as the form
	 * session attribute is not usually accessed directly. Can be overridden to use
	 * an application-specific attribute name, which allows other code to access
	 * the session attribute directly.
	 * @param request current HTTP request
	 * @return the name of the form session attribute
	 * @see #getFormSessionAttributeName
	 * @see javax.servlet.http.HttpSession#getAttribute
	 */
	protected String getFormSessionAttributeName(HttpServletRequest request) {
		return getClass().getName() + ".FORM." + getFormObjectName(request);
	}


	/**
	 * Show a new form. Prepares a backing object for the current form
	 * and the given request, including checking its validity.
	 * @param request current HTTP request
	 * @return the prepared form view
	 * @throws Exception in case of an invalid new form object
	 */
	protected final ModelAndView showNewForm(HttpServletRequest request) throws Exception {
		logger.debug("Displaying new form");

		// Create form-backing object for new form.
		T formObject = formBackingObject(request);
		Assert.state(formObject != null, "Form object returned by formBackingObject() must not be null");

		// Bind without validation, to allow for prepopulating a form, and for
		// convenient error evaluation in views (on both first attempt and resubmit).
		ServletRequestDataBinder binder = createBinder(request, formObject);
		binder.bind(request);
		BindingResult bindingResult = binder.getBindingResult();
		onBind(request, formObject, bindingResult);

		return showForm(request, bindingResult);
	}


	protected ServletRequestDataBinder createBinder(HttpServletRequest request, T formObject) throws Exception {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(formObject, getFormObjectName(request));
		initBinder(request, binder);
		return binder;
	}

	/**
	 * Initialize the given binder instance, for example with custom editors.
	 * Called by {@link #createBinder}.
	 * <p>This method allows you to register custom editors for certain fields of your
	 * formObject class. For instance, you will be able to transform Date objects into a
	 * String pattern and back, in order to allow your JavaBeans to have Date properties
	 * and still be able to set and display them in an HTML interface.
	 * <p>The default implementation is empty.
	 * @param request current HTTP request
	 * @param binder the new binder instance
	 * @throws Exception in case of invalid state or arguments
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 * @see org.springframework.beans.propertyeditors.CustomDateEditor
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		if (this.webBindingInitializer != null) {
			this.webBindingInitializer.initBinder(binder, new ServletWebRequest(request));
		}
	}

	/**
	 * Bind the parameters of the given request to the given form object.
	 * @param request current HTTP request
	 * @param formObject the object to bind onto
	 * @throws Exception in case of invalid state or arguments
	 */
	protected void onBind(HttpServletRequest request, T formObject, BindingResult bindingResult) throws Exception {
	}


	/**
	 * Return the form object for the given request.
	 * <p>Calls {@link #formBackingObject} if not in session form mode.
	 * Else, retrieves the form object from the session. Note that the form object
	 * gets removed from the session, but it will be re-added when showing the
	 * form for resubmission.
	 * @param request current HTTP request
	 * @return object form to bind onto
	 * @throws org.springframework.web.HttpSessionRequiredException
	 * if a session was expected but no active session (or session form object) found
	 * @throws Exception in case of invalid state or arguments
	 * @see #formBackingObject
	 */
	protected T currentFormObject(HttpServletRequest request) throws Exception {
		// Session-form mode: retrieve form object from HTTP session attribute.
		HttpSession session = request.getSession(false);
		if (session == null) {
			throw new HttpSessionRequiredException("Must have session when trying to bind (in session-form mode)");
		}
		String formAttrName = getFormSessionAttributeName(request);
		T sessionFormObject = (T) session.getAttribute(formAttrName);
		if (sessionFormObject == null) {
			throw new HttpSessionRequiredException("Form object not found in session (in session-form mode)");
		}

		// Remove form object from HTTP session: we might finish the form workflow
		// in this request. If it turns out that we need to show the form view again,
		// we'll re-bind the form object to the HTTP session.
		if (logger.isDebugEnabled()) {
			logger.debug("Removing form session attribute [" + formAttrName + "]");
		}
		session.removeAttribute(formAttrName);

		return sessionFormObject;
	}


	/**
	 * Prepare model and view for the given form, including reference and errors,
	 * adding a controller-specific control model.
	 * <p>In session form mode: Re-puts the form object in the session when returning
	 * to the form, as it has been removed by getCommand.
	 * <p>Can be used in subclasses to redirect back to a specific form page.
	 * @param request current HTTP request
	 * @param bindingResult validation errors holder
	 * @return the prepared form view
	 */
	protected final ModelAndView showForm(HttpServletRequest request, BindingResult bindingResult) {
		// In session form mode, re-expose form object as HTTP session attribute.
		// Re-binding is necessary for proper state handling in a cluster,
		// to notify other nodes of changes in the form object.
		String formAttrName = getFormSessionAttributeName(request);
		if (logger.isDebugEnabled()) {
			logger.debug("Setting form session attribute [" + formAttrName + "] to: " + bindingResult.getTarget());
		}
		request.getSession().setAttribute(formAttrName, bindingResult.getTarget());

		// Fetch errors model as starting point, containing form object under
		// "formObjectName", and corresponding Errors instance under internal key.
		ModelMap model = new ModelMap().addAllObjects(bindingResult.getModel());

		// Merge reference data into model, if any.
		populateModel(request, model, bindingResult);

		// Trigger rendering of the specified view, using the final model.
		return new ModelAndView(getFormView(request), model);
	}

	/**
	 * Populate the model map for the given request, consisting of
	 * key-value pairs that will be exposed as model attributes to the view.
	 * <p>The default implementation is empty.
	 * Subclasses can override this to expose reference data used in the view.
	 * @param request current HTTP request
	 * @param model the ModelMap to populate
	 * @param bindingResult validation errors holder
	 * @see ModelAndView
	 */
	protected void populateModel(HttpServletRequest request, ModelMap model, BindingResult bindingResult) {
	}


	/**
	 * Handle an invalid submit request, e.g. when in session form mode but no form object
	 * was found in the session (like in case of an invalid resubmit by the browser).
	 * <p>The default implementation simply tries to resubmit the form with a new
	 * form object. This should also work if the user hit the back button, changed
	 * some form data, and resubmitted the form.
	 * <p>Note: To avoid duplicate submissions, you need to override this method.
	 * Either show some "invalid submit" message, or call {@link #showNewForm} for
	 * resetting the form (prepopulating it with the current values if "bindOnNewForm"
	 * is true). In this case, the form object in the session serves as transaction token.
	 * <pre>
	 * protected ModelAndView handleInvalidSubmit(HttpServletRequest request) throws Exception {
	 *   return showNewForm(request);
	 * }</pre>
	 * You can also show a new form but with special errors registered on it:
	 * <pre class="code">
	 * protected ModelAndView handleInvalidSubmit(HttpServletRequest request) throws Exception {
	 *   BindingResult bindingResult = getErrorsForNewForm(request);
	 *   errors.reject("duplicateFormSubmission", "Duplicate form submission");
	 *   return showForm(request, response, bindingResult);
	 * }</pre>
	 * @param request current HTTP request
	 * @return a prepared view, or <code>null</code> if handled directly
	 * @throws Exception in case of errors
	 * @see #showNewForm
	 * @see #showForm
	 */
	protected ModelAndView handleInvalidSubmit(HttpServletRequest request) throws Exception {
		return processFormSubmission(request);
	}


	/**
	 * This implementation calls {@link #showForm} in case of errors,
	 * and delegates to  {@link #onSubmit}'s variant else.
	 * <p>This can only be overridden to check for an action that should be executed
	 * without respect to binding errors, like a cancel action. To just handle successful
	 * submissions without binding errors, override the {@link #onSubmit} method.
	 * @see #showForm
	 * @see #onSubmit
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request) throws Exception {
		T formObject = formBackingObject(request);
		ServletRequestDataBinder binder = createBinder(request, formObject);
		binder.bind(request);
		BindingResult bindingResult = binder.getBindingResult();
		onBind(request, formObject, bindingResult);
		return processFormSubmission(request, formObject, bindingResult);
	}

	/**
	 * This implementation calls {@link #showForm} in case of errors,
	 * and delegates to  {@link #onSubmit}'s variant else.
	 * <p>This can only be overridden to check for an action that should be executed
	 * without respect to binding errors, like a cancel action. To just handle successful
	 * submissions without binding errors, override the {@link #onSubmit} method.
	 * @see #showForm
	 * @see #onSubmit
	 */
	protected ModelAndView processFormSubmission(
			HttpServletRequest request, T formObject, BindingResult bindingResult) throws Exception {

		if (bindingResult.hasErrors()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Data binding errors: " + bindingResult.getErrorCount());
			}
			return showForm(request, bindingResult);
		}
		else {
			logger.debug("No errors -> processing submit");
			ModelAndView mav = onSubmit(request, formObject, bindingResult);
			if (mav != null) {
				return mav;
			}
			else {
				return showForm(request, bindingResult);
			}
		}
	}


	/**
	 * Return the name of the form object in the model.
	 * The form object will be included in the model under this name.
	 * @param request current HTTP request
	 * @return the name of the form object
	 */
	protected abstract String getFormObjectName(HttpServletRequest request);

	/**
	 * Return the name of the view that should be used for form display.
	 * @param request current HTTP request
	 * @return the name of the form view
	 */
	protected abstract String getFormView(HttpServletRequest request);

	/**
	 * Retrieve a backing object for the current form from the given request.
	 * <p>The properties of the form object will correspond to the form field values
	 * in your form view. This object will be exposed in the model under the specified
	 * formObject name, to be accessed under that name in the view: for example, with
	 * a "spring:bind" tag. The default formObject name is "formObject".
	 * <p>Note that you need to activate session form mode to reuse the form-backing
	 * object across the entire form workflow. Else, a new instance of the form object
	 * class will be created for each submission attempt, just using this backing
	 * object as template for the initial form.
	 * @param request current HTTP request
	 * @return the backing object
	 * @throws Exception in case of invalid state or arguments
	 */
	protected abstract T formBackingObject(HttpServletRequest request) throws Exception;

	/**
	 * Submit callback with all parameters. Called in case of submit without errors
	 * reported by the registered validator, or on every submit if no validator.
	 * <p>Subclasses can override this to provide custom submission handling like storing
	 * the object to the database. Implementations can also perform custom validation and
	 * call showForm to return to the form. Do <i>not</i> implement multiple onSubmit
	 * methods: In that case, just this method will be called by the controller.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the form object and the Errors instance, under the specified form object
	 * name, as expected by the "spring:bind" tag.
	 * @param request current servlet request
	 * @param formObject form object with request parameters bound onto it
	 * @param bindingResult Errors instance without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or <code>null</code>
	 * @throws Exception in case of errors
	 * @see #onSubmit
	 * @see #showForm
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindingResult#getModel()
	 */
	protected abstract ModelAndView onSubmit(HttpServletRequest request, T formObject, BindingResult bindingResult)
			throws Exception;

}
