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
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.flow.ActionBeanEvent;
import org.springframework.web.flow.MutableAttributesAccessor;

/**
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class PopulateFormAction extends BindAndValidateAction {

	private boolean prepopulateFromRequestParameters;

	public void setPrepopulateFromRequest(boolean prepopulate) {
		this.prepopulateFromRequestParameters = prepopulate;
	}

	protected ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException {

		Object formObject = loadRequiredFormObject(request, model);

		ServletRequestDataBinder binder = createBinder(request, formObject, model);

		if (prepopulateFromRequestParameters) {
			if (logger.isDebugEnabled()) {
				logger.debug("Prepopulating backing form object from request parameters");
			}
			binder.bind(request);
		}

		exportErrors(binder.getErrors(), model);

		// placeholders indicating attributes on forms that have not been
		// mapped to dynamic fields on backing objects - for use during
		// development only
		model.setAttribute("_notMapped", "!NOT MAPPED!");
		model.setAttribute("_notMappedItems", createNotMappedEnumSet());
		populateFormReferenceData(request, model);
		return binder.getErrors().hasErrors() ? error() : success();
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
			super(code, "!NOT MAPPED!");
		}
	}

	/**
	 * Template method to be implemented by subclasses
	 * @param request current HTTP request
	 * @param model the flow model
	 */
	protected void populateFormReferenceData(HttpServletRequest request, MutableAttributesAccessor model) {

	}

}