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

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * AbstractTemplateView provides template based view technologies such as
 * Velocity and FreeMarker with the ability to use request and session
 * attributes in their model.
 *
 * <p>JSP/JSTL and other view technologies automatically have access to the
 * HttpServletRequest object and thereby the request/session attributes
 * for the current user.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since May 17, 2004
 * @version $Id: AbstractTemplateView.java,v 1.2 2004-05-21 19:33:38 jhoeller Exp $
 */
public abstract class AbstractTemplateView extends AbstractUrlBasedView {

	private boolean exposeRequestAttributes = false;

	private boolean exposeSessionAttributes = false;

	/**
	 * Set whether all request attributes should be added to the
	 * model prior to merging with the template.
	 */
	public void setExposeRequestAttributes(boolean exposeRequestAttributes) {
		this.exposeRequestAttributes = exposeRequestAttributes;
	}

	/**
	 * Set whether all HttpSession attributes should be added to the
	 * model prior to merging with the template.
	 */
	public void setExposeSessionAttributes(boolean exposeSessionAttributes) {
		this.exposeSessionAttributes = exposeSessionAttributes;
	}

	protected final void renderMergedOutputModel(Map model, HttpServletRequest request,
																							 HttpServletResponse response) throws Exception {

		if (this.exposeRequestAttributes) {
			for (Enumeration enum = request.getAttributeNames(); enum.hasMoreElements();) {
				String attribute = (String) enum.nextElement();
				Object attributeValue = request.getAttribute(attribute);
				if (logger.isDebugEnabled()) {
					logger.debug("Exposing request attribute '" + attribute + "' with value [" +
											 attributeValue + "] to model");
				}
				model.put(attribute, attributeValue);
			}
		}

		if (this.exposeSessionAttributes) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				for (Enumeration enum = session.getAttributeNames(); enum.hasMoreElements();) {
					String attribute = (String) enum.nextElement();
					Object attributeValue = session.getAttribute(attribute);
					if (logger.isDebugEnabled()) {
						logger.debug("Exposing session attribute '" + attribute + "' with value [" +
												 attributeValue + "] to model");
					}
					model.put(attribute, attributeValue);
				}
			}
		}

		renderMergedTemplateModel(model, request, response);
	}

	/**
	 * Subclasses must implement this method to actually render the view.
	 * @param model combined output Map, with request attributes and
	 * session attributes merged into it if required
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if rendering failed
	 */
	protected abstract void renderMergedTemplateModel(Map model, HttpServletRequest request,
																										HttpServletResponse response) throws Exception;

}
