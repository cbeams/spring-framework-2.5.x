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
 * form and success views, and an onSubmit chain for convenient overriding.</p>
 *
 * <p>Automatically resubmits to the form view in case of validation errors,
 * and renders the success view in case of a valid submission.</p>
 *
 * <p>The submit behavior can be customized by overriding one of the onSubmit
 * methods. Submit actions can be used as custom validation if necessary
 * (e.g. login), by calling showForm in case of validation errors.</p>
 *
 * <p>Besides some extra functionality like described above and in the
 * list of exposed configuration properties and the workflow description,
 * this class does the same as the {@link AbstractFormController AbstractFormController}
 * (hmmm, it also extends it).</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="BaseCommandController.html#workflow">and that defined by superclass</a>):</b><br>
 * The workflow of this Controller does not differ too much from the one described
 * in the {@link AbstractFormController AbstractFormController}, except for
 * the fact that overriding of the processFormSubmission method and the
 * showForm method is not necessary, since the view for the respective occasions
 * can be configured externally.
 * <ol>
 *  <li>XXX After validation of the command object and the perscribed
 *      call to {@link #onBindAndValidate onBindAndValidate} (for more
 *      information on that matter, see the AbstractFormController),
 *      the following:</li>
 *  <li>call to {@link #processFormSubmission processFormSubmission} which inspects the
 *      errors object to see if any errors are available (they could be inserted in
 *      the <code>bindAndValidate</code> method</li>
 *  <li>If errors occured, the controller will return the formView, giving
 *      the user the form again (with possible error message render accordingly)</li>
 *  <li>If no errors occurred, a call to
 *      {@link #onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException) onSubmit()}
 *      using all parameters is done which (in case of the default implementation)
 *      calls {@link #onSubmit(Object) onSubmit()} with just the command object.
 *      This allows for convenient overriding of custom hooks</li>
 *  <li>After that has finished, the successView is returned (which again,
 *      is configurable through the exposed configuration properties)</li>
 *  </ol>
 * </p>
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
 *      <td>Indicates what view to use when the user first asks for the form
 *          or when validation errors have occurred while submitting the form</td>
 *  </tr>
 *  <tr>
 *      <td>successView</td>
 *      <td><i>null</i></td>
 *      <td>Indicates what view to use when successful formsubmissions have
 *          occurred. This could for instance be a view congrulating the user
 *          with his successful submission</td>
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
	 * formBackingObject, as this determines the class anyway.
	 * @see #setCommandClass
	 * @see #setCommandName
	 * @see #setSessionForm
	 * @see #setFormView
	 * @see #setSuccessView
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
	protected final String getFormView() {
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
	protected final String getSuccessView() {
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
	protected final ModelAndView showForm(HttpServletRequest request, HttpServletResponse response,
	                                      BindException errors) throws Exception {
		return showForm(request, errors, getFormView());
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
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response,
	                                             Object command, BindException errors) throws Exception {
		if (errors.hasErrors()) {
			logger.debug("Data binding errors: " + errors.getErrorCount());
			return showForm(request, response, errors);
		}
		else {
			logger.debug("No errors -> processing submit");
			return onSubmit(request, response, command, errors);
		}
	}

	/**
	 * Submit callback with all parameters. Called in case of submit without errors
	 * reported by the registered validator respectively on every submit if no validator.
	 * <p>Default implementation calls onSubmit(command), using the returned ModelAndView
	 * if actually implemented in a subclass. Else, the default behavior is applied:
	 * rendering the success view with the command and Errors instance as model.
	 * <p>Subclasses can override this to provide custom submission handling like storing
	 * the object to the database. Implementations can also perform custom validation and
	 * call showForm to return to the form. Do <i>not</i> implement both onSubmit template
	 * methods: In that case, just this method will be called by the controller.
	 * <p>Call errors.getModel() to populate the ModelAndView model with the command and
	 * the Errors instance, under the command name, as expected by the "spring:bind" tag.
	 * @param request current servlet request
	 * @param response current servlet response
	 * @param command form object with request parameters bound onto it
	 * @param errors Errors instance without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
	 * @see #onSubmit(Object)
	 * @see #showForm
	 * @see org.springframework.validation.Errors
	 */
	protected ModelAndView onSubmit(HttpServletRequest request,	HttpServletResponse response,
																	Object command,	BindException errors) throws Exception {
		ModelAndView mv = onSubmit(command);
		if (mv != null) {
			// simple onSubmit version implemented in custom subclass
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
	 * Simple onSubmit version. Called by the default implementation of the onSubmit
	 * version with all parameters.
	 * <p>This implementation returns null, making the calling onSubmit method perform
	 * its default rendering of the success view.
	 * <p>Subclasses can override this to provide custom submission handling that
	 * just needs the command object.
	 * @param command form object with request parameters bound onto it
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
	 * @see #onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)
	 */
	protected ModelAndView onSubmit(Object command) throws Exception {
		return null;
	}

}
