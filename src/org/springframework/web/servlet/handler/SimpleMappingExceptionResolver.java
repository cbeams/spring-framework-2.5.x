package org.springframework.web.servlet.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Exception resolver that allows for mapping exception class names to view names,
 * either for a list of given handlers or for all handlers in the DispatcherServlet.
 * @author Juergen Hoeller
 * @since 22.11.2003
 */
public class SimpleMappingExceptionResolver implements HandlerExceptionResolver, Ordered {

	public static final String DEFAULT_EXCEPTION_ATTRIBUTE = "exception";

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Map exceptionMappings;

	private List mappedHandlers;

	private String exceptionAttribute = DEFAULT_EXCEPTION_ATTRIBUTE;

	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	/**
	 * Set the mappings between exception class names and view names.
	 * @param mappings fully qualified exception class names as keys,
	 * and view names as values
	 */
	public void setExceptionMappings(Properties mappings) throws ClassNotFoundException {
		this.exceptionMappings = new HashMap();
		for (Iterator it = mappings.keySet().iterator(); it.hasNext();) {
			String exceptionClassName = (String) it.next();
			String viewName = mappings.getProperty(exceptionClassName);
			Class exceptionClass = Class.forName(exceptionClassName, true, Thread.currentThread().getContextClassLoader());
			this.exceptionMappings.put(exceptionClass, viewName);
		}
	}

	/**
	 * Set the list of handlers that this exception resolver should map.
	 * The exception mappings will only apply to those handlers.
	 * If none set, the exception mappings will apply to all handlers.
	 */
	public void setMappedHandlers(List mappedHandlers) {
		this.mappedHandlers = mappedHandlers;
	}

	/**
	 * Set the name of the model attribute as which the exception should be exposed.
	 * Default is "exception".
	 */
	public void setExceptionAttribute(String exceptionAttribute) {
		this.exceptionAttribute = exceptionAttribute;
	}

	public ModelAndView resolveException(Exception ex, Object handler) {
		if (this.mappedHandlers == null || this.mappedHandlers.contains(handler)) {
			for (Iterator it = this.exceptionMappings.keySet().iterator(); it.hasNext();) {
				Class exceptionClass = (Class) it.next();
				if (exceptionClass.isInstance(ex)) {
					String viewName = (String) this.exceptionMappings.get(exceptionClass);
					return getModelAndView(viewName, ex);
				}
			}
		}
		return null;
	}

	protected ModelAndView getModelAndView(String mappedViewName, Exception ex) {
		ModelAndView mv = new ModelAndView(mappedViewName);
		if (this.exceptionAttribute != null && this.exceptionAttribute.length() > 0) {
			mv.addObject(this.exceptionAttribute, ex);
		}
		return mv;
	}

}
