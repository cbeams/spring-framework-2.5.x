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
import javax.servlet.http.HttpServletResponse;

import org.springframework.enums.ShortCodedEnum;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.flow.ActionBeanEvent;
import org.springframework.web.flow.MutableAttributesAccessor;

/**
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class PopulateFormAction extends BindAndValidateAction {

	private static final String NOT_MAPPED_MARKER_VALUE = "!NOT MAPPED!";

	private static final String NOT_MAPPED_COLLECTION_MARKER_PARAMETER_NAME = "_notMappedItems";

	private static final String NOT_MAPPED_MARKER_PARAMETER_NAME = "_notMapped";

	private boolean prepopulateFromRequestParameters;

	public void setPrepopulateFromRequest(boolean prepopulate) {
		this.prepopulateFromRequestParameters = prepopulate;
	}

	protected static class PopulateFormReferenceDataException extends RuntimeException {
		public PopulateFormReferenceDataException(String message, Throwable cause) {
			super(message, cause);
		}

		public PopulateFormReferenceDataException(Exception cause) {
			super(cause);
		}

		public String getMessage() {
			if (StringUtils.hasText(super.getMessage())) {
				return super.getMessage();
			}
			else {
				if (getCause() instanceof ServletRequestBindingException) {
					return "Unable to populate form reference data due to request parameter binding error - programmer error "
							+ "likely in submitting view code or request access code; see cause for more details.";
				}
				else {
					return "Unable to populate form reference data";
				}
			}
		}
	}

	protected ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) {
		Object formObject = loadRequiredFormObject(request, model);
		ServletRequestDataBinder binder = createBinder(request, formObject, model);
		if (prepopulateFromRequestParameters) {
			if (logger.isDebugEnabled()) {
				logger.debug("Prepopulating backing form object from request parameters");
			}
			binder.bind(request);
		}
		exportErrors(binder.getErrors(), model);
		exportViewMarkers(request, model);
		try {
			populateFormReferenceData(request, model);
		}
		catch (ServletRequestBindingException e) {
			throw new PopulateFormReferenceDataException(e);
		}
		return binder.getErrors().hasErrors() ? error() : success();
	}

	protected void exportViewMarkers(HttpServletRequest request, MutableAttributesAccessor model) {
		if (logger.isDebugEnabled()) {
			logger.debug("Exporting view markers/placeholders to notify business-tier developers of "
					+ "form fields that are missing, not mapped to backing objects, or need clarification");
			// placeholders indicating attributes on forms that have not been
			// mapped to dynamic fields on backing objects - for use during
			// development only
			model.setAttribute(NOT_MAPPED_MARKER_PARAMETER_NAME, NOT_MAPPED_MARKER_VALUE);
			model.setAttribute(NOT_MAPPED_COLLECTION_MARKER_PARAMETER_NAME, createNotMappedEnumSet());
		}
	}

	private Set createNotMappedEnumSet() {
		Set notMapped = new HashSet();
		notMapped.add(new NotMappedEnum(1));
		notMapped.add(new NotMappedEnum(2));
		notMapped.add(new NotMappedEnum(3));
		return notMapped;
	}

	private static final class NotMappedEnum extends ShortCodedEnum {
		public NotMappedEnum(int code) {
			super(code, NOT_MAPPED_MARKER_VALUE);
		}
	}

	/**
	 * Template method to be implemented by subclasses
	 * @param request current HTTP request
	 * @param model the flow model
	 */
	protected void populateFormReferenceData(HttpServletRequest request, MutableAttributesAccessor model)
			throws PopulateFormReferenceDataException, ServletRequestBindingException {

	}

}