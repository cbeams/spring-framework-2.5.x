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

package org.springframework.web.portlet.handler;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Ordered;
import org.springframework.web.portlet.HandlerExceptionResolver;
import org.springframework.web.portlet.ModelAndView;

/**
 * Exception resolver that allows for mapping exception class names to view names,
 * either for a list of given handlers or for all handlers in the DispatcherPortlet.
 *
 * <p>Error views are analogous to error page JSPs, but can be used with any
 * kind of exception including any checked one, with fine-granular mappings for
 * specific handlers.
 *
 * @author Juergen Hoeller
 * @author John Lewis
 * @since 1.3
 */
public class SimpleMappingExceptionResolver implements HandlerExceptionResolver, Ordered {

	public static final String DEFAULT_EXCEPTION_ATTRIBUTE = "exception";

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Properties exceptionMappings;

	private String defaultErrorView;

	private Set mappedHandlers;

	private String exceptionAttribute = DEFAULT_EXCEPTION_ATTRIBUTE;

	private boolean renderWhenMinimized = false;
	

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Set the mappings between exception class names and error view names.
	 * The exception class name can be a substring, with no wildcard support
	 * at present. A value of "PortletException" would match
	 * <code>javax.portet.PortletException</code> and subclasses, for example.
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
	 * Set the name of the model attribute as which the exception should
	 * be exposed. Default is "exception".
	 * @see #DEFAULT_EXCEPTION_ATTRIBUTE
	 */
	public void setExceptionAttribute(String exceptionAttribute) {
		this.exceptionAttribute = exceptionAttribute;
	}

	/**
	 * Set if the resolver should render a view when the portlet is in
	 * a minimized window.  The default is false.
	 * @see javax.portlet.RenderRequest#getWindowState
	 * @see javax.portlet.WindowState#MINIMIZED
	 */
    public void setRenderWhenMinimized(boolean renderWhenMinimized) {
        this.renderWhenMinimized = renderWhenMinimized;
    }

    
	public ModelAndView resolveException(
	    RenderRequest request, RenderResponse response, Object handler, Exception ex) {

		// Check whether we're supposed to apply to the given handler.
		if (this.mappedHandlers != null && !this.mappedHandlers.contains(handler))
			return null;

	    // if the portlet is minimized and we don't want to render then return null
	    if (WindowState.MINIMIZED.equals(request.getWindowState()) &&
	            ! renderWhenMinimized)
	        return null;
	    
		String viewName = null;

		// Check for specific exception mappings.
		if (this.exceptionMappings != null) {
			int deepest = Integer.MAX_VALUE;
			for (Enumeration names = this.exceptionMappings.propertyNames(); names.hasMoreElements();) {
				String exceptionMapping = (String) names.nextElement();
				int depth = getDepth(exceptionMapping, ex);
				if (depth >= 0 && depth < deepest) {
					deepest = depth;
					viewName = this.exceptionMappings.getProperty(exceptionMapping);
				}
			}
		}

		// Return default error view else, if defined.
		if (viewName == null && this.defaultErrorView != null)
			viewName = this.defaultErrorView;

		if (viewName != null)
			return getModelAndView(viewName, request, response, handler, ex);
		else
			return null;
	}

	/**
	 * Return the depth to the superclass matching.
	 * 0 means ex matches exactly. Returns -1 if there's no match.
	 * Otherwise, returns depth. Lowest depth wins.
	 * <p>Follows the same algorithm as RollbackRuleAttribute.
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute
	 */
	public int getDepth(String exceptionMapping, Exception ex) {
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
     * Return a ModelAndView for the given view name and exception. Default
     * implementation adds the specified exception attribute. Can be overridden
     * in subclasses.
     * @param viewName the name of the error view
     * @param request the render request
     * @param response the render response
     * @param handler the handler executed when the exception was thrown
     * @param ex the exception that got thrown during handler execution
     * @return the ModelAndView instance
     * @see #setExceptionAttribute
     */
    protected ModelAndView getModelAndView(String viewName, RenderRequest request,
            RenderResponse response, Object handler, Exception ex) {
        ModelAndView mv = new ModelAndView(viewName);
        if (this.exceptionAttribute != null) {
            mv.addObject(this.exceptionAttribute, ex);
        }
        return mv;
    }

}
