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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.util.enums.support.ShortCodedLabeledEnum;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecutionContext;

/**
 * A base superclass for actions that contain form view setup logic. Extends
 * from <code>BindAndValidate</code> action to share common behaivior.
 * <p>
 * Form setup actions typically execute before a view is displayed to load the
 * backing form object and errors instance, and retrieve any supporting
 * reference data (typically to populate drop downs). Basically, this action
 * exists to setup any dynamic data needed for rendering by a view template.
 * <p>
 * Fully instantiable as is, or by a custom subclass. This action differs from a
 * standard BindAndValidateAction in that it doesn't do validation (not needed
 * during form setup), is capable of form prepopulation from request parameters,
 * and has a specific <code>setupReferenceData</code> hook.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class SetupFormAction extends BindAndValidateAction {

	private static final String NOT_MAPPED_PLACEHOLDER_ATTRIBUTE = "notMapped";

	private static final String NOT_MAPPED_COLLECTION_PLACEHOLDER_ATTRIBUTE = "notMappedItems";

	private static final String NOT_MAPPED_PLACEHOLDER_VALUE = "!NOT MAPPED!";

	private boolean prepopulateFromRequest;

	/**
	 * Sets a flag that determines whether this setup form action should
	 * prepopulate its form object from request parameter values when executed.
	 * @param prepopulateFromRequest true if the form object should be populated
	 *        with request parameters, false otherwise
	 */
	public void setPrepopulateFromRequest(boolean prepopulateFromRequest) {
		this.prepopulateFromRequest = prepopulateFromRequest;
	}

	// validation doesn't happen on form setup by default
	protected boolean suppressValidation(HttpServletRequest request) {
		return true;
	}

	protected Event doExecuteAction(FlowExecutionContext context) throws Exception {
		Object formObject = loadRequiredFormObject(context);
		DataBinder binder = createBinder(context, formObject);
		if (prepopulateFromRequest) {
			if (logger.isDebugEnabled()) {
				logger.debug("Prepopulating backing form object from request parameters");
			}
			// binder.bind(context);
		}
		// exposeBindExceptionErrors(context, binder.getErrors());
		exposeViewPlaceholders(context);
		try {
			setupReferenceData(context);
		}
		catch (ServletRequestBindingException e) {
			throw new ReferenceDataSetupException(e);
		}
		return binder.getErrors().hasErrors() ? error() : success();
	}

	/**
	 * Exposes convenience 'debug' placeholders for views that have incomplete
	 * field mappings.
	 * @param request The request
	 * @param model The model
	 */
	protected void exposeViewPlaceholders(FlowExecutionContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Exposing view markers/placeholders to notify business-tier developers of "
					+ "form fields that are missing, not mapped to backing objects, or need clarification");
			// placeholders indicating attributes on forms that have not been
			// mapped to dynamic fields on backing objects - for use during
			// development only
			context.requestScope().setAttribute(NOT_MAPPED_PLACEHOLDER_ATTRIBUTE, NOT_MAPPED_PLACEHOLDER_VALUE);
			context.requestScope().setAttribute(NOT_MAPPED_COLLECTION_PLACEHOLDER_ATTRIBUTE, createNotMappedEnumSet());
		}
	}

	/**
	 * Helper method to setup a set of placeholder data.
	 */
	private Set createNotMappedEnumSet() {
		Set notMapped = new HashSet();
		notMapped.add(new NotMappedEnum(1));
		notMapped.add(new NotMappedEnum(2));
		notMapped.add(new NotMappedEnum(3));
		return notMapped;
	}

	/**
	 * Helper class used as placeholder data in the
	 * <code>exposeViewPlaceholders()</code> method.
	 */
	private static final class NotMappedEnum extends ShortCodedLabeledEnum {
		public NotMappedEnum(int code) {
			super(code, NOT_MAPPED_PLACEHOLDER_VALUE);
		}
	}

	/**
	 * Template method to be implemented by subclasses to setup any reference
	 * data needed to support this form. For example, a subclass may load
	 * reference data from the DB or a 2nd level cache and place it in the
	 * request or flow model.
	 * @param request current HTTP request
	 * @param model the flow model
	 */
	protected void setupReferenceData(FlowExecutionContext context) throws ReferenceDataSetupException,
			ServletRequestBindingException {
	}

	/**
	 * Exception thrown when reference data setup fails.
	 * 
	 * @author Keith Donald
	 */
	protected static class ReferenceDataSetupException extends RuntimeException {

		/**
		 * Create a new reference data setup exception.
		 * @param message a descriptive message
		 * @param cause the underlying cause of this exception
		 */
		public ReferenceDataSetupException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Create a new reference data setup exception.
		 * @param cause the underlying cause of this exception
		 */
		public ReferenceDataSetupException(Exception cause) {
			super(cause);
		}

		public String getMessage() {
			if (StringUtils.hasText(super.getMessage())) {
				return super.getMessage();
			}
			else {
				if (getCause() instanceof ServletRequestBindingException) {
					return "Unable to set form reference data due to request parameter binding error - programmer error "
							+ "likely in submitting view code or request access code; see cause for more details.";
				}
				else {
					return "Unable to set supporting reference data during form setup";
				}
			}
		}
	}
}