/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.web.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * <p>
 * Extended <code>VariableResolver</code> that exposes the Spring
 * <code>WebApplicationContext</code> instance under a variable named with the
 * specified manifest constant.
 * </p>
 * 
 * @since 1.3
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 */
public class WebApplicationContextVariableResolver extends VariableResolver {

	// ------------------------------------------------------------- Constructor

	/**
     * <p>
     * Construct a new {@link WebApplicationContextVariableResolver} instance.
     * </p>
     * 
     * @param original
     *            Original resolver to delegate to.
     */
	public WebApplicationContextVariableResolver(VariableResolver original) {

		this.original = original;

	}

	// ------------------------------------------------------ Instance Variables

	/**
     * <p>
     * The original <code>VariableResolver</code> passed to our constructor.
     * </p>
     */
	private VariableResolver	original							  = null;

	// ------------------------------------------------------ Manifest Constants

	/**
     * <p>
     * Variable name to be resoved to our web application context.
     * </p>
     */
	private static final String WEB_APPLICATION_CONTEXT_VARIABLE_NAME = "webApplicationContext";

	// ------------------------------------------------ VariableResolver Methods

	/**
     * <p>
     * Resolve variable names known to this resolver; otherwise, delegate to the
     * original resolver passed to our constructor.
     * </p>
     * 
     * @param name
     *            Variable name to be resolved
     */
	public Object resolveVariable(FacesContext context, String name)
			throws EvaluationException {

		if (WEB_APPLICATION_CONTEXT_VARIABLE_NAME.equals(name)) {
			return FacesContextUtils.getWebApplicationContext(context);
		} else {
			return original.resolveVariable(context, name);
		}

	}
}
