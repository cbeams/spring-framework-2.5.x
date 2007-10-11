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

package org.springframework.web.servlet.mvc.annotation;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerMapping}
 * interface that maps handlers based on HTTP paths expressed through the
 * {@link RequestMapping} annotation at the type or method level. Registered by
 * default in {@link org.springframework.web.servlet.DispatcherServlet} on Java 5+.
 *
 * <p>Annotated controllers are usually marked with the {@link Controller} stereotype
 * at the type level. This is not strictly necessary when {@link RequestMapping} is
 * applied at the type level (since such a handler usually implements the
 * {@link org.springframework.web.servlet.mvc.Controller} interface). However,
 * {@link Controller} is required for detecting {@link RequestMapping} annotations
 * at the method level.
 *
 * <p><b>NOTE:</b> Method-level mappings are only allowed to narrow the mapping
 * expressed at the class level (if any). HTTP paths need to uniquely map onto
 * specific handler beans, with any given HTTP path only allowed to be mapped
 * onto one specific handler bean (not spread across multiple handler beans).
 * It is strongly recommended to co-locate related handler methods into the same bean.
 * 
 * <p>The {@link AnnotationMethodHandlerAdapter} is responsible for processing
 * annotated handler methods, as mapped by this HandlerMapping. For
 * {@link RequestMapping} at the type level, specific HandlerAdapters such as
 * {@link org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter} apply.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see RequestMapping
 * @see AnnotationMethodHandlerAdapter
 */
public class DefaultAnnotationHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	/**
	 * Checks for presence of the {@link org.springframework.web.bind.annotation.RequestMapping} annotation on the handler class
	 * and on any of its methods.
	 */
	protected String[] determineUrlsForHandler(String beanName) {
		Class<?> handlerType = getApplicationContext().getType(beanName);
		RequestMapping mapping = handlerType.getAnnotation(RequestMapping.class);
		if (mapping != null) {
			if (!mapping.type().equals("") || mapping.params().length > 0) {
				throw new IllegalStateException("Only path value supported for RequestMapping annotation " +
						"at the type level - map HTTP method and/or parameters at the method level");
			}
			return mapping.value();
		}
		else if (handlerType.isAnnotationPresent(Controller.class)) {
			final Set urls = new LinkedHashSet();
			ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					RequestMapping mapping = method.getAnnotation(RequestMapping.class);
					if (mapping != null) {
						String[] mappedPaths = mapping.value();
						for (int i = 0; i < mappedPaths.length; i++) {
							urls.add(mappedPaths[i]);
						}
					}
				}
			});
			return StringUtils.toStringArray(urls);
		}
		else {
			return new String[0];
		}
	}

}
