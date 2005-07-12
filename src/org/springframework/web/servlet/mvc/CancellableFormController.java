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

package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

/**
 * <p>Extension of <code>SimpleFormController</code> that supports &quot;cancellation&quot; of form processing. By default,
 * this controller looks for a given parameter in the request, identified by the <code>cancelParameterKey<code>. If this
 * parameter is present then the controller will return the configured <code>cancelView</code>, otherwise processing is
 * passed back to the superclass.</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="SimpleFormController.html#workflow">in addition to the superclass</a>):</b><br>
 *  <ol>
 *   <li>Call to {@link #processFormSubmission processFormSubmission} which calls {@link #isCancelRequest} to see if
 *       the incoming request is to cancel the current form entry. By default, {@link #isCancelRequest} returns
 *       <code>true</code> if the configured <code>cancelParameterKey</code> exists in the request. This behavior can
 *       be overridden in sub-classes.</li>
 *   <li>If {@link #isCancelRequest} returns <code>false</code> then the controller will delegate all processing back to
 *       {@link SimpleFormController SimpleFormController}, otherwise it will call {@link #onCancel}. By default,
 *       {@link #onCancel} will simply return the configured <code>cancelView</code> - this behavior can be overridden
 *       in sub-classes.</li>
 *  </ol>
 * </p>
 *
 * @author Rob Harrop
 * @see #setCancelParameterKey(String)
 * @see #setCancelView(String)
 * @see #isCancelRequest(javax.servlet.http.HttpServletRequest)
 * @see #onCancel(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object)
 */
public class CancellableFormController extends SimpleFormController {

	private static final String DEFAULT_CANCEL_PARAMETER_KEY = "_cancel";

	private String cancelParameterKey = DEFAULT_CANCEL_PARAMETER_KEY;

	private String cancelView;

	/**
	 * Gets the key of the request parameter used to identify a cancel request.
	 */
	public final String getCancelParameterKey() {
		return cancelParameterKey;
	}

	/**
	 * Sets the key of the request parameter used to identify a cancel request.
	 */
	public final void setCancelParameterKey(String cancelParameterKey) {
		this.cancelParameterKey = cancelParameterKey;
	}

	/**
	 * Gets the name of the cancel view.
	 */
	public final String getCancelView() {
		return cancelView;
	}

	/**
	 * Sets the name of the cancel view.
	 */ 
	public final void setCancelView(String cancelView) {
		this.cancelView = cancelView;
	}

	/**
	 * This implementation first checks to see if the incoming is a cancel request with a call to {@link #isCancelRequest}.
	 * If so, control is passed to {@link #onCancel} otherwise control is passed up to {@link SimpleFormController#processFormSubmission}.
	 * @see #isCancelRequest(javax.servlet.http.HttpServletRequest)
	 * @see #onCancel(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object)
	 * @see SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(
			HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
			throws Exception {

		if(isCancelRequest(request)) {
			return onCancel(request, response, command);
		} else {
			return super.processFormSubmission(request, response, command, errors);
		}
	}

	/**
	 * Determines whether or not the incoming request is a request to cancel the processing of the current form. By default,
	 * this method returns <code>true</code> if a parameter matching the configured <code>cancelParameterKey</code> is
	 * present in the request, otherwise it returns <code>false</code>. Sub-classes may override this method to provide
	 * custom logic to detect a cancel request.
	 * @see #setCancelParameterKey(String)
	 */
	protected boolean isCancelRequest(HttpServletRequest request) {
		return (request.getParameterMap().containsKey(getCancelParameterKey()));
	}

	/**
	 * Called if {@link #isCancelRequest} returns <code>true</code>. By default, returns the configured
	 * <code>cancelView</code>. Sub-classes may override this method to construct a custom {@link ModelAndView ModelAndView}
	 * that may contain model parameters used in the cancel. If you want to simply move the user to a new view and you
	 * don't want to add additional model parameters, use {@link #setCancelView(String)} rather than overriding this method.
	 *
	 * @see #isCancelRequest(javax.servlet.http.HttpServletRequest)
	 * @see #setCancelView(String)
	 */
	protected ModelAndView onCancel(HttpServletRequest request, HttpServletResponse response, Object command) {
		return new ModelAndView(getCancelView());
	}
}
