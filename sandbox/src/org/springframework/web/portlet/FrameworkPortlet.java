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
package org.springframework.web.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContext;
import org.springframework.web.portlet.context.support.PortletRequestHandledEvent;
import org.springframework.web.portlet.context.support.PortletWebApplicationContextUtils;
import org.springframework.web.portlet.context.support.XmlPortletApplicationContext;


/**
 * Base portlet for portlets within the portlet framework. Allows integration
 * with an application context, in a JavaBean-based overall solution.
 *
 * <p>This class offers the following functionality:
 * <ul>
 * <li>Uses a PortletApplicationContext to access a BeanFactory. The portlet's
 * configuration is determined by beans in the portlet's namespace.
 * <li>Publishes events on request processing, whether or not a request is
 * successfully handled.
 * </ul>
 *
 * <p>Subclasses must implement doActionService() and doRenderService() to handle
 * action and render requests. Because this extends PortletBean rather than Portlet
 * directly, bean properties are mapped onto it. Subclasses can override
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
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @author Nick Lothian
 * @see #doService
 * @see #initFrameworkPortlet
 * @see #setContextClass
 * @see #setContextConfigLocation
 * @see #setNamespace
 */

public abstract class FrameworkPortlet extends PortletBean {


	/**
	 * Suffix for PortletApplicationContext namespaces. If a portlet of this class is
	 * given the name "test" in a context, the namespace used by the portlet will
	 * resolve to "test-portlet".
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-portlet";

	/**
	 * Default context class for FrameworkPortlet.
	 * @see org.springframework.web.context.support.XmlPortletApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlPortletApplicationContext.class;

	/**
	 * Prefix for the PortletContext attribute for the PortletApplicationContext.
	 * The completion is the portlet name.
	 */
	public static final String PORTLET_CONTEXT_PREFIX = FrameworkPortlet.class.getName() + ".CONTEXT.";
	
	/**
	 * Default URL to ViewRendererServlet.  This bridge servlet is used to convert
	 * portlet render requests to servlet requests in order to leverage the view support
	 * in org.springframework.web.view.
	 */
	public static final String DEFAULT_VIEW_RENDERER_SERVLET = "/WEB-INF/servlet/view";

	/** Custom PortletApplicationContext implementation class */
	private Class contextClass = DEFAULT_CONTEXT_CLASS;

	/** Namespace for this portlet */
	private String namespace;

	/** Explicit context config location */
	private String contextConfigLocation;

	/** Should we publish the context as a PortletContext attribute? */
	private boolean publishContext = true;

	/** PortletApplicationContext for this portlet */
	private PortletApplicationContext portletApplicationContext;

	/** ViewRendererServlet **/
	private String viewRendererServlet;

    /**
     * Return the ViewRendererServlet.
     */
    public String getViewRendererServlet() {
        return (viewRendererServlet != null ? viewRendererServlet : DEFAULT_VIEW_RENDERER_SERVLET);
    }
    
    /**
     * Set the ViewRendererServlet.  This servlet is used to ultimately render
     * all views in the portlet application.
     */
    public void setViewRendererServlet(String viewRendererServlet) {
        this.viewRendererServlet = viewRendererServlet;
    }

	/**
	 * Set a custom context class. This class must be of type PortletApplicationContext;
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
	 * no custom namespace was set: e.g. "test-portlet" for a portlet named "test".
	 */
	public String getNamespace() {
		return (namespace != null) ? namespace : getPortletName() + DEFAULT_NAMESPACE_SUFFIX;
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
	 * Overridden method of PortletBean, invoked after any bean properties
	 * have been set. Creates this portlet's PortletApplicationContext.
	 */
	protected final void initPortletBean() throws PortletException, BeansException {
		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled()) {
			logger.info("Framework portlet '" + getPortletName() + "' init");
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
			logger.info("Framework portlet '" + getPortletName() + "' init completed in " + elapsedTime + " ms");
		}
	}

	/**
	 * Initialize and publish the PortletApplicationContext for this portlet.
	 * Delegates to createPortletApplicationContext for actual creation.
	 * Can be overridden in subclasses.
	 * @throws BeansException if the context couldn't be initialized
	 * @see #createPortletApplicationContext
	 */
	protected PortletApplicationContext initPortletApplicationContext() throws BeansException {
	    PortletContext portletContext = getPortletContext();
		portletContext.log("Initializing PortletApplicationContext for portlet '" + getPortletName() + "'");
		WebApplicationContext parent = PortletWebApplicationContextUtils.getWebApplicationContext(portletContext);

		PortletApplicationContext pac = createPortletApplicationContext(parent);
		// Set the Servlet Context. This is required by the Spring Tag Libraries
		pac.setServletContext(parent.getServletContext());
				
		if (logger.isInfoEnabled()) {
			logger.info("Using context class '" + pac.getClass().getName() + "' for portlet '" + getPortletName() + "'");
		}

		if (this.publishContext) {
			// publish the context as a portlet context attribute
			String attName = getPortletContextAttributeName();
			portletContext.setAttribute(attName, pac);
			if (logger.isInfoEnabled()) {
				logger.info("Published PortletApplicationContext of portlet '" + getPortletName() +
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
	 * @throws BeansException if the context couldn't be initialized
	 * @see #setContextClass
	 * @see org.springframework.web.portlet.context.support.XmlPortletApplicationContext
	 */
	protected PortletApplicationContext createPortletApplicationContext(WebApplicationContext parent)
			throws BeansException {

		if (logger.isInfoEnabled()) {
			logger.info("Portlet with name '" + getPortletName() +
									"' will try to create custom PortletApplicationContext context of class '" +
									getContextClass().getName() + "'" + " using parent context [" + parent + "]");
		}
		if (!ConfigurablePortletApplicationContext.class.isAssignableFrom(getContextClass())) {
			throw new ApplicationContextException("Fatal initialization error in portlet with name '" +
																						getPortletName() + "': custom PortletApplicationContext class [" +
																						getContextClass().getName() +
																						"] is not of type ConfigurablePortletApplicationContext");
		}

		ConfigurablePortletApplicationContext cpac =
				(ConfigurablePortletApplicationContext) BeanUtils.instantiateClass(getContextClass());
		cpac.setParent(parent);
		cpac.setPortletContext(getPortletContext());
		cpac.setNamespace(getNamespace());
		if (this.contextConfigLocation != null) {
			cpac.setConfigLocations(
			    StringUtils.tokenizeToStringArray(this.contextConfigLocation,
			                                      ConfigurablePortletApplicationContext.CONFIG_LOCATION_DELIMITERS,
			                                      true, true));
		}
		cpac.refresh();
		return cpac;
	}

	/**
	 * Return the PortletContext attribute name for this portlet's
	 * PortletApplicationContext.
	 */
	public String getPortletContextAttributeName() {
		return PORTLET_CONTEXT_PREFIX + getPortletName();
	}

	/**
	 * Return this portlet's PortletApplicationContext.
	 */
	public final PortletApplicationContext getPortletApplicationContext() {
		return portletApplicationContext;
	}

	/**
	 * This method will be invoked after any bean properties have been set and
	 * the PortletApplicationContext has been loaded. The default implementation is empty;
	 * subclasses may override this method to perform any initialization they require.
	 * @throws PortletException in case of an initialization exception
	 * @throws BeansException if thrown by ApplicationContext methods
	 */
	protected void initFrameworkPortlet() throws PortletException, BeansException {
	}

	/**
	 * Overide GenericPortlet's doDispatch method to route all Render requests
	 * to the serviceWrapper. 
	 */
    protected final void doDispatch(RenderRequest request, RenderResponse response) 
    	throws PortletException, IOException {
        serviceWrapper(request, response);
    }

    /**
     * Route all Action requests to serviceWrapper.
     */
    public final void processAction(ActionRequest request, ActionResponse response) 
    	throws PortletException, IOException {
        serviceWrapper(request, response);
    }
	
	/**
	 * Handle this request, publishing an event regardless of the outcome.
	 * The actually event handling is performed by the abstract doActionService()
	 * and doRenderService() methods for Action requests and Render requests
	 * respectively.
	 */
	private void serviceWrapper(PortletRequest request, PortletResponse response)
	    throws PortletException, IOException {

		long startTime = System.currentTimeMillis();
		Exception failureCause = null;
		try {
            if (request instanceof ActionRequest) {
                doActionService((ActionRequest) request, (ActionResponse) response);
            } else {
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
		catch (RuntimeException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Exception ex) {
			failureCause = ex;
			throw new PortletException(ex.getMessage(), ex);
		}
		finally {
			long processingTime = System.currentTimeMillis() - startTime;
			// whether or not we succeeded, publish an event
			if (failureCause != null) {
				logger.error("Could not complete request", failureCause);
				this.portletApplicationContext.publishEvent(
				    new PortletRequestHandledEvent(this, request.getRemoteUser(), processingTime, 
				            getPortletConfig().getPortletName(), failureCause));
			}
			else {
				logger.debug("Successfully completed request");
				this.portletApplicationContext.publishEvent(
				    new PortletRequestHandledEvent(this, request.getRemoteUser(), processingTime,
				            getPortletConfig().getPortletName()));
			}
		}
	}

	/**
	 * Subclasses must implement this method to do the work of Action request handling.
	 * The contract is the same as that for the processAction method of Portlet.
	 * This class intercepts calls to ensure that event publication takes place.
	 * @see javax.portlet.Portlet#processAction
	 * @throws Exception in case of any kind of processing failure
	 */
	protected abstract void doActionService(ActionRequest request, ActionResponse response)
		throws Exception;
	
	/**
	 * Subclasses must implement this method to do the work of Render request handling.
	 * The contract is the same as that for the render method of Portlet.
	 * This class intercepts calls to ensure that event publication takes place.
	 * @see javax.portlet.Portlet#render
	 * @throws Exception in case of any kind of processing failure
	 */
	protected abstract void doRenderService(RenderRequest request, RenderResponse response)
		throws Exception;
	
	/**
	 * Close the PortletApplicationContext of this portlet.
	 * @see org.springframework.context.ConfigurableApplicationContext#close
	 */
	public void destroy() {
		// Close the portlet application context of this portlet.
		getPortletContext().log("Closing PortletApplicationContext of portlet '" + getPortletName() + "'");
		if (this.portletApplicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.portletApplicationContext).close();
		}
	}

}
