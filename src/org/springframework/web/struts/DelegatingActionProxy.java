package org.springframework.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import org.springframework.web.context.WebApplicationContext;

/**
 * Proxy for a Spring-managed Struts 1.1 Action that's defined in
 * ContextLoaderPlugIn's WebApplicationContext.
 *
 * <p>The proxy is defined in the Struts config file, specifying this
 * class as action class. It will delegate to a Struts Action bean
 * in the ContextLoaderPlugIn context.
 *
 * <pre>
 * &lt;action path="/login" type="org.springframework.web.struts.DelegatingActionProxy"/&gt;</pre>
 *
 * The name of the Action bean in the WebApplicationContext will be
 * determined from the mapping path and module prefix. This can be
 * customized by overriding the <code>determineActionBeanName</code> method.
 *
 * <p>Example:
 * <ul>
 * <li>mapping path "/login" -> bean name "/login"<br>
 * <li>mapping path "/login", module prefix "/mymodule" ->
 * bean name "/mymodule/login"
 * </ul>
 *
 * <p>A corresponding bean definition in the ContextLoaderPlugin
 * context looks as follows, being able to fully leverage
 * Spring's configuration facilities:
 *
 * <pre>
 * &lt;bean name="/login" class="myapp.MyAction"&gt;
 *   &lt;property name="..."&gt;...&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Note: The idea of delegating to Spring-managed Struts Actions originated in
 * Don Brown's <a href="http://struts.sourceforge.net/struts-spring">Spring Struts Plugin</a>.
 * ContextLoaderPlugIn and DelegatingActionProxy constitute a clean-room
 * implementation of the same idea, essentially superseding the original plugin.
 * Many thanks to Don Brown and Matt Raible for the original work, and for the
 * agreement to reimplement the idea in standard Spring!
 *
 * @author Juergen Hoeller
 * @since 05.04.2004
 * @see ContextLoaderPlugIn
 * @see #determineActionBeanName
 */
public class DelegatingActionProxy extends Action {

	protected final Log logger = LogFactory.getLog(getClass());

	private WebApplicationContext webApplicationContext;

	/**
	 * Initialize the WebApplicationContext for this Action.
	 * @see #initWebApplicationContext
	 */
	public void setServlet(ActionServlet actionServlet) {
		super.setServlet(actionServlet);
		if (actionServlet != null) {
			this.webApplicationContext = initWebApplicationContext(actionServlet);
		}
	}

	/**
	 * Fetch ContextLoaderPlugIn's WebApplicationContext from the
	 * ServletContext, containing the Struts Action beans to delegate to.
	 * @param actionServlet the associated ActionServlet
	 * @return the WebApplicationContext
	 * @throws IllegalStateException if no WebApplicationContext could be found
	 * @see ContextLoaderPlugIn#SERVLET_CONTEXT_ATTRIBUTE
	 */
	protected WebApplicationContext initWebApplicationContext(ActionServlet actionServlet)
			throws IllegalStateException {
		WebApplicationContext wac = (WebApplicationContext)
				this.servlet.getServletContext().getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_ATTRIBUTE);
		if (wac == null) {
			throw new IllegalStateException("Could not find ContextLoaderPlugIn's WebApplicationContext as " +
																			"ServletContext attribute [" + ContextLoaderPlugIn.SERVLET_CONTEXT_ATTRIBUTE +
																			"] - did you register " + ContextLoaderPlugIn.class.getName() + "?");
		}
		return wac;
	}

	/**
	 * Return the WebApplicationContext that this proxy delegates to.
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	/**
	 * Pass the execute call on to the Spring-managed delegate Action.
	 * @see #getDelegateAction
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
															 HttpServletResponse response) throws Exception {
		Action delegateAction = getDelegateAction(mapping);
		return delegateAction.execute(mapping, form, request, response);
	}

	/**
	 * Return the delegate Action for the given mapping.
	 * <p>The default implementation determines a bean name from the
	 * given ActionMapping and looks up the corresponding bean in the
	 * WebApplicationContext.
	 * @param mapping the Struts ActionMapping
	 * @return the delegate Action
	 * @see #determineActionBeanName
	 */
	protected Action getDelegateAction(ActionMapping mapping) {
		String beanName = determineActionBeanName(mapping);
		return (Action) this.webApplicationContext.getBean(beanName, Action.class);
	}

	/**
	 * Determine the name of the Action bean, to be looked up in
	 * the WebApplicationContext.
	 * <p>The default implementation takes the mapping path and
	 * prepends the module prefix, if any.
	 * @param mapping the Struts ActionMapping
	 * @return the name of the Action bean
	 * @see org.apache.struts.action.ActionMapping#getPath
	 * @see org.apache.struts.config.ModuleConfig#getPrefix
	 */
	protected String determineActionBeanName(ActionMapping mapping) {
		String prefix = mapping.getModuleConfig().getPrefix();
		String path = mapping.getPath();
		String beanName = (prefix != null && prefix.length() > 0) ? prefix + path : path;
		if (logger.isDebugEnabled()) {
			logger.debug("DelegatingActionProxy with mapping path '" + path + "' and module prefix '" +
			             prefix + "' delegating to Spring bean with name [" + beanName + "]");
		}
		return beanName;
	}

}
