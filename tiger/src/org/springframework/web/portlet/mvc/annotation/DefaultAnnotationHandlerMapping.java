/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.portlet.mvc.annotation;

import java.lang.reflect.Method;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

import org.springframework.beans.BeansException;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.handler.AbstractMapBasedHandlerMapping;

/**
 * Implementation of the {@link org.springframework.web.portlet.HandlerMapping}
 * interface that maps handlers based on portlet modes expressed through the
 * {@link RequestMapping} annotation at the type or method level. Registered by
 * default in {@link org.springframework.web.servlet.DispatcherServlet} on Java 5+.
 *
 * <p>Annotated controllers are usually marked with the {@link Controller} stereotype
 * at the type level. This is not strictly necessary when {@link RequestMapping} is
 * applied at the type level (since such a handler usually implements the
 * {@link org.springframework.web.portlet.mvc.Controller} interface). However,
 * {@link Controller} is required for detecting {@link RequestMapping} annotations
 * at the method level.
 *
 * <p><b>NOTE:</b> Method-level mappings are only allowed to narrow the mapping
 * expressed at the class level (if any). Portlet modes need to uniquely map onto
 * specific handler beans, with any given portlet mode only allowed to be mapped
 * onto one specific handler bean (not spread across multiple handler beans).
 * It is strongly recommended to co-locate related handler methods into the same bean.
 *
 * <p>The {@link AnnotationMethodHandlerAdapter} is responsible for processing
 * annotated handler methods, as mapped by this HandlerMapping. For
 * {@link org.springframework.web.bind.annotation.RequestMapping} at the type level, specific HandlerAdapters such as
 * {@link org.springframework.web.portlet.mvc.SimpleControllerHandlerAdapter} apply.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class DefaultAnnotationHandlerMapping extends AbstractMapBasedHandlerMapping {

	/**
	 * Calls the <code>registerHandlers</code> method in addition
	 * to the superclass's initialization.
	 * @see #detectHandlers
	 */
	public void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		detectHandlers();
	}

	/**
	 * Register all handlers specified in the Portlet mode map for the corresponding modes.
	 * @throws org.springframework.beans.BeansException if the handler couldn't be registered
	 */
	protected void detectHandlers() throws BeansException {
		String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class);
		for (final String beanName : beanNames) {
			Class<?> handlerType = getApplicationContext().getType(beanName);
			RequestMapping mapping = handlerType.getAnnotation(RequestMapping.class);
			if (mapping != null) {
				if (!mapping.type().equals("") || mapping.params().length > 0) {
					throw new IllegalStateException("Only portlet mode value supported for RequestMapping annotation " +
							"at the type level - map action/render type and/or parameters at the method level");
				}
				String[] modeKeys = mapping.value();
				for (String modeKey : modeKeys) {
					registerHandler(new PortletMode(modeKey), beanName);
				}
			}
			else if (handlerType.isAnnotationPresent(Controller.class)) {
				ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
					public void doWith(Method method) {
						RequestMapping mapping = method.getAnnotation(RequestMapping.class);
						if (mapping != null) {
							String[] modeKeys = mapping.value();
							for (String modeKey : modeKeys) {
								registerHandler(new PortletMode(modeKey), beanName);
							}
						}
					}
				});
			}
		}
	}


	/**
	 * Uses the current PortletMode as lookup key.
	 */
	protected Object getLookupKey(PortletRequest request) throws Exception {
		return request.getPortletMode();
	}

}
