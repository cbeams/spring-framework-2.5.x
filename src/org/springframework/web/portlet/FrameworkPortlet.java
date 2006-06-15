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

package org.springframework.web.portlet;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;
import org.springframework.web.portlet.context.PortletRequestHandledEvent;
import org.springframework.web.portlet.context.XmlPortletApplicationContext;

/**
 * Base portlet for portlets within the portlet framework. Allows integration
 * with an application context, in a JavaBean-based overall solution.
 *
 * <p>This class offers the following functionality:
 * <ul>
 * <li>Uses a Portlet ApplicationContext to access a BeanFactory. The portlet's
 * configuration is determined by beans in the portlet's namespace.
 * <li>Publishes events on request processing, whether or not a request is
 * successfully handled.
 * </ul>
 *
 * <p>Subclasses must implement <code>doActionService</code> and <code>doRenderService</code>
 * to handle action and render requests. Because this extends PortletBean rather
 * than Portlet directly, bean properties are mapped onto it. Subclasses can override
 * initFrameworkPortlet() for custom initialization.
 *
 * <p>Regards a "contextClass" parameter at the portlet init-param level,
 * falling back to the default context class (XmlPortletApplicationContext) if not found.
 * With the default FrameworkPortlet, a context class needs to implement
 * ConfigurablePortletApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" portlet init-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by any
 * number of commas and spaces, like "test-portlet.xml, myPortlet.xml".
 * If not explicitly specified, the context implementation is supposed to build a
 * default location from the namespace of the portlet.
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>The default namespace is "'portlet-name'-portlet", e.g. "test-portlet" for a
 * portlet-name "test" (leading to a "/WEB-INF/test-portlet.xml" default location
 * with XmlPortletApplicationContext). The namespace can also be set explicitly via
 * the "namespace" portlet init-param.
 *
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 * @author Juergen Hoeller
 * @since 2.0
 * @see #doActionService
 * @see #doRenderService
 * @see #initFrameworkPortlet
 * @see #setContextClass
 * @see #setContextConfigLocation
 * @see #setNamespace
 */
public abstract class FrameworkPortlet extends PortletBean {

	/**
	 * Default context class for FrameworkPortlet.
	 * @see org.springframework.web.portlet.context.XmlPortletApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlPortletApplicationContext.class;

	/**
	 * Suffix for Portlet ApplicationContext namespaces. If a portlet of this class is
	 * given the name "test" in a context, the namespace used by the portlet will
	 * resolve to "test-portlet".
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-portlet";

	/**
	 * Prefix for the PortletContext attribute for the Portlet ApplicationContext.
	 * The completion is the portlet name.
	 */
	public static final String PORTLET_CONTEXT_PREFIX = FrameworkPortlet.class.getName() + ".CONTEXT.";

	/**
	 * Default USER_INFO attribute names to search for the current username:
	 * "user.login.id", "user.name".
	 */
	public static final String[] DEFAULT_USERINFO_ATTRIBUTE_NAMES = {"user.login.id", "user.name"};


	/**
	 * Portlet ApplicationContext implementation class to use
	 */
	private Class contextClass = DEFAULT_CONTEXT_CLASS;

	/**
	 * Namespace for this portlet
	 */
	private String namespace;

	/**
	 * Explicit context config location
	 */
	private String contextConfigLocation;

	/**
	 * Should we publish the context as a PortletContext attribute?
	 */
	private boolean publishContext = true;

	/**
	 * Should we publish a PortletRequestHandledEvent at the end of each request?
	 */
	private boolean publishEvents = true;

	/**
	 * USER_INFO attributes that may contain the username of the current user
	 */
	private String[] userinfoUsernameAttributes = DEFAULT_USERINFO_ATTRIBUTE_NAMES;

	/**
	 * ApplicationContext for this portlet
	 */
	private ApplicationContext portletApplicationContext;


	/**
	 * Set a custom context class. This class must be of type ApplicationContext;
	 * when using the default FrameworkPortlet implementation, the context class
	 * must also implement ConfigurablePortletApplicationContext.
	 * @see #createPortletApplicationContext
	 */
	public void setContextClass(Class contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Return the custom context class.
	 */
	public Class getContextClass() {
		return contextClass;
	}

	/**
	 * Set a custom namespace for this portlet,
	 * to be used for building a default context config location.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Return the namespace for this portlet, falling back to default scheme if
	 * no custom namespace was set. (e.g. "test-portlet" for a portlet named "test")
	 */
	public String getNamespace() {
		return (this.namespace != null) ? this.namespace : getPortletName() + DEFAULT_NAMESPACE_SUFFIX;
	}

	/**
	 * Set the context config location explicitly, instead of relying on the default
	 * location built from the namespace. This location string can consist of
	 * multiple locations separated by any number of commas and spaces.
	 */
	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	/**
	 * Return the explicit context config location, if any.
	 */
	public String getContextConfigLocation() {
		return contextConfigLocation;
	}

	/**
	 * Set whether to publish this portlet's context as a PortletContext attribute,
	 * available to all objects in the web container. Default is true.
	 * <p>This is especially handy during testing, although it is debatable whether
	 * it's good practice to let other application objects access the context this way.
	 */
	public void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}

	/**
	 * Return whether to publish this portlet's context as a PortletContext attribute.
	 */
	public boolean isPublishContext() {
		return publishContext;
	}

	/**
	 * Set whether this portlet should publish a PortletRequestHandledEvent at the end
	 * of each request. Default is true; can be turned off for a slight performance
	 * improvement, provided that no ApplicationListeners rely on such events.
	 * @see org.springframework.web.portlet.context.PortletRequestHandledEvent
	 */
	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}

	/**
	 * Return whether this portlet should publish a PortletRequestHandledEvent at the end
	 * of each request.
	 */
	public boolean isPublishEvents() {
		return publishEvents;
	}

	/**
	 * Set the list of attributes to search in the USER_INFO map when trying
	 * to find the username of the current user.
	 * @see #getUsernameForRequest
	 */
	public void setUserinfoUsernameAttributes(String[] userinfoUsernameAttributes) {
		this.userinfoUsernameAttributes = userinfoUsernameAttributes;
	}

	/**
	 * Returns the list of attributes that will be searched in the USER_INFO map
	 * when trying to find the username of the current user
	 * @see #getUsernameForRequest
	 */
	public String[] getUserinfoUsernameAttributes() {
		return userinfoUsernameAttributes;
	}


	/**
	 * Overridden method of PortletBean, invoked after any bean properties
	 * have been set. Creates this portlet's ApplicationContext.
	 */
	protected final void initPortletBean() throws PortletException, BeansException {
		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled()) {
			logger.info("FrameworkPortlet '" + getPortletName() + "': initialization started");
		}

		try {
			this.portletApplicationContext = initPortletApplicationContext();
			initFrameworkPortlet();
		}
		catch (PortletException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}
		catch (BeansException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("FrameworkPortlet '" + getPortletName() + "': initialization completed in " + elapsedTime + " ms");
		}
	}

	/**
	 * Initialize and publish the PortletApplicationContext for this portlet.
	 * Delegates to createPortletApplicationContext for actual creation.
	 * Can be overridden in subclasses.
	 * @return the Portlet ApplicationContext for this portlet
	 * @throws BeansException if the context couldn't be initialized
	 * @see #createPortletApplicationContext
	 */
	protected ApplicationContext initPortletApplicationContext() throws BeansException {
		getPortletContext().log("Loading PortletApplicationContext for Spring FrameworkPortlet '" + getPortletName() + "'");

		ApplicationContext parent = PortletApplicationContextUtils.getWebApplicationContext(getPortletContext());
		ApplicationContext pac = createPortletApplicationContext(parent);
		if (logger.isInfoEnabled()) {
			logger.info("Using context class '" + pac.getClass().getName() + "' for portlet '" +
					getPortletName() + "'");
		}

		if (isPublishContext()) {
			// publish the context as a portlet context attribute
			String attName = getPortletContextAttributeName();
			getPortletContext().setAttribute(attName, pac);
			if (logger.isDebugEnabled()) {
				logger.debug("Published PortletApplicationContext of portlet '" + getPortletName() +
						"' as PortletContext attribute with name [" + attName + "]");
			}
		}
		return pac;
	}

	/**
	 * Instantiate the PortletApplicationContext for this portlet, either a default
	 * XmlPortletApplicationContext or a custom context class if set. This implementation
	 * expects custom contexts to implement ConfigurablePortletApplicationContext.
	 * Can be overridden in subclasses.
	 * @param parent the parent ApplicationContext to use, or null if none
	 * @return the Portlet ApplicationContext for this portlet
	 * @throws BeansException if the context couldn't be initialized
	 * @see #setContextClass
	 * @see org.springframework.web.portlet.context.XmlPortletApplicationContext
	 */
	protected ApplicationContext createPortletApplicationContext(ApplicationContext parent)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Portlet with name '" + getPortletName() +
					"' will try to create custom PortletApplicationContext context of class '" +
					getContextClass().getName() + "'" + ", using parent context [" + parent + "]");
		}
		if (!ConfigurablePortletApplicationContext.class.isAssignableFrom(getContextClass())) {
			throw new ApplicationContextException("Fatal initialization error in portlet with name '" + getPortletName() +
					"': custom PortletApplicationContext class [" + getContextClass().getName() +
					"] is not of type ConfigurablePortletApplicationContext");
		}

		ConfigurablePortletApplicationContext pac =
				(ConfigurablePortletApplicationContext) BeanUtils.instantiateClass(getContextClass());
		pac.setParent(parent);
		pac.setPortletContext(getPortletContext());
		pac.setPortletConfig(getPortletConfig());
		pac.setNamespace(getNamespace());
		if (getContextConfigLocation() != null) {
			pac.setConfigLocations(StringUtils.tokenizeToStringArray(getContextConfigLocation(),
					ConfigurablePortletApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}
		pac.refresh();
		return pac;
	}

	/**
	 * Return the PortletContext attribute name for this portlets's PortletApplicationContext.
	 * Default implementation returns PORTLET_CONTEXT_PREFIX + portlet name.
	 * @see #PORTLET_CONTEXT_PREFIX
	 * @see #getPortletName
	 */
	public String getPortletContextAttributeName() {
		return PORTLET_CONTEXT_PREFIX + getPortletName();
	}

	/**
	 * Return this portlet's ApplicationContext.
	 */
	public final ApplicationContext getPortletApplicationContext() {
		return portletApplicationContext;
	}

	/**
	 * This method will be invoked after any bean properties have been set and
	 * the PortletApplicationContext has been loaded. The default implementation is empty;
	 * subclasses may override this method to perform any initialization they require.
	 *
	 * @throws PortletException in case of an initialization exception
	 * @throws BeansException if thrown by ApplicationContext methods
	 */
	protected void initFrameworkPortlet() throws PortletException, BeansException {
	}


	/**
	 * Delegate render requests to processRequest/doRenderService.
	 */
	protected final void doDispatch(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate action requests to processRequest/doActionService.
	 */
	public final void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {

		processRequest(request, response);
	}
	
	/**
	 * Process this request, publishing an event regardless of the outcome.
	 * The actual event handling is performed by the abstract
	 * <code>doActionService()</code> and <code>doRenderService()</code> template methods.
	 * @see #doActionService
	 * @see #doRenderService
	 */
	protected final void processRequest(PortletRequest request, PortletResponse response)
			throws PortletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		try {
			if (request instanceof ActionRequest) {
				doActionService((ActionRequest) request, (ActionResponse) response);
			}
			else {
				doRenderService((RenderRequest) request, (RenderResponse) response);
			}
		}
		catch (PortletException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Throwable ex) {
			failureCause = ex;
			throw new PortletException("Request processing failed", ex);
		}

		finally {
			if (failureCause != null) {
				logger.error("Could not complete request", failureCause);
			}
			else {
				logger.debug("Successfully completed request");
			}
			if (isPublishEvents()) {
				// Whether or not we succeeded, publish an event.
				long processingTime = System.currentTimeMillis() - startTime;
				this.portletApplicationContext.publishEvent(
						new PortletRequestHandledEvent(this,
								getPortletConfig().getPortletName(), request.getPortletMode().toString(),
								(request instanceof ActionRequest ? "action" : "render"),
								request.getRequestedSessionId(), getUsernameForRequest(request),
								processingTime, failureCause));
			}
		}
	}

	/**
	 * Determine the username for the given request.
	 * Default implementation first tries the UserPrincipal.
	 * If that does not exist, then it checks the USER_INFO map.
	 * Can be overridden in subclasses.
	 * @param request current portlet request
	 * @return the username, or null if none
	 * @see javax.portlet.PortletRequest#getUserPrincipal
	 * @see javax.portlet.PortletRequest#getRemoteUser
	 * @see javax.portlet.PortletRequest#USER_INFO
	 * @see #setUserinfoUsernameAttributes(String[])
	 */
	protected String getUsernameForRequest(PortletRequest request) {
		// Try the Principal.
		Principal userPrincipal = request.getUserPrincipal();
		if (userPrincipal != null) {
			return userPrincipal.getName();
		}

		// Try the remote user name.
		String userName = request.getRemoteUser();
		if (userName != null) {
			return userName;
		}
		
		// Try the Portlet USER_INFO map.
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
		if (userInfo != null) {
			for (int i = 0, n = this.userinfoUsernameAttributes.length; i < n; i++) {
				userName = (String) userInfo.get(this.userinfoUsernameAttributes[i]);
				if (userName != null) {
					return userName;
				}
			}
		}
		
		// Nothing worked...
		return null;
	}

	/**
	 * Subclasses must implement this method to do the work of render request handling.
	 * <p>The contract is essentially the same as that for the <code>doDispatch</code>
	 * method of GenericPortlet.
	 * <p>This class intercepts calls to ensure that exception handling and
	 * event publication takes place.
	 * @param request current render request
	 * @param response current render response
	 * @throws Exception in case of any kind of processing failure
	 * @see javax.portlet.GenericPortlet#doDispatch
	 */
	protected abstract void doRenderService(RenderRequest request, RenderResponse response)
			throws Exception;

	/**
	 * Subclasses must implement this method to do the work of action request handling.
	 * <p>The contract is essentially the same as that for the <code>processAction</code>
	 * method of GenericPortlet.
	 * <p>This class intercepts calls to ensure that exception handling and
	 * event publication takes place.
	 * @param request current action request
	 * @param response current action response
	 * @throws Exception in case of any kind of processing failure
	 * @see javax.portlet.GenericPortlet#processAction
	 */
	protected abstract void doActionService(ActionRequest request, ActionResponse response)
			throws Exception;


	/**
	 * Close the PortletApplicationContext of this portlet.
	 * @see org.springframework.context.ConfigurableApplicationContext#close
	 */
	public void destroy() {
		// Close the portlet application context of this portlet.
		getPortletContext().log("Closing PortletApplicationContext of Spring FrameworkPortlet '" + getPortletName() + "'");
		if (this.portletApplicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.portletApplicationContext).close();
		}
	}

}
