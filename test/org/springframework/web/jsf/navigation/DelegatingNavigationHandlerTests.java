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

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.web.jsf.MockFacesContext;

/**
 * Test for DelegatingNavigationHandler
 * 
 * @author Colin Sampaleanu
 */
public class DelegatingNavigationHandlerTests extends TestCase {

	public void testHandleNavigation() {

		MockFacesContext facesContext = new MockFacesContext();
		final StaticMockNavigationHandler firstHandler = new StaticMockNavigationHandler(
				new JsfChainingNavigationHandler());
		StaticMockNavigationHandler jsfHandler = new StaticMockNavigationHandler();

		DelegatingNavigationHandler delNavHandler = new DelegatingNavigationHandler(
				jsfHandler) {

			protected NavigationHandler getNavigationHandler(FacesContext facesContext)
					throws BeansException {
				return firstHandler;
			}
		};

		delNavHandler.handleNavigation(facesContext, "action", "myViewId");
		assertEquals(jsfHandler.lastFromAction, "fromAction");
		assertEquals(jsfHandler.lastOutcome, "myViewId");
	}

	public static class StaticMockNavigationHandler extends NavigationHandler {

		private NavigationHandler original;

		private String			lastFromAction;

		private String			lastOutcome;

		public StaticMockNavigationHandler() {
		}

		public StaticMockNavigationHandler(NavigationHandler original) {
			this.original = original;
		}

		/**
         * @param facesContext
         *            <code>FacesContext</code> for the current request
         * @param fromAction
         *            The action binding expression that was evaluated to
         *            retrieve the specified outcome (if any)
         * @param outcome
         *            The logical outcome returned by the specified action
         */
		public void handleNavigation(FacesContext facesContext, String fromAction,
				String outcome) {
			lastFromAction = fromAction;
			lastOutcome = outcome;
			if (original != null)
				original.handleNavigation(facesContext, fromAction, outcome);
		}
	}
}
