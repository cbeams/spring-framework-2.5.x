/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.servlet.handler;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Exception resolver that allows for mapping exception class names to view names,
 * either for a list of given handlers or for all handlers in the DispatcherServlet.
 *
 * <p>Error views are analogous to error page JSPs, but can be used with any
 * kind of exception including any checked one, with fine-granular mappings for
 * specific handlers.
 *
 * @author Juergen Hoeller
 * @since 22.11.2003
 */
public class SimpleMappingExceptionResolver implements HandlerExceptionResolver, Ordered {

	/**
	 * The default name of the exception attribute: "exception".
	 */
	public static final String DEFAULT_EXCEPTION_ATTRIBUTE = "exception";
	

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Properties exceptionMappings;

	private String defaultErrorView;

	private Set mappedHandlers;

	private Integer defaultStatusCode;

	private String exceptionAttribute = DEFAULT_EXCEPTION_ATTRIBUTE;


	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Set the mappings between exception class names and error view names.
	 * The exception class name can be a substring, with no wildcard support
	 * at present. A value of "ServletException" would match
	 * <code>javax.servlet.ServletException</code> and subclasses, for example.
	 * <p><b>NB:</b> Consider carefully how specific the pattern is, and whether
	 * to include package information (which isn't mandatory). For example,
	 * "Exception" will match nearly anything, and will probably hide other rules.
	 * "java.lang.Exception" would be correct if "Exception" was meant to define
	 * a rule for all checked exceptions. With more unusual exception names such
	 * as "BaseBusinessException" there's no need to use a FQN.
	 * <p>Follows the same matching algorithm as RuleBasedTransactionAttribute
	 * and RollbackRuleAttribute.
	 * @param mappings exception patterns (can also be fully qualified class names)
	 * as keys, and error view names as values
	 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute
	 */
	public void setExceptionMappings(Properties mappings) {
		this.exceptionMappings = mappings;
	}

	/**
	 * Set the name of the default error view.
	 * This view will be returned if no specific mapping was found.
	 * <p>Default is none.
	 */
	public void setDefaultErrorView(String defaultErrorView) {
		this.defaultErrorView = defaultErrorView;
		if (logger.isInfoEnabled()) {
			logger.info("Default error view is '" + this.defaultErrorView + "'");
		}
	}

	/**
	 * Specify the set of handlers that this exception resolver should map.
	 * The exception mappings and the default error view will only apply
	 * to the specified handlers.
	 * <p>If no handlers set, both the exception mappings and the default error
	 * view will apply to all handlers. This means that a specified default
	 * error view will be used as fallback for all exceptions; any further
	 * HandlerExceptionResolvers in the chain will be ignored in this case.
	 */
	public void setMappedHandlers(Set mappedHandlers) {
		this.mappedHandlers = mappedHandlers;
	}

	/**
	 * Set the default HTTP status code that this exception resolver will apply
	 * if it resolves an error view.
	 * <p>Note that this error code will only get applied in case of a top-level
	 * request. It will not be set for an include request, since the HTTP status
	 * cannot be modified from within an include.
	 * <p>If not specified, no status code will be applied, either leaving this to
	 * the controller or view, or keeping the servlet engine's default of 200 (OK).
	 * @param defaultStatusCode HTTP status code value, for example
	 * 500 (SC_INTERNAL_SERVER_ERROR) or 404 (SC_NOT_FOUND)
	 * @see javax.servlet.http.HttpServletResponse#SC_INTERNAL_SERVER_ERROR
	 * @see javax.servlet.http.HttpServletResponse#SC_NOT_FOUND
	 */
	public void setDefaultStatusCode(int defaultStatusCode) {
		this.defaultStatusCode = new Integer(defaultStatusCode);
	}

	/**
	 * Set the name of the model attribute as which the exception should
	 * be exposed. Default is "exception".
	 * <p>This can be either set to a different attribute name or to
	 * <code>null</code> for not exposing an exception attribute at all.
	 * @see #DEFAULT_EXCEPTION_ATTRIBUTE
	 */
	public void setExceptionAttribute(String exceptionAttribute) {
		this.exceptionAttribute = exceptionAttribute;
	}


	public ModelAndView resolveException(
	    HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

		// Check whether we're supposed to apply to the given handler.
		if (this.mappedHandlers != null && !this.mappedHandlers.contains(handler)) {
			return null;
		}

		String viewName = null;

		// Check for specific exception mappings.
		if (this.exceptionMappings != null) {
			viewName = findMatchingViewName(this.exceptionMappings, ex);
		}

		// Return default error view else, if defined.
		if (viewName == null && this.defaultErrorView != null) {
			viewName = this.defaultErrorView;
		}

		if (viewName != null) {
			// Apply HTTP status code for error views, if specified.
			// Only apply it if we're processing a top-level request.
			if (this.defaultStatusCode != null && !WebUtils.isIncludeRequest(request)) {
				response.setStatus(this.defaultStatusCode.intValue());
			}
			return getModelAndView(viewName, ex, request);
		}
		else {
			return null;
		}
	}


	/**
	 * Find a matching view name in the given exception mappings
	 * @param exceptionMappings mappings between exception class names and error view names
	 * @param ex the exception that got thrown during handler execution
	 * @return the view name, or <code>null</code> if none found
	 * @see #setExceptionMappings
	 */
	protected String findMatchingViewName(Properties exceptionMappings, Exception ex) {
		String viewName = null;
		int deepest = Integer.MAX_VALUE;
		for (Enumeration names = exceptionMappings.propertyNames(); names.hasMoreElements();) {
			String exceptionMapping = (String) names.nextElement();
			int depth = getDepth(exceptionMapping, ex);
			if (depth >= 0 && depth < deepest) {
				deepest = depth;
				viewName = exceptionMappings.getProperty(exceptionMapping);
			}
		}
		return viewName;
	}

	/**
	 * Return the depth to the superclass matching.
	 * 0 means ex matches exactly. Returns -1 if there's no match.
	 * Otherwise, returns depth. Lowest depth wins.
	 * <p>Follows the same algorithm as RollbackRuleAttribute.
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute
	 */
	protected int getDepth(String exceptionMapping, Exception ex) {
		return getDepth(exceptionMapping, ex.getClass(), 0);
	}

	private int getDepth(String exceptionMapping, Class exceptionClass, int depth) {
		if (exceptionClass.getName().indexOf(exceptionMapping) != -1) {
			// Found it!
			return depth;
		}
		// If we've gone as far as we can go and haven't found it...
		if (exceptionClass.equals(Throwable.class)) {
			return -1;
		}
		return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
	}


	/**
	 * Return a ModelAndView for the given request, view name and exception.
	 * Default implementation delegates to <code>getModelAndView(viewName, ex)</code>.
	 * @param viewName the name of the error view
	 * @param ex the exception that got thrown during handler execution
	 * @param request current HTTP request (useful for obtaining metadata)
	 * @return the ModelAndView instance
	 * @see #getModelAndView(String, Exception)
	 */
	protected ModelAndView getModelAndView(String viewName, Exception ex, HttpServletRequest request) {
		return getModelAndView(viewName, ex);
	}

	/**
	 * Return a ModelAndView for the given view name and exception.
	 * Default implementation adds the specified exception attribute.
	 * Can be overridden in subclasses.
	 * @param viewName the name of the error view
	 * @param ex the exception that got thrown during handler execution
	 * @return the ModelAndView instance
	 * @see #setExceptionAttribute
	 */
	protected ModelAndView getModelAndView(String viewName, Exception ex) {
		ModelAndView mv = new ModelAndView(viewName);
		if (this.exceptionAttribute != null) {
			mv.addObject(this.exceptionAttribute, ex);
		}
		return mv;
	}

}
