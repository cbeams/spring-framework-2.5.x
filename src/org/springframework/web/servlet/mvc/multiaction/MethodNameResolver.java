/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface that parameterizes the MultiActionController class
 * using the <b>Strategy</b> GoF Design pattern, allowing
 * the mapping from incoming request to handler method name
 * to be varied without affecting other application code.
 * <br>Illustrates how delegation can be more flexible than
 * subclassing.
 * @author Rod Johnson
 */
public interface MethodNameResolver {
	
	/**
	 * Return a method name that can handle this request. Such
	 * mappings are typically, but not necessarily, based on URL.
	 * @return a method name that can handle this request.
	 * Never returns null; throws exception
	 * @throws NoSuchRequestHandlingMethodException if no method
	 * can be found for this URL
	 */
	String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException;
}