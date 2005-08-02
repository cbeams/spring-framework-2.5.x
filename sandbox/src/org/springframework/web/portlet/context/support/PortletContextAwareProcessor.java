/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.portlet.context.support;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.portlet.context.PortletContextAware;

/**
 * BeanPostProcessor implementation that passes the PortletContext to
 * beans that implement the PortletContextAware interface.
 *
 * <p>Portlet application contexts will automatically register this with their
 * underlying bean factory. Applications do not use this directly.
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @see org.springframework.web.portlet.context.PortletContextAware
 * @see org.springframework.web.portlet.context.support.XmlPortletApplicationContext#postProcessBeanFactory
 */
public class PortletContextAwareProcessor implements BeanPostProcessor {

	protected final Log logger = LogFactory.getLog(getClass());

	private final PortletContext portletContext;

	/**
	 * Create a new PortletContextAwareProcessor for the given context.
	 */
	public PortletContextAwareProcessor(PortletContext portletContext) {
		this.portletContext = portletContext;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof PortletContextAware) {
			if (this.portletContext == null) {
				throw new IllegalStateException("Cannot satisfy PortletContextAware for bean '" +
						beanName + "' without PortletContext");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking setPortletContext on PortletContextAware bean '" + beanName + "'");
			}
			((PortletContextAware) bean).setPortletContext(this.portletContext);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

}
