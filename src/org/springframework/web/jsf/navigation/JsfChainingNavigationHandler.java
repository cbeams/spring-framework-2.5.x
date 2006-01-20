/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.web.jsf.navigation;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import org.springframework.web.jsf.navigation.DelegatingNavigationHandler.NavigationHandlerContext;

/**
 * <code>NavigationHandler</code> which is meant to chain to any JSF-managed
 * NavigationHandler, which will have been loaded in a ThreadLocal by
 * DelegatingNavigationHandler. This NavigationHandler should be injected
 * (via constructor injection) as the last nested NavigationHandler in your
 * chain of Spring-managed NavigationHandlers.
 * 
 * @author Colin Sampaleanu
 * @since 1.2.7
 * @see org.springframework.webflow.jsf.util.navigation.DelegatingNavigationHandler
 */
public class JsfChainingNavigationHandler extends NavigationHandler {

	/**
     * Handle the navigation request implied by the specified parameters. Does
     * nothing if there is no ThreadLocal-bound navigation handler
     * 
     * @param facesContext
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public void handleNavigation(FacesContext facesContext, String fromAction,
			String outcome) {
		NavigationHandler threadBoundHandler = NavigationHandlerContext
				.getNavigationHandler();
		if (threadBoundHandler != null) {
			threadBoundHandler.handleNavigation(facesContext, fromAction, outcome);
		}
	}
}