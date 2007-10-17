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
 * <p>Handler methods which are annotated with this annotation are allowed
 * to have very flexible signatures. They may have arguments of the following
 * types, in arbitrary order (except for validation results, which need to
 * follow right after the corresponding command object, if desired):
 * <ul>
 * <li>request / response / session (Servlet API or Portlet API).
 * <li>{@link org.springframework.web.context.request.WebRequest}.
 * <li>{@link java.util.Locale} for the current request locale.
 * <li>{@link java.io.InputStream} / {@link java.io.Reader} for access
 * to the request's content.
 * <li>{@link java.io.OutputStream} / {@link java.io.Writer} for generating
 * the response's content.
 * <li>{@link RequestParam} annotated parameters for access to specific
 * request parameters.
 * <li>{@link java.util.Map} / {@link org.springframework.ui.ModelMap} for
 * enriching the implicit model that will be exposed to the web view.
 * <li>Command objects to bind parameters to (as bean properties or fields, with
 * customizable type conversion, depending on the HandlerAdapter configuration -
 * see the "webBindingInitializer" property on AnnotationMethodHandlerAdapter).
 * Such command objects along with their validation results will be exposed
 * as model attributes, by default using the non-qualified command class name
 * in property notation (e.g. "orderAddress" for type "mypackage.OrderAddress").
 * Specify a parameter-level {@link ModelAttribute} annotation for declaring
 * a specific model attribute name.
 * <li>{@link org.springframework.validation.Errors} /
 * {@link org.springframework.validation.BindingResult} validation results
 * for a preceding command object (the immediate preceding argument).
 * <li>{@link org.springframework.web.bind.support.FormStatus} status handle
 * for marking form processing as complete (triggering the cleanup of session
 * attributes that have been indicated by the {@link FormAttributes} annotation
 * at the handler type level).
 * </ul>
 *
 * <p>The following return types are supported for handler methods:
 * <ul>
 * <li>A <code>ModelAndView</code> object (Servlet MVC or Portlet MVC),
 * with the model implicitly enriched with command objects and the results
 * of {@link ModelAttribute} annotated reference data accessor methods.
 * <li>A {@link java.util.Map} object for exposing a model,
 * with the view name implicitly determined through a
 * {@link org.springframework.web.servlet.RequestToViewNameTranslator}
 * and the model implicitly enriched with command objects and the results
 * of {@link ModelAttribute} annotated reference data accessor methods.
 * <li>A {@link java.lang.String} value which is interpreted as view name,
 * with the model implicitly determined through command objects and
 * {@link ModelAttribute} annotated reference data accessor methods.
 * The handler method may also programmatically enrich the model through
 * declaring a {@link org.springframework.ui.ModelMap} attribute (see above).
 * <li><code>void</code> if the method handles the response itself
 * (e.g. through writing the response content directly).
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 2.5
 * @see RequestParam
 * @see ModelAttribute
 * @see FormAttributes
 * @see org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping
 * @see org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter
 * @see org.springframework.web.portlet.mvc.annotation.DefaultAnnotationHandlerMapping
 * @see org.springframework.web.portlet.mvc.annotation.AnnotationMethodHandlerAdapter
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
