package org.springframework.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple implementation of MethodNameResolver that looks for a
 * parameter value containing the name of the method to invoke.
 *
 * <p>The name of the parameter and optionally also the name of a
 * default handler method can be specified as JavaBean properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setParamName
 * @see #setDefaultMethodName
 */
public class ParameterMethodNameResolver implements MethodNameResolver {
	
	private String paramName = "action";

	private String defaultMethodName;

	/**
	 * Set the parameter name we're looking for.
	 * Default is "action".
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Set the name of the default handler method that should be
	 * used when no parameter was found in the request
	 */
	public void setDefaultMethodName(String defaultMethodName) {
		this.defaultMethodName = defaultMethodName;
	}

	public String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
		String methodName = request.getParameter(this.paramName);
		if (methodName == null) {
			methodName = this.defaultMethodName;
		}
		if (methodName == null) {
			throw new NoSuchRequestHandlingMethodException(request);
		}
		return methodName;
	}

}
