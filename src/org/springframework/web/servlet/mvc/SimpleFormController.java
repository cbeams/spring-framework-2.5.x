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

package org.springframework.web.servlet.mvc;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Concrete FormController implementation that provides configurable
 * form and success views, and an onSubmit chain for convenient overriding.
 * Automatically resubmits to the form view in case of validation errors,
 * and renders the success view in case of a valid submission.</p>
 *
 * <p>The workflow of this Controller does not differ much from the one described
 * in the {@link AbstractFormController AbstractFormController}. The difference
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
 *      In that case, the controller will also suppress validation.
 *      This is intended for requests that change the structure of the form,
 *      which should not cause validation and show the form in any case.</li>
 *  <li>If no errors occurred, the controller will call
 *      {@link #onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException) onSubmit}
 *      using all parameters, which in case of the default implementation delegates to
 *      {@link #onSubmit(Object, BindException) onSubmit} with just the command object.
 *      The default implementation of the latter method will return the configured
 *      successView. Consider implementing {@link #doSubmitAction} doSubmitAction
 *      for simply performing a submit action and rendering the success view.</li>
 *  </ol>
 * </p>
 *
 * <p>The submit behavior can be customized by overriding one of the
 * {@link #onSubmit onSubmit} methods. Submit actions can also perform
 * custom validation if necessary (typically database-driven checks), calling
 * {@link #showForm(HttpServletRequest, HttpServletResponse, BindException) showForm}
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
 *          the {@link #onSubmit(Object) onSubmit()} methods.</td>
 *  </tr>
 * <table>
 * </p>
 *
 * @author Juergen Hoeller
 * @since 05.05.2003
 */
public class SimpleFormController extends AbstractFormController {

	private String formView;

	private String successView;


	/**
	 * Create a new SimpleFormController.
	 * <p>Subclasses should set the following properties, either in the constructor
	 * or via a BeanFactory: commandName, commandClass, sessionForm, formView,
	 * successView. Note that commandClass doesn't need to be set when overriding
	 * <code>formBackingObject</code>, as this determines the class anyway.
	 * @see #setCommandClass
	 * @see #setCommandName
	 * @see #setSessionForm
	 * @see #setFormView
	 * @see #setSuccessView
	 * @see #formBackingObject
	 */
	public SimpleFormController() {
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
	 * Create a reference data map for the given request and command,
	 * consisting of bean name/bean instance pairs as expected by ModelAndView.
	 * <p>Default implementation delegates to referenceData(request).
	 * Subclasses can override this to set reference data used in the view.
	 * @param request current HTTP request
	 * @param command form object with request parameters bound onto it
	 * @param errors validation errors holder
	 * @return a Map with reference data entries, or null if none
	 * @throws Exception in case of invalid state or arguments
	 * @see ModelAndView
	 */
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		return referenceData(request);
	}

	/**
	 * Create a reference data map for the given request.
	 * Called by referenceData version with all parameters.
	 * <p>Default implementation returns null.
	 * Subclasses can override this to set reference data used in the view.
	 * @param request current HTTP request
	 * @return a Map with reference data entries, or null if none
	 * @throws Exception in case of invalid state or arguments
	 * @see #referenceData(HttpServletRequest, Object, Errors)
	 * @see ModelAndView
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		return null;
	}

	/**
	 * This implementation shows the configured form view.
	 * Can be called within onSubmit implementations, to redirect back to the form
	 * in case of custom validation errors (i.e. not determined by the validator).
	 * @see #setFormView
	 */
	protected ModelAndView showForm(
			HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
		return showForm(request, errors, getFormView());
	}

	/**
	 * This implementation delegates to isFormChangeRequest:
	 * A form change request changes the appearance of the form
	 * and should not get validated but just show the new form.
	 * @see #isFormChangeRequest
	 */
	protected final boolean suppressValidation(HttpServletRequest request) {
		return isFormChangeRequest(request);
	}

	/**
	 * This implementation calls showForm in case of errors,
	 * and delegates to onSubmit's full version else.
	 * <p>This can only be overridden to check for an action that should be executed
	 * without respect to binding errors, like a cancel action. To just handle successful
	 * submissions without binding errors, override one of the onSubmit methods.
	 * @see #showForm
	 * @see #onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)
	 */
	protected ModelAndView processFormSubmission(
			HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		if (errors.hasErrors() || isFormChangeRequest(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Data binding errors: " + errors.getErrorCount());
			}
			return showForm(request, response, errors);
		}
		else {
			logger.debug("No errors -> processing submit");
			return onSubmit(request, response, command, errors);
		}
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
	 * Submit callback with all parameters. Called in case of submit without errors
	 * reported by the registered validator respectively on every submit if no validator.
	 * <p>Default implementation delegates to onSubmit(Object, BindException).
	 * For simply performing a submit action and rendering the specified success view,
	 * consider implementing doSubmitAction rather than an onSubmit version.
	 * <p>Subclasses can override this to provide custom submission handling like storing
	 * the object to the database. Implementations can also perform custom validation and
	 * call showForm to return to the form. Do <i>not</i> implement multiple onSubmit
	 * methods: In that case, just this method will be called by the controller.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param request current servlet request
	 * @param response current servlet response
	 * @param command form object with request parameters bound onto it
	 * @param errors Errors instance without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
	 * @see #onSubmit(Object, BindException)
	 * @see #doSubmitAction
	 * @see #showForm
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 */
	protected ModelAndView onSubmit(
			HttpServletRequest request,	HttpServletResponse response, Object command,	BindException errors)
			throws Exception {
		return onSubmit(command, errors);
	}

	/**
	 * Simpler onSubmit version. Called by the default implementation of the onSubmit
	 * version with all parameters.
	 * <p>Default implementation calls onSubmit(command), using the returned ModelAndView
	 * if actually implemented in a subclass. Else, the default behavior is applied:
	 * rendering the success view with the command and Errors instance as model.
	 * <p>Subclasses can override this to provide custom submission handling that
	 * does not need request and response.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param command form object with request parameters bound onto it
	 * @param errors Errors instance without errors
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
	 * @see #onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)
	 * @see #onSubmit(Object)
	 * @see #setSuccessView
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 */
	protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
		ModelAndView mv = onSubmit(command);
		if (mv != null) {
			// simplest onSubmit version implemented in custom subclass
			return mv;
		}
		else {
			// default behavior: render success view
			if (getSuccessView() == null) {
				throw new ServletException("successView isn't set");
			}
			return new ModelAndView(getSuccessView(), errors.getModel());
		}
	}

	/**
	 * Simplest onSubmit version. Called by the default implementation of the
	 * onSubmit version with command and BindException parameters.
	 * <p>This implementation calls <code>doSubmitAction</code> and returns null
	 * as ModelAndView, making the calling onSubmit method perform its default
	 * rendering of the success view.
	 * <p>Subclasses can override this to provide custom submission handling
	 * that just depends on the command object. It's preferable to use either
	 * <code>onSubmit(command, errors)</code> or <code>doSubmitAction(command)</code>,
	 * though: Use the former when you want to build your own ModelAndView; use the
	 * latter when you want to perform an action and forward to the successView.
	 * @param command form object with request parameters bound onto it
	 * @return the prepared model and view, or null for default (i.e. successView)
	 * @throws Exception in case of errors
	 * @see #onSubmit(Object, BindException)
	 * @see #doSubmitAction
	 * @see #setSuccessView
	 */
	protected ModelAndView onSubmit(Object command) throws Exception {
		doSubmitAction(command);
		return null;
	}

	/**
	 * Template method for submit actions. Called by the default implementation
	 * of the simplest onSubmit version.
	 * <p><b>This is the preferred submit callback to implement if you want to
	 * perform an action (like storing changes to the database) and then render
	 * the success view with the command and Errors instance as model.</b>
	 * You don't need to care about the success ModelAndView here.
	 * @param command form object with request parameters bound onto it
	 * @throws Exception in case of errors
	 * @see #onSubmit(Object)
	 * @see #setSuccessView
	 */
	protected void doSubmitAction(Object command) throws Exception {
	}

}
