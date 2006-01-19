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
 * NavigationHandler implementation that delegates to a NavigationHandler
 * obtained from a Spring ApplicationContext.  The handler in the context 
 * may nest other handlers (via constructor injection) and so on, in 
 * standard JSF fashion, forming a chain. If the last nested handler is an
 * instance of {@link org.springframework.web.jsf.navigation.JsfChainingNavigationHandler},
 * control will return back to the original handler that was passed in by
 * JSF to the constructor of this class. Normally the latter handler will
 * be the default JSF NavigationHandler, or any previous NavigationHandler
 * defined in faces-config.xml.
 * <p>  
 * <p>Configure this handler in your <code>faces-config.xml</code> file as follows:
 *
 * <pre>
 * &lt;navigation-handler>
 *	  org.springframework.jsf.navigation.DelegatingNavigationHandler
 * &lt;/navigation-handler></pre>
 * 
 * The ApplicationConext will be searched for the NavigationHandler under the 
 * bean name {@link #NAVIGATION_HANDLER_BEAN}. If this is just a single bean
 * with no nested handlers, then delegation will terminate at that handler.
 * 
 * <pre>
 * &lt;bean name="jsfNavigationHandler" class="com.mycompany.MyNavigtionHandler"/></pre> 
 *
 * On the other hand, most NavigationHandlers that are not supposed to be the sole
 * NavigationHandler in the application are by convention expected to provide a 
 * constructor which takes a subsequent NavigationHandler to delegate to if the
 * first class does not know how to handle navigation. In this fashion, you may 
 * chain NavigationHandlers by using nested bean definitions. As long as the last nested
 * bean is an instance of JsfChainingNavigationHandler, then delegation will chain
 * back to the original NavigationHandler instance that was passed to the constructor
 * of DelegatingNavigationHandler by JSF. 
 *  
 * <pre>
 * <!-- our chain of navigationhandlers --> 
 * &lt;bean name="jsfNavigationHandler" 
 *          class="org.springframework.webflow.manager.jsf.FlowNavigationHandler"> 
 * &lt;constructor-arg> 
 *  &lt;bean class="com.mycompany.MyNestedNavHandler"> 
 *    &lt;!-- JsfChainingNavigationHandler delegates back to any JSF-managed handlers --> 
 *    &lt;constructor-arg> 
 *      &lt;bean class="org.springframework.webflow.jsf.util.navigation.JsfChainingNavigationHandler"/> 
 *    &lt;/constructor-arg> 
 *  &lt;/bean> 
 * &lt;/bean></pre>
 * 
 * This class may be subclassed to change the bean name used to search for the
 * navigation handler, change the strategy used to get the handler, or change
 * the strategy used to get the ApplicationContext (normally obtained via
 * {@link FacesContextUtils#getWebApplicationContext(FacesContext)})
 * 
 * @author Colin Sampaleanu
 * @since 1.2.7
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
     * @return NavigationHandler to delegate to
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
	 * anything else. Normally retrieves ApplicationContext via
	 * {@link FacesContextUtils#getWebApplicationContext(FacesContext)}
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
	
	static abstract class NavigationHandlerContext {

		private static ThreadLocal navigationHandler = new InheritableThreadLocal();

		/**
		 * Return the NavigationHandler associated with the current thread,
		 * if any.
		 * @return the current NavigationHandler, or <code>null</code> if none
		 */
		public static NavigationHandler getNavigationHandler() {
			return (NavigationHandler) navigationHandler.get();
		}

		/**
		 * Associate the given NavigationHandler with the current thread.
		 * @param navigationHandler the current NavigationHandler, or <code>null</code> to reset
		 * the thread-bound NavigationHandler
		 */
		public static void setNavigationHandler(NavigationHandler handler) {
			navigationHandler.set(handler);
		}
	}
}
