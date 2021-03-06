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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Form controller for typical wizard-style workflows.
 *
 * <p>In contrast to classic forms, wizards have more than one form view page.
 * Therefore, there are various actions instead of one single submit action:
 * <ul>
 * <li>finish: trying to leave the wizard successfully, i.e. performing its
 * final action, and thus needing a valid state;
 * <li>cancel: leaving the wizard without performing its final action, and
 * thus without regard to the validity of its current state;
 * <li>page change: showing another wizard page, e.g. the next or previous
 * one, with regard to "dirty back" and "dirty forward".
 * </ul>
 *
 * <p>Finish and cancel actions can be triggered by request parameters, named
 * PARAM_FINISH ("_finish") and PARAM_CANCEL ("_cancel"), ignoring parameter
 * values to allow for HTML buttons. The target page for page changes can be
 * specified by PARAM_TARGET, appending the page number to the parameter name
 * (e.g. "_target1"). The action parameters are recognized when triggered by
 * image buttons too (via "_finish.x", "_abort.x", or "_target1.x").
 *
 * <p>The current page number will be stored in the session. It can also be
 * specified as request parameter PARAM_PAGE, to properly handle usage of
 * the back button in a browser: In this case, a submission always contains
 * the correct page number, even if the user submitted from an old view.
 *
 * <p>The page can only be changed if it validates correctly, except if a
 * "dirty back" or "dirty forward" is allowed. At finish, all pages get
 * validated again to guarantee a consistent state.
 *
 * <p>Note that a validator's default validate method is not executed when using
 * this class! Rather, the <code>validatePage</code> implementation should call
 * special <code>validateXXX</code> methods that the validator needs to provide,
 * validating certain pieces of the object. These can be combined to validate
 * the elements of individual pages.
 *
 * <p>Note: Page numbering starts with 0, to be able to pass an array
 * consisting of the corresponding view names to the "pages" bean property.
 *
 * @author Juergen Hoeller
 * @since 25.04.2003
 * @see #setPages
 * @see #validatePage
 * @see #processFinish
 * @see #processCancel
 */
public abstract class AbstractWizardFormController extends AbstractFormController {

	/**
	 * Parameter triggering the finish action.
	 * Can be called from any wizard page!
	 */
	public static final String PARAM_FINISH = "_finish";

	/**
	 * Parameter triggering the cancel action.
	 * Can be called from any wizard page!
	 */
	public static final String PARAM_CANCEL = "_cancel";

	/**
	 * Parameter specifying the target page,
	 * appending the page number to the name.
	 */
	public static final String PARAM_TARGET = "_target";

	/**
	 * Parameter specifying the current page as value. Not necessary on
	 * form pages, but allows to properly handle usage of the back button.
	 * @see #setPageAttribute
	 */
	public static final String PARAM_PAGE = "_page";


	private String[] pages;

	private String pageAttribute;

	private boolean allowDirtyBack = true;

	private boolean allowDirtyForward = false;


	/**
	 * Create a new AbstractWizardFormController.
	 * <p>"sessionForm" is automatically turned on, "validateOnBinding"
	 * turned off, and "cacheSeconds" set to 0 by the base class
	 * (-> no caching for all form controllers).
	 */
	public AbstractWizardFormController() {
		// AbstractFormController sets default cache seconds to 0.
		super();

		// Always needs session to keep data from all pages.
		setSessionForm(true);

		// Never validate everything on binding ->
		// wizards validate individual pages.
		setValidateOnBinding(false);
	}

	/**
	 * Set the wizard pages, i.e. the view names for the pages.
	 * The array index is interpreted as page number.
	 * @param pages view names for the pages
	 */
	public final void setPages(String[] pages) {
		if (pages == null || pages.length == 0)  {
			throw new IllegalArgumentException("No wizard pages defined");
		}
		this.pages = pages;
	}

	/**
	 * Return the wizard pages, i.e. the view names for the pages.
	 * The array index corresponds to the page number.
	 * <p>Note that a concrete wizard form controller might override
	 * <code>getViewName(HttpServletRequest, Object, int)</code> to
	 * determine the view name for each page dynamically.
	 * @see #getViewName(javax.servlet.http.HttpServletRequest, Object, int)
	 */
	public final String[] getPages() {
		return pages;
	}

	/**
	 * Return the number of wizard pages.
	 * Useful to check whether the last page has been reached.
	 * <p>Note that a concrete wizard form controller might override
	 * <code>getPageCount(HttpServletRequest, Object)</code> to determine
	 * the page count dynamically. The default implementation of that extended
	 * <code>getPageCount</code> variant returns the static page count as
	 * determined by this <code>getPageCount()</code> method.
	 * @see #getPageCount(javax.servlet.http.HttpServletRequest, Object)
	 */
	protected final int getPageCount() {
		return this.pages.length;
	}

	/**
	 * Set the name of the page attribute in the model, containing
	 * an Integer with the current page number.
	 * <p>This will be necessary for single views rendering multiple view pages.
	 * It also allows for specifying the optional "_page" parameter.
	 * @param pageAttribute name of the page attribute
	 * @see #PARAM_PAGE
	 */
	public final void setPageAttribute(String pageAttribute) {
		this.pageAttribute = pageAttribute;
	}

	/**
	 * Return the name of the page attribute in the model.
	 */
	public final String getPageAttribute() {
		return pageAttribute;
	}

	/**
	 * Set if "dirty back" is allowed, i.e. if moving to a former wizard
	 * page is allowed in case of validation errors for the current page.
	 * @param allowDirtyBack if "dirty back" is allowed
	 */
	public final void setAllowDirtyBack(boolean allowDirtyBack) {
		this.allowDirtyBack = allowDirtyBack;
	}

	/**
	 * Return whether "dirty back" is allowed.
	 */
	public final boolean isAllowDirtyBack() {
		return allowDirtyBack;
	}

	/**
	 * Set if "dirty forward" is allowed, i.e. if moving to a later wizard
	 * page is allowed in case of validation errors for the current page.
	 * @param allowDirtyForward if "dirty forward" is allowed
	 */
	public final void setAllowDirtyForward(boolean allowDirtyForward) {
		this.allowDirtyForward = allowDirtyForward;
	}

	/**
	 * Return whether "dirty forward" is allowed.
	 */
	public final boolean isAllowDirtyForward() {
		return allowDirtyForward;
	}


	/**
	 * Calls page-specific onBindAndValidate method.
	 */
	protected final void onBindAndValidate(HttpServletRequest request, BindingResult bindingResult) throws Exception {
		onBindAndValidate(request, bindingResult, getCurrentPage(request));
	}

	/**
	 * Callback for custom post-processing in terms of binding and validation.
	 * Called on each submit, after standard binding but before page-specific
	 * validation of this wizard form controller.
	 * <p>Note: AbstractWizardFormController does not perform standand
	 * validation on binding but rather applies page-specific validation
	 * on processing the form submission.
	 * @param request current HTTP request
	 * @param bindingResult Errors instance for additional custom validation
	 * @param page current wizard page
	 * @throws Exception in case of invalid state or arguments
	 * @see #bindAndValidate
	 * @see #processFormSubmission
	 * @see org.springframework.validation.Errors
	 */
	protected void onBindAndValidate(HttpServletRequest request, BindingResult bindingResult, int page)
	    throws Exception {
	}

	/**
	 * Consider an explicit finish or cancel request as a form submission too.
	 * @see #isFinishRequest(javax.servlet.http.HttpServletRequest)
	 * @see #isCancelRequest(javax.servlet.http.HttpServletRequest)
	 */
	protected boolean isFormSubmission(HttpServletRequest request) {
		return super.isFormSubmission(request) || isFinishRequest(request) || isCancelRequest(request);
	}

	/**
	 * Calls page-specific referenceData method.
	 */
	protected final Map referenceData(HttpServletRequest request, BindingResult bindingResult) throws Exception {
		return referenceData(request, bindingResult, getCurrentPage(request));
	}

	/**
	 * Create a reference data map for the given request, consisting of
	 * bean name/bean instance pairs as expected by ModelAndView.
	 * <p>Default implementation delegates to referenceData(HttpServletRequest, int).
	 * Subclasses can override this to set reference data used in the view.
	 * @param request current HTTP request
	 * @param bindingResult validation errors holder
	 * @param page current wizard page
	 * @return a Map with reference data entries, or <code>null</code> if none
	 * @throws Exception in case of invalid state or arguments
	 * @see org.springframework.web.servlet.ModelAndView
	 */
	protected Map referenceData(HttpServletRequest request, BindingResult bindingResult, int page) throws Exception {
		return null;
	}


	/**
	 * Show first page as form view.
	 */
	protected final ModelAndView showForm(HttpServletRequest request, BindingResult bindingResult) throws Exception {
		return showPage(request, bindingResult, getInitialPage(request, bindingResult));
	}

	/**
	 * Prepare the form model and view, including reference and error data,
	 * for the given page. Can be used in <code>processFinish</code> implementations,
	 * to show the corresponding page in case of validation errors.
	 * @param request current HTTP request
	 * @param errors validation errors holder
	 * @param page number of page to show
	 * @return the prepared form view
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ModelAndView showPage(HttpServletRequest request, BindingResult errors, int page)
	    throws Exception {

		if (page >= 0 && page < getPageCount(request, errors.getTarget())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Showing wizard page " + page + " for form bean '" + getFormObjectName() + "'");
			}

			// Set page session attribute, expose overriding request attribute.
			Integer pageInteger = new Integer(page);
			String pageAttrName = getPageSessionAttributeName(request);
			if (isSessionForm()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting page session attribute [" + pageAttrName + "] to: " + pageInteger);
				}
				request.getSession().setAttribute(pageAttrName, pageInteger);
			}
			request.setAttribute(pageAttrName, pageInteger);

			// Set page request attribute for evaluation by views.
			Map controlModel = new HashMap();
			if (this.pageAttribute != null) {
				controlModel.put(this.pageAttribute, new Integer(page));
			}
			String viewName = getViewName(request, errors.getTarget(), page);
			return showForm(request, errors, viewName, controlModel);
		}

		else {
			throw new ServletException("Invalid wizard page number: " + page);
		}
	}

	/**
	 * Return the page count for this wizard form controller.
	 * Default implementation delegates to <code>getPageCount()</code>.
	 * <p>Can be overridden to dynamically adapt the page count.
	 * @param request current HTTP request
	 * @param command the command object as returned by formBackingObject
	 * @return the current page count
	 * @see #getPageCount
	 */
	protected int getPageCount(HttpServletRequest request, Object command) {
		return getPageCount();
	}

	/**
	 * Return the name of the view for the specified page of this wizard form controller.
	 * Default implementation takes the view name from the <code>getPages()</code> array.
	 * <p>Can be overridden to dynamically switch the page view or to return view names
	 * for dynamically defined pages.
	 * @param request current HTTP request
	 * @param command the command object as returned by formBackingObject
	 * @return the current page count
	 * @see #getPageCount
	 */
	protected String getViewName(HttpServletRequest request, Object command, int page) {
		return getPages()[page];
	}

	/**
	 * Return the initial page of the wizard, i.e. the page shown at wizard startup.
	 * Default implementation delegates to <code>getInitialPage(HttpServletRequest)</code>.
	 * @param request current HTTP request
	 * @param bindingResult validation errors holder
	 * @return the initial page number
	 * @see #formBackingObject
	 */
	protected int getInitialPage(HttpServletRequest request, BindingResult bindingResult) {
		return 0;
	}

	/**
	 * Return the name of the HttpSession attribute that holds the page object
	 * for this wizard form controller.
	 * <p>Default implementation delegates to the <code>getPageSessionAttributeName</code>
	 * version without arguments.
	 * @param request current HTTP request
	 * @return the name of the form session attribute
	 * @see #getPageSessionAttributeName
	 * @see #getFormSessionAttributeName(javax.servlet.http.HttpServletRequest)
	 * @see javax.servlet.http.HttpSession#getAttribute
	 */
	protected String getPageSessionAttributeName(HttpServletRequest request) {
		return getClass().getName() + ".PAGE." + getFormObjectName();
	}

	/**
	 * Handle an invalid submit request, e.g. when in session form mode but no form object
	 * was found in the session (like in case of an invalid resubmit by the browser).
	 * <p>Default implementation for wizard form controllers simply shows the initial page
	 * of a new wizard form. If you want to show some "invalid submit" message, you need
	 * to override this method.
	 * @param request current HTTP request
	 * @return a prepared view, or <code>null</code> if handled directly
	 * @throws Exception in case of errors
	 * @see #showNewForm
	 * @see #setBindOnNewForm
	 */
	protected ModelAndView handleInvalidSubmit(HttpServletRequest request) throws Exception {
		return showNewForm(request);
	}


	/**
	 * Apply wizard workflow: finish, cancel, page change.
	 */
	protected final ModelAndView processFormSubmission(HttpServletRequest request, BindingResult bindingResult)
			throws Exception {

		int currentPage = getCurrentPage(request);
		// Remove page session attribute, provide copy as request attribute.
		String pageAttrName = getPageSessionAttributeName(request);
		if (isSessionForm()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing page session attribute [" + pageAttrName + "]");
			}
			request.getSession().removeAttribute(pageAttrName);
		}
		request.setAttribute(pageAttrName, new Integer(currentPage));

		// cancel?
		if (isCancelRequest(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cancelling wizard for form bean '" + getFormObjectName() + "'");
			}
			return processCancel(request, bindingResult);
		}

		// finish?
		if (isFinishRequest(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Finishing wizard for form bean '" + getFormObjectName() + "'");
			}
			return validatePagesAndFinish(request, bindingResult, currentPage);
		}

		// Normal submit: validate current page and show specified target page.
		if (!suppressValidation(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Validating wizard page " + currentPage + " for form bean '" + getFormObjectName() + "'");
			}
			validatePage(bindingResult.getTarget(), bindingResult, currentPage, false);
		}

		// Give subclasses a change to perform custom post-procession
		// of the current page and its command object.
		postProcessPage(request, bindingResult, currentPage);

		int targetPage = getTargetPage(request, bindingResult, currentPage);
		if (logger.isDebugEnabled()) {
			logger.debug("Target page " + targetPage + " requested");
		}
		if (targetPage != currentPage) {
			if (!bindingResult.hasErrors() || (this.allowDirtyBack && targetPage < currentPage) ||
					(this.allowDirtyForward && targetPage > currentPage)) {
				// Allowed to go to target page.
				return showPage(request, bindingResult, targetPage);
			}
		}

		// Show current page again.
		return showPage(request, bindingResult, currentPage);
	}

	/**
	 * Return the current page number. Used by processFormSubmission.
	 * <p>The default implementation checks the page session attribute.
	 * Subclasses can override this for customized page determination.
	 * @see #processFormSubmission
	 * @see #getPageSessionAttributeName
	 */
	protected int getCurrentPage(HttpServletRequest request) {
		// Check for overriding attribute in request.
		String pageAttrName = getPageSessionAttributeName(request);
		Integer pageAttr = (Integer) request.getAttribute(pageAttrName);
		if (pageAttr != null) {
			return pageAttr.intValue();
		}
		// Check for explicit request parameter.
		String pageParam = request.getParameter(PARAM_PAGE);
		if (pageParam != null) {
			return Integer.parseInt(pageParam);
		}
		// Check for original attribute in session.
		if (isSessionForm()) {
			pageAttr = (Integer) request.getSession().getAttribute(pageAttrName);
			if (pageAttr != null) {
				return pageAttr.intValue();
			}
		}
		throw new IllegalStateException(
				"Page attribute [" + pageAttrName + "] neither found in session nor in request");
	}

	/**
	 * Determine whether the incoming request is a request to finish the
	 * processing of the current form.
	 * <p>By default, this method returns <code>true</code> if a parameter
	 * matching the "_finish" key is present in the request, otherwise it
	 * returns <code>false</code>. Subclasses may override this method
	 * to provide custom logic to detect a finish request.
	 * <p>The parameter is recognized both when sent as a plain parameter
	 * ("_finish") or when triggered by an image button ("_finish.x").
	 * @param request current HTTP request
	 * @see #PARAM_FINISH
	 */
	protected boolean isFinishRequest(HttpServletRequest request) {
		return WebUtils.hasSubmitParameter(request, PARAM_FINISH);
	}

	/**
	 * Determine whether the incoming request is a request to cancel the
	 * processing of the current form.
	 * <p>By default, this method returns <code>true</code> if a parameter
	 * matching the "_cancel" key is present in the request, otherwise it
	 * returns <code>false</code>. Subclasses may override this method
	 * to provide custom logic to detect a cancel request.
	 * <p>The parameter is recognized both when sent as a plain parameter
	 * ("_cancel") or when triggered by an image button ("_cancel.x").
	 * @param request current HTTP request
	 * @see #PARAM_CANCEL
	 */
	protected boolean isCancelRequest(HttpServletRequest request) {
		return WebUtils.hasSubmitParameter(request, PARAM_CANCEL);
	}

	/**
	 * Return the target page specified in the request.
	 * <p>Default implementation delegates to
	 * <code>getTargetPage(HttpServletRequest, int)</code>.
	 * Subclasses can override this for customized target page determination.
	 * @param request current HTTP request
	 * @param bindingResult validation errors holder
	 * @param currentPage the current page, to be returned as fallback
	 * if no target page specified
	 * @return the page specified in the request, or current page if not found
	 */
	protected int getTargetPage(HttpServletRequest request, BindingResult bindingResult, int currentPage) {
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			if (paramName.startsWith(PARAM_TARGET)) {
				for (int i = 0; i < WebUtils.SUBMIT_IMAGE_SUFFIXES.length; i++) {
					String suffix = WebUtils.SUBMIT_IMAGE_SUFFIXES[i];
					if (paramName.endsWith(suffix)) {
						paramName = paramName.substring(0, paramName.length() - suffix.length());
					}
				}
				return Integer.parseInt(paramName.substring(PARAM_TARGET.length()));
			}
		}
		return currentPage;
	}

	/**
	 * Validate all pages and process finish.
	 * If there are page validation errors, show the corresponding view page.
	 */
	private ModelAndView validatePagesAndFinish(
			HttpServletRequest request, BindingResult bindingResult, int currentPage) throws Exception {

		// In case of binding errors -> show current page.
		if (bindingResult.hasErrors()) {
			return showPage(request, bindingResult, currentPage);
		}

		if (!suppressValidation(request)) {
			// In case of remaining errors on a page -> show the page.
			for (int page = 0; page < getPageCount(request, bindingResult); page++) {
				validatePage(bindingResult, bindingResult, page, true);
				if (bindingResult.hasErrors()) {
					return showPage(request, bindingResult, page);
				}
			}
		}

		// No remaining errors -> proceed with finish.
		return processFinish(request, bindingResult);
	}


	/**
	 * Template method for custom validation logic for individual pages.
	 * Default implementation calls <code>validatePage(command, errors, page)</code>.
	 * <p>Implementations will typically call fine-granular <code>validateXXX</code>
	 * methods of this instance's Validator, combining them to validation of the
	 * corresponding pages. The Validator's default <code>validate</code> method
	 * will not be called by a wizard form controller!
	 * @param command form object with the current wizard state
	 * @param errors validation errors holder
	 * @param page number of page to validate
	 * @param finish whether this method is called during final revalidation on finish
	 * (else, it is called for validating the current page)
	 * @see #validatePage(Object, org.springframework.validation.Errors, int)
	 * @see org.springframework.validation.Validator#validate
	 */
	protected void validatePage(Object command, Errors errors, int page, boolean finish) {
		validatePage(command, errors, page);
	}

	/**
	 * Template method for custom validation logic for individual pages.
	 * Default implementation is empty.
	 * <p>Implementations will typically call fine-granular validateXXX methods of this
	 * instance's validator, combining them to validation of the corresponding pages.
	 * The validator's default <code>validate</code> method will not be called by a
	 * wizard form controller!
	 * @param command form object with the current wizard state
	 * @param errors validation errors holder
	 * @param page number of page to validate
	 * @see org.springframework.validation.Validator#validate
	 */
	protected void validatePage(Object command, Errors errors, int page) {
	}

	/**
	 * Post-process the given page after binding and validation, potentially
	 * updating its command object. The passed-in request might contain special
	 * parameters sent by the page.
	 * <p>Only invoked when displaying another page or the same page again,
	 * not when finishing or cancelling.
	 * @param request current HTTP request
	 * @param bindingResult validation errors holder
	 * @param page number of page to post-process
	 * @throws Exception in case of invalid state or arguments
	 */
	protected void postProcessPage(HttpServletRequest request, BindingResult bindingResult, int page)
			throws Exception {
	}

	/**
	 * Template method for processing the final action of this wizard.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * <p>You can call the <code>showPage</code> method to return back to the wizard,
	 * in case of last-minute validation errors having been found that you would
	 * like to present to the user within the original wizard form.
	 * @param request current HTTP request
	 * @param bindingResult validation errors holder
	 * @return the finish view
	 * @throws Exception in case of invalid state or arguments
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 * @see #showPage(javax.servlet.http.HttpServletRequest, org.springframework.validation.BindingResult, int)
	 */
	protected abstract ModelAndView processFinish(HttpServletRequest request, BindingResult bindingResult)
			throws Exception;

	/**
	 * Template method for processing the cancel action of this wizard.
	 * <p>Default implementation throws a ServletException, saying that a cancel
	 * operation is not supported by this controller. Thus, you do not need to
	 * implement this template method if you do not support a cancel operation.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param request current HTTP request
	 * @param bindingResult Errors instance containing errors
	 * @return the cancellation view
	 * @throws Exception in case of invalid state or arguments
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 */
	protected ModelAndView processCancel(HttpServletRequest request, BindingResult bindingResult) throws Exception {
		throw new ServletException(
				"Wizard form controller class [" + getClass().getName() + "] does not support a cancel operation");
	}

}
