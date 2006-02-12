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

package org.springframework.web.servlet.mvc.form;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Concrete FormController implementation that provides configurable
 * form and success views, and an processFinish chain for convenient overriding.
 * Automatically resubmits to the form view in case of validation errors,
 * and renders the success view in case of a valid submission.</p>
 *
 * <p>The workflow of this Controller does not differ much from the one described
 * in the {@link org.springframework.web.servlet.mvc.AbstractFormController AbstractFormController}. The difference
 * is that you do not need to implement {@link #showForm showForm} and
 * {@link #processFormSubmission processFormSubmission}: A form view and a
 * success view can be configured declaratively.</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="AbstractFormController.html#workflow">in addition to the superclass</a>):</b><br>
 * <ol>
 *  <li>Call to {@link #processFormSubmission processFormSubmission} which inspects
 *      the {@link org.springframework.validation.Errors Errors} object to see if
 *      any errors have occurred during binding and validation.</li>
 *  <li>If errors occured, the controller will return the configured formView,
 *      showing the form again (possibly rendering according error messages).</li>
 *  <li>If {@link #isFormChangeRequest isFormChangeRequest} is overridden and returns
 *      true for the given request, the controller will return the formView too.
 *      In that case, the controller will also suppress validation. Before returning the formView,
 *      the controller will invoke {@link #onFormChange}, giving sub-classes a chance
 *      to make modification to the command object.
 *      This is intended for requests that change the structure of the form,
 *      which should not cause validation and show the form in any case.</li>
 *  <li>If no errors occurred, the controller will call
 *      {@link #onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException) processFinish}
 *      using all parameters, which in case of the default implementation delegates to
 *      {@link #onSubmit(Object, org.springframework.validation.BindException) processFinish} with just the command object.
 *      The default implementation of the latter method will return the configured
 *      <code>successView</code>. Consider implementing {@link #doSubmitAction} doSubmitAction
 *      for simply performing a submit action and rendering the success view.</li>
 *  </ol>
 * </p>
 *
 * <p>The submit behavior can be customized by overriding one of the
 * {@link #processFinish processFinish} methods. Submit actions can also perform
 * custom validation if necessary (typically database-driven checks), calling
 * {@link #showForm(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.validation.BindingResult) showForm}
 * in case of validation errors to show the form view again.</p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="AbstractFormController.html#config">and those defined by superclass</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></td>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>formView</td>
 *      <td><i>null</i></td>
 *      <td>Indicates what view to use when the user asks for a new form
 *          or when validation errors have occurred on form submission.</td>
 *  </tr>
 *  <tr>
 *      <td>successView</td>
 *      <td><i>null</i></td>
 *      <td>Indicates what view to use when successful form submissions have
 *          occurred. Such a success view could e.g. display a submission summary.
 *          More sophisticated actions can be implemented by overriding one of
 *          the {@link #onSubmit(Object) processFinish()} methods.</td>
 *  </tr>
 * <table>
 * </p>
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 05.05.2003
 */
public abstract class SimpleFormController extends AbstractFormController {

	private String formView;

	private String successView;


	/**
	 * Create a new SimpleFormController.
	 * <p>Subclasses should set the following properties, either in the constructor
	 * or via a BeanFactory: commandName, commandClass, sessionForm, formView,
	 * successView. Note that commandClass doesn't need to be set when overriding
	 * <code>formBackingObject</code>, as this determines the class anyway.
	 * @see #setFormObjectName
	 * @see #setSessionForm
	 * @see #setFormView
	 * @see #setSuccessView
	 * @see #formBackingObject
	 */
	public SimpleFormController() {
		// AbstractFormController sets default cache seconds to 0.
		super();
	}

	/**
	 * Set the name of the view that should be used for form display.
	 */
	public final void setFormView(String formView) {
		this.formView = formView;
	}

	/**
	 * Return the name of the view that should be used for form display.
	 */
	public final String getFormView() {
		return this.formView;
	}

	/**
	 * Set the name of the view that should be shown on successful submit.
	 */
	public final void setSuccessView(String successView) {
		this.successView = successView;
	}

	/**
	 * Return the name of the view that should be shown on successful submit.
	 */
	public final String getSuccessView() {
		return this.successView;
	}


	/**
	 * This implementation shows the configured form view, delegating to the
	 * analogous showForm version with a controlModel argument.
	 * <p>Can be called within processFinish implementations, to redirect back to the form
	 * in case of custom validation errors (i.e. not determined by the validator).
	 * <p>Can be overridden in subclasses to show a custom view, writing directly
	 * to the response or preparing the response before rendering a view.
	 * <p>If calling showForm with a custom control model in subclasses, it's preferable
	 * to override the analogous showForm version with a controlModel argument
	 * (which will handle both standard form showing and custom form showing then).
	 * @see #setFormView
	 */
	protected ModelAndView showForm(HttpServletRequest request, BindingResult errors) throws Exception {
		return showForm(request, errors, getFormView(), null);
	}

	/**
	 * This implementation calls <code>showForm</code> in case of errors,
	 * and delegates to <code>processFinish</code>'s full version else.
	 * <p>This can only be overridden to check for an action that should be executed
	 * without respect to binding errors, like a cancel action. To just handle successful
	 * submissions without binding errors, override one of the <code>processFinish</code>
	 * methods or <code>doSubmitAction</code>.
	 * @see #showForm(javax.servlet.http.HttpServletRequest, org.springframework.validation.BindingResult)
	 * @see #processFinish(javax.servlet.http.HttpServletRequest, org.springframework.validation.BindingResult)
	 * @see #doSubmitAction(Object)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, BindingResult bindingResult)
			throws Exception {

		if (bindingResult.hasErrors()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Data binding errors: " + bindingResult.getErrorCount());
			}
			return showForm(request, bindingResult);
		}
		else if (isFormChangeRequest(request)) {
			logger.debug("Detected form change request -> routing request to onFormChange");
			onFormChange(request, bindingResult);
			return showForm(request, bindingResult);
		}
		else {
			logger.debug("No errors -> processing submit");
			return processFinish(request, bindingResult);
		}
	}

	/**
	 * This implementation delegates to <code>isFormChangeRequest</code>:
	 * A form change request changes the appearance of the form
	 * and should not get validated but just show the new form.
	 * @see #isFormChangeRequest
	 */
	protected boolean suppressValidation(HttpServletRequest request) {
		return isFormChangeRequest(request);
	}

	/**
	 * Determine whether the given request is a form change request.
	 * A form change request changes the appearance of the form
	 * and should always show the new form, without validation.
	 * <p>Gets called by suppressValidation and processFormSubmission.
	 * Consequently, this single method determines to suppress validation
	 * <i>and</i> to show the form view in any case.
	 * @param request current HTTP request
	 * @return whether the given request is a form change request
	 * @see #suppressValidation
	 * @see #processFormSubmission
	 */
	protected boolean isFormChangeRequest(HttpServletRequest request) {
		return false;
	}

	/**
	 * Called during form submission if
	 * {@link #isFormChangeRequest(javax.servlet.http.HttpServletRequest)}
	 * returns <code>true</code>. Allows subclasses to implement custom logic
	 * to modify the command object to directly modify data in the form.
	 * <p>Default implementation is empty.
	 * @param request current servlet request
	 * @param bindingResult validation errors holder, containing
	 * form object with request parameters bound onto it
	 * @throws Exception in case of errors
	 * @see #isFormChangeRequest(javax.servlet.http.HttpServletRequest)
	 */
	protected void onFormChange(HttpServletRequest request, BindingResult bindingResult)
			throws Exception {
	}


	/**
	 * Submit callback with all parameters. Called in case of submit without errors
	 * reported by the registered validator, or on every submit if no validator.
	 * <p>Default implementation delegates to <code>processFinish(Object, BindException)</code>.
	 * For simply performing a submit action and rendering the specified success
	 * view, consider implementing <code>doSubmitAction</code> rather than an
	 * <code>processFinish</code> version.
	 * <p>Subclasses can override this to provide custom submission handling like storing
	 * the object to the database. Implementations can also perform custom validation and
	 * call showForm to return to the form. Do <i>not</i> implement multiple processFinish
	 * methods: In that case, just this method will be called by the controller.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param request current servlet request
	 * @param bindingResult validation errors holder, containing
	 * form object with request parameters bound onto it
	 * @return the prepared model and view, or <code>null</code>
	 * @throws Exception in case of errors
	 * @see #doSubmitAction
	 * @see #showForm
	 * @see org.springframework.validation.BindingResult#getModel
	 */
	protected ModelAndView processFinish(HttpServletRequest request, BindingResult bindingResult) throws Exception {
		doSubmitAction(bindingResult.getTarget());
		return new ModelAndView(getSuccessView(), bindingResult.getModel());
	}

	/**
	 * Template method for submit actions. Called by the default implementation
	 * of the simplest processFinish version.
	 * <p><b>This is the preferred submit callback to implement if you want to
	 * perform an action (like storing changes to the database) and then render
	 * the success view with the command and Errors instance as model.</b>
	 * You don't need to care about the success ModelAndView here.
	 * @param command form object with request parameters bound onto it
	 * @throws Exception in case of errors
	 * @see #setSuccessView
	 */
	protected void doSubmitAction(Object command) throws Exception {
	}

}
