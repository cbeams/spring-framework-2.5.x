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

package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.View;

/**
 * Abstract base class for template view resolvers like
 * VelocityViewResolver and FreeMarkerViewResolver.
 *
 * <p>Provides a convenient way to specify AbstractTemplateView's
 * exposure flags for request attributes, session attributes,
 * and Spring's macro helpers.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see AbstractTemplateView
 * @see org.springframework.web.servlet.view.velocity.VelocityViewResolver
 * @see org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver
 */
public class AbstractTemplateViewResolver extends UrlBasedViewResolver {

	private boolean exposeRequestAttributes = false;

	private boolean exposeSessionAttributes = false;

	private boolean exposeSpringMacroHelpers = false;


	protected Class requiredViewClass() {
		return AbstractTemplateView.class;
	}

	/**
	 * Set whether all request attributes should be added to the
	 * model prior to merging with the template. Default is false.
	 * @see AbstractTemplateView#setExposeRequestAttributes
	 */
	public void setExposeRequestAttributes(boolean exposeRequestAttributes) {
		this.exposeRequestAttributes = exposeRequestAttributes;
	}

	/**
	 * Set whether all HttpSession attributes should be added to the
	 * model prior to merging with the template. Default is false.
	 * @see AbstractTemplateView#setExposeSessionAttributes
	 */
	public void setExposeSessionAttributes(boolean exposeSessionAttributes) {
		this.exposeSessionAttributes = exposeSessionAttributes;
	}

	/**
	 * Set whether to expose a RequestContext for use by Spring's macro library,
	 * under the name "springBindRequestContext". Default is false.
	 * @see AbstractTemplateView#setExposeSpringMacroHelpers
	 */
	public void setExposeSpringMacroHelpers(boolean exposeSpringMacroHelpers) {
		this.exposeSpringMacroHelpers = exposeSpringMacroHelpers;
	}


	protected View loadView(String viewName, Locale locale) throws BeansException {
		AbstractTemplateView view = (AbstractTemplateView) super.loadView(viewName, locale);
		view.setExposeRequestAttributes(this.exposeRequestAttributes);
		view.setExposeSessionAttributes(this.exposeSessionAttributes);
		view.setExposeSpringMacroHelpers(this.exposeSpringMacroHelpers);
		return view;
	}

}
