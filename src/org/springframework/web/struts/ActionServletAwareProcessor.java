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

package org.springframework.web.struts;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;

import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * BeanPostProcessor implementation that passes the ActionServlet to beans
 * that extend the Struts Action class. Invokes <code>Action.setServlet</code>
 * with <code>null</dode> on bean destruction, providing the same lifecycle
 * handling as Struts' ActionServlet.
 *
 * <p>ContextLoaderPlugIn automatically registers this processor
 * with the underlying bean factory of its WebApplicationContext.
 * Applications do not need to use this class directly.
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
