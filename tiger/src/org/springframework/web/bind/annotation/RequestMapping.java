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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping web requests onto specific handler classes and/or
 * handler methods. Provides consistent style between Servlet and Portlet
 * environments, with the semantics adapting to the concrete environment.
 *
 * <p><b>NOTE:</b> Method-level mappings are only allowed to narrow the mapping
 * expressed at the class level (if any). HTTP paths / portlet modes need to
 * uniquely map onto specific handler beans, with any given path / mode only
 * allowed to be mapped onto one specific handler bean (not spread across
 * multiple handler beans). It is strongly recommended to co-locate related
 * handler methods into the same bean.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping
 * @see org.springframework.web.portlet.mvc.annotation.DefaultAnnotationHandlerMapping
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

	/**
	 * The primary mapping expressed by this annotation.
	 * <p>In a Servlet environment: the path mapping URLs (e.g. "/myPath.do")
	 * <p>In a Portlet environment: the mapped portlet modes (e.g. "EDIT")
	 */
	String[] value() default {};

	/**
	 * The type of the mapped request, narrowing the primary mapping.
	 * By default, the mapping will apply to any type of request.
	 * <p>In a Servlet environment: the HTTP method to apply to (e.g. "POST")
	 * <p>In a Portlet environment: the action/render phase (e.g. "ACTION")
	 * The phase will be inferred from the method signature by default
	 * (presence of <code>ActionRequest</code> / <code>RenderRequest</code>
	 * argument, or a <code>String</code> / <code>Map</code> /
	 * <code>ModelAndView</code> return value which suggests a render method).
	 */
	String type() default "";

	/**
	 * The parameters of the mapped request, narrowing the primary mapping.
	 * <p>Same format for any environment: a sequence of "myParam=myValue" style
	 * expressions, with a request only mapped if each such parameter is found
	 * to have the given value. "myParam" style expressions are also supported,
	 * with such parameters having to be present in the request (allowed to
	 * have any value).
	 */
	String[] params() default {};

}
