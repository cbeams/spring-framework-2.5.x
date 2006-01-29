/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.jsf;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

/**
 * Base class for JSF NavigationHandler implementations that want
 * to be capable of decorating an original NavigationHandler.
 *
 * <p>Supports the standard JSF style of decoration (through a constructor argument)
 * as well as an overloaded <code>handleNavigation</code> method with explicit
 * NavigationHandler argument (passing in the original NavigationHandler). Subclasses
 * are forced to implement this overloaded <code>handleNavigation</code> method.
 * Standard JSF invocations will automatically delegate to the overloaded method,
 * with the constructor-injected NavigationHandler as argument.
 *
 * @author Juergen Hoeller
 * @since 1.2.7
 */
public abstract class DecoratingNavigationHandler extends NavigationHandler {

	private NavigationHandler originalNavigationHandler;


	/**
	 * Create a DecoratingNavigationHandler without fixed original NavigationHandler.
	 */
	protected DecoratingNavigationHandler() {
	}

	/**
	 * Create a DecoratingNavigationHandler with fixed original NavigationHandler.
	 * @param originalNavigationHandler the original NavigationHandler to decorate
	 */
	protected DecoratingNavigationHandler(NavigationHandler originalNavigationHandler) {
		this.originalNavigationHandler = originalNavigationHandler;
	}


	/**
	 * This implementation of the standard JSF <code>handleNavigation</code> method
	 * delegates to the overloaded variant, passing in constructor-injected
	 * NavigationHandler as argument.
	 * @see #handleNavigation(javax.faces.context.FacesContext, String, String, javax.faces.application.NavigationHandler)
	 */
	public final void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
		handleNavigation(facesContext, fromAction, outcome, this.originalNavigationHandler);
	}

	/**
	 * Special <code>handleNavigation</code> variant with explicit NavigationHandler
	 * argument. Either called directly, by code with an explicit original handler,
	 * or called from the standard <code>handleNavigation</code> method, as
	 * plain JSF-defined NavigationHandler.
	 * @param facesContext the current JSF context
	 * @param fromAction the action binding expression that was evaluated to retrieve the
	 * specified outcome, or <code>null</code> if the outcome was acquired by some other means
	 * @param outcome the logical outcome returned by a previous invoked application action
	 * (which may be <code>null</code>)
	 * @param originalNavigationHandler the original NavigationHandler,
	 * or <code>null</code> if none
	 */
	public abstract void handleNavigation(
			FacesContext facesContext, String fromAction, String outcome, NavigationHandler originalNavigationHandler);

}
