package org.springframework.web.struts;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.WebUtils;

/**
 * Convenience class for Spring-aware Struts 1.1 Actions.
 *
 * <p>Provides a reference to the current Spring application context, e.g.
 * for bean lookup or resource loading. Auto-detects a ContextLoaderPlugIn
 * context, falling back to the root WebApplicationContext. For typical
 * usage, i.e. accessing middle tier beans, use a root WebApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 06.04.2004
 * @see ContextLoaderPlugIn#SERVLET_CONTEXT_ATTRIBUTE
 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
 * @see org.springframework.web.context.ContextLoaderListener
 * @see org.springframework.web.context.ContextLoaderServlet
 */
public abstract class ActionSupport extends Action {

	private WebApplicationContext webApplicationContext;

	private MessageSourceAccessor messageSourceAccessor;

	/**
	 * Initialize the WebApplicationContext for this Action.
	 * Invokes onInit after successful initialization of the context.
	 * @see #initWebApplicationContext
	 * @see #onInit
	 */
	public void setServlet(ActionServlet actionServlet) {
		super.setServlet(actionServlet);
		if (actionServlet != null) {
			this.webApplicationContext = initWebApplicationContext(actionServlet);
			this.messageSourceAccessor = new MessageSourceAccessor(this.webApplicationContext);
			onInit();
		}
		else {
			onDestroy();
		}
	}

	/**
	 * Fetch ContextLoaderPlugIn's WebApplicationContext from the ServletContext,
	 * falling back to the root WebApplicationContext (the usual case).
	 * @param actionServlet the associated ActionServlet
	 * @return the WebApplicationContext
	 * @throws IllegalStateException if no WebApplicationContext could be found
	 * @see ContextLoaderPlugIn#SERVLET_CONTEXT_ATTRIBUTE
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
	 */
	protected WebApplicationContext initWebApplicationContext(ActionServlet actionServlet)
			throws IllegalStateException {
		ServletContext sc = actionServlet.getServletContext();
		WebApplicationContext wac = (WebApplicationContext)
				sc.getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_ATTRIBUTE);
		if (wac == null) {
			wac = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
		}
		return wac;
	}

	/**
	 * Return the current Spring WebApplicationContext.
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}

	/**
	 * Return a MessageSourceAccessor for the application context
	 * used by this object, for easy message access.
	 */
	protected final MessageSourceAccessor getMessageSourceAccessor() {
		return this.messageSourceAccessor;
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
	protected final File getTempDir() {
		return WebUtils.getTempDir(getServletContext());
	}

	/**
	 * Callback for custom initialization after the context has been set up.
	 * @see #setServlet
	 */
	protected void onInit() {
	}

	/**
	 * Callback for custom destruction when the ActionServlet shuts down.
	 * @see #setServlet
	 */
	protected void onDestroy() {
	}

}
