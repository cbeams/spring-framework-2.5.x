package org.springframework.web.struts;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;

import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * BeanPostProcessor implementation that passes the ActionServlet
 * to beans that extend the Struts Action class. Invokes
 * <code>Action.setServlet</code> with null on bean destruction,
 * providing the same lifecycle handling as Struts' ActionServlet.
 *
 * <p>ContextLoaderPlugIn automatically registers this processor
 * with the underlying bean factory of its WebApplicationContext.
 * Applications do not use this class directly.
 *
 * @author Juergen Hoeller
 * @since 05.04.2004
 * @see ContextLoaderPlugIn
 */
public class ActionServletAwareProcessor implements DestructionAwareBeanPostProcessor {

	private final ActionServlet actionServlet;

	/**
	 * Create a new ActionServletAwareProcessor for the given servlet.
	 */
	public ActionServletAwareProcessor(ActionServlet actionServlet) {
		this.actionServlet = actionServlet;
	}

	public Object postProcessBeforeInitialization(Object bean, String name) {
		if (bean instanceof Action) {
			((Action) bean).setServlet(this.actionServlet);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String name) {
		return bean;
	}

	public void postProcessBeforeDestruction(Object bean, String name) {
		if (bean instanceof Action) {
			((Action) bean).setServlet(null);
		}
	}

}
