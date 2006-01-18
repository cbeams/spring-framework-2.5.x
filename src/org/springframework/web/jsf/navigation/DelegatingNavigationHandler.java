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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * NavigationHandler that is supposed to be deployed in faces-config.xml
 * and delegates to a Spring-managed NavigationHandler coming from a
 * Spring ApplicationContext. It will ensure that that as long as the last
 * NavigationHandler at the end of that chain (of nested handlers)
 * delegates to an instance of JsfChainingNavigationHandler, that call
 * will in turn delegate back to the JSF-managed NavigationHandler, that
 * has been passed into this class via a constructor argument. Normally the
 * latter will be the default JSF NavigationHandler, or any previous 
 * NavigationHandler defined in faces-config.xml.
 * 
 * @author Colin Sampaleanu
 * @see org.springframework.webflow.jsf.util.navigation.JsfChainingNavigationHandler
 */
public class DelegatingNavigationHandler extends NavigationHandler {

	public final static String NAVIGATION_HANDLER_BEAN = "jsfNavigationHandler";

	protected String		   navigationHandlerBean   = NAVIGATION_HANDLER_BEAN;

	/**
     * Last (JSF-managed) navigation handler in the chain.
     */
	private NavigationHandler  first;

	/**
     * Last (JSF-managed) navigation handler in the chain.
     */
	private NavigationHandler  last;

	/**
     * Creates a new DelegatingNavigationHandler
     * 
     * @param navigationHandler
     *            Next NavigationHandler in the chain
     */
	public DelegatingNavigationHandler(final NavigationHandler navigationHandler) {
		this.last = navigationHandler;
	}

	/**
     * Creates a new DelegatingNavigationHandler object.
     */
	public DelegatingNavigationHandler() {
	}

	
	/**
     * Extension point for subclasses. This method returns a
     * <code>NavigationHandler</code>s for this class to process, from the
     * Spring application context. By default, it looks for a bean with the name
     * {@link #NAVIGATION_HANDLER_BEAN}, and does so in a lazy fashion, only
     * once.
     * 
     * @param facesContext
     *            Current FacesContext
     * 
     * @return Iterator over chain of <code>NavigationHandler</code>s.
     */
	protected NavigationHandler getNavigationHandler(final FacesContext facesContext)
			throws BeansException {

		if (first != null)
			return first;

		ApplicationContext appContext = getApplicationContext(facesContext); 
		first = (NavigationHandler) appContext.getBean(navigationHandlerBean,
				NavigationHandler.class);
		return first;
	}
	
	/**
	 * Template method to get WebApplicationContext. Broken out more for unit tests than
	 * anything else. Normally retrieves ApplicationContext via FacesContextUtils
     * @param facesContext
     *            Current FacesContext
	 * @return a WebApplicatioContext
	 * @throws BeansException
	 */
	protected WebApplicationContext getApplicationContext(final FacesContext facesContext)
			throws BeansException {
		
		return FacesContextUtils.getWebApplicationContext(facesContext);
	}

	/**
     * Handle the navigation request implied by the specified parameters. We first
     * ensure that the original JSF NavigationHandler (if any) is placed in a 
     * ThreadLocal, to (optionally) be called at the end of the chain, and then
     * delegate to the NavigationHandler out of the application context.
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
		
		NavigationHandler handler = getNavigationHandler(facesContext);

		NavigationHandler originalThreadBoundHandler = NavigationHandlerContext
				.getNavigationHandler();
		try {
			NavigationHandlerContext.setNavigationHandler(last);
			handler.handleNavigation(facesContext, fromAction, outcome);
		} finally {
			NavigationHandlerContext.setNavigationHandler(originalThreadBoundHandler);
		}
	}
}
