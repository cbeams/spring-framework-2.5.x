package org.springframework.remoting.jaxrpc;

import java.io.File;

import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.WebUtils;

/**
 * Convenience base class for JAX-RPC servlet endpoint implementations.
 * Provides a reference to the current Spring application context,
 * e.g. for bean lookup or resource loading.
 *
 * <p>The Web Service servlet needs to run in the same web application
 * as the Spring context to allow for access to Spring's facilities.
 * In case of Axis, copy the AxisServlet definition into your web.xml,
 * and set up the endpoint in "server-config.wsdd" (or use the deploy tool).
 *
 * <p>This class does not extend WebApplicationContextSupport to not expose
 * any public setters. For some reason, Axis tries to resolve public setters
 * with WSDL means... with private and protected means, everything works.
 *
 * @author Juergen Hoeller
 * @since 16.12.2003
 * @see #init
 * @see #getWebApplicationContext
 */
public class ServletEndpointSupport implements ServiceLifecycle {

	protected final Log logger = LogFactory.getLog(getClass());
	
	private ServletEndpointContext servletEndpointContext;

	private WebApplicationContext webApplicationContext;

	/**
	 * Initialize this JAX-RPC servlet endpoint.
	 * Calls onInit after successful context initialization.
	 * @param context ServletEndpointContext
	 * @throws ServiceException if the context is not a ServletEndpointContext
	 * @see #onInit
	 */
	public final void init(Object context) throws ServiceException {
		if (!(context instanceof ServletEndpointContext)) {
			throw new ServiceException("ServletEndpointSupport needs ServletEndpointContext, not [" + context + "]");
		}
		this.servletEndpointContext = (ServletEndpointContext) context;
		ServletContext servletContext = this.servletEndpointContext.getServletContext();
		this.webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		onInit();
	}

	/**
	 * Return the current JAX-RPC ServletEndpointContext.
	 */
	protected final ServletEndpointContext getServletEndpointContext() {
		return servletEndpointContext;
	}

	/**
	 * Return the current Spring WebApplicationContext.
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}

	/**
	 * Return the current ServletContext.
	 */
	protected final ServletContext getServletContext() {
		return this.webApplicationContext.getServletContext();
	}

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @return the File representing the temporary directory
	 */
	protected File getTempDir() {
		return WebUtils.getTempDir(getServletContext());
	}

	/**
	 * Callback for custom initialization after the context has been set up.
	 */
	protected void onInit() {
	}

	/**
	 * This implementation of destroy is empty.
	 * Can be overridden in subclasses.
	 */
	public void destroy() {
	}

}
