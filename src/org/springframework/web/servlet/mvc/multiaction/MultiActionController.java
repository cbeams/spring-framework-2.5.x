/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.servlet.mvc.multiaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.support.SessionRequiredException;

/**
 * Controller implementation that allows multiple request types to be
 * handled by the same class. Subclasses of this class can handle several
 * different types of request with methods of the form
 *
 * <pre>
 * ModelAndView actionName(HttpServletRequest request, HttpServletResponse response);</pre>
 *
 * May take a third parameter HttpSession in which an existing session will be required,
 * or a third parameter of an arbitrary class that gets treated as command
 * (i.e. an instance of the class gets created, and request parameters get bound to it)
 *
 * <p>These methods can throw any kind of exception, but should only let propagate
 * those that they consider fatal, or which their class or superclass is prepared to
 * catch by implementing an exception handler.
 *
 * <p>This model allows for rapid coding, but loses the advantage of compile-time
 * checking. It is similar to a Struts 1.1 DispatchAction, but more sophisticated.
 * Also supports delegation to another object.
 *
 * <p>An implementation of the MethodNameResolver interface defined in this package
 * should return a method name for a given request, based on any aspect of the request,
 * such as its URL or an "action" parameter. The actual strategy can be configured
 * via the "methodNameResolver" bean property, for each MultiActionController.
 *
 * <p>The default MethodNameResolver is InternalPathMethodNameResolver; further included
 * strategies are PropertiesMethodNameResolver and ParameterMethodNameResolver.
 *
 * <p>Subclasses can implement custom exception handler methods with names such as:
 *
 * <pre>
 * ModelAndView anyMeaningfulName(HttpServletRequest request, HttpServletResponse response, ExceptionClass exception);</pre>
 *
 * The third parameter can be any subclass or Exception or RuntimeException.
 *
 * <p>There can also be an optional lastModified method for handlers, of signature:
 *
 * <pre>
 * long anyMeaningfulNameLastModified(HttpServletRequest request)</pre>
 *
 * If such a method is present, it will be invoked. Default return from getLastModified
 * is -1, meaning that the content must always be regenerated.
 *
 * <p>Note that method overloading isn't allowed.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see MethodNameResolver
 * @see InternalPathMethodNameResolver
 * @see PropertiesMethodNameResolver
 * @see ParameterMethodNameResolver
 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
 */
public class MultiActionController extends AbstractController implements LastModified  {
		
	/** Suffix for last-modified methods */
	public static final String LAST_MODIFIED_METHOD_SUFFIX = "LastModified";
	
	/**
	 * Log category to use when no mapped handler is found for a request.
	 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	/**
	 * Additional logger to use when no mapped handler is found for a request.
	 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/**
	 * Helper object that knows how to return method names from incoming requests.
	 * Can be overridden via the methodNameResolver bean property
	 */
	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

	/** Object we'll invoke methods on. Defaults to this. */
	private Object delegate;

	/** Methods, keyed by name */
	private Map methodHash;
	
	/** LastModified methods, keyed by handler method name (without LAST_MODIFIED_SUFFIX) */
	private Map lastModifiedMethodHash;
	
	/** Methods, keyed by exception class */
	private Map exceptionHandlerHash;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Constructor for MultiActionController that looks for handler methods
	 * in the present subclass.Caches methods for quick invocation later.
	 * This class's use of reflection will impose little overhead at runtime.
	 * @throws ApplicationContextException if the class doesn't contain any
	 * action handler methods (and so could never handle any requests).
	 */
	public MultiActionController() throws ApplicationContextException {
		setDelegate(this);
	}
	
	/**
	 * Constructor for MultiActionController that looks for handler methods in delegate,
	 * rather than a subclass of this class. Caches methods.
	 * @param delegate handler class. This doesn't need to implement any particular
	 * interface, as everything is done using reflection.
	 * @throws ApplicationContextException if the class doesn't contain any handler methods
	 */
	public MultiActionController(Object delegate) throws ApplicationContextException {
		setDelegate(delegate);
	}
	
	
	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------

	/**
	 * Set the method name resolver that this class should use.
	 * Allows parameterization of handler method mappings.
	 */
	public final void setMethodNameResolver(MethodNameResolver methodNameResolver) {
		this.methodNameResolver = methodNameResolver;
	}
	
	/**
	 * Get the MethodNameResolver used by this class.
	 */
	public final MethodNameResolver getMethodNameResolver() {
		return this.methodNameResolver;
	}
	
	/**
	 * Set the delegate used by this class. The default is
	 * "this", assuming that handler methods have been added
	 * by a subclass. This method is rarely invoked once
	 * the class is configured.
	 * @param delegate class containing methods, which may
	 * be the present class, the handler methods being in a subclass
	 * @throws ApplicationContextException if there aren't
	 * any valid request handling methods in the subclass.
	 */
	public final void setDelegate(Object delegate) throws ApplicationContextException {
		if (delegate == null) {
			throw new IllegalArgumentException("delegate cannot be null in MultiActionController");
		}
		this.delegate = delegate;
		this.methodHash = new HashMap();
		this.lastModifiedMethodHash = new HashMap();
		
		// Look at all methods in the subclass, trying to find
		// methods that are validators according to our criteria
		Method[] methods = delegate.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			// we're looking for methods with given parameters
			if (methods[i].getReturnType().equals(ModelAndView.class)) {
				// we have a potential handler method, with the correct return type
				Class[] params = methods[i].getParameterTypes();
				
				// Check that the number and types of methods is correct.
				// We don't care about the declared exceptions
				if (params.length >= 2 && params[0].equals(HttpServletRequest.class) &&
						params[1].equals(HttpServletResponse.class)) {
					// we're in business
					if (logger.isInfoEnabled()) {
						logger.info("Found action method [" + methods[i] + "]");
					}
					this.methodHash.put(methods[i].getName(), methods[i]);
					
					// look for corresponding LastModified method
					try {
						Method lastModifiedMethod = delegate.getClass().getMethod(methods[i].getName() + LAST_MODIFIED_METHOD_SUFFIX,
																																			new Class[] { HttpServletRequest.class } );
						// put in cache, keyed by handler method name
						this.lastModifiedMethodHash.put(methods[i].getName(), lastModifiedMethod);
						if (logger.isInfoEnabled()) {
							logger.info("Found last modified method for action method [" + methods[i] + "]");
						}
					}
					catch (NoSuchMethodException ex) {
						// No last modified method. That's ok.
					}
				}
			}
		}
		
		// There must be SOME handler methods.
		// WHAT IF SETTING DELEGATE LATER!?
		if (this.methodHash.isEmpty()) {
			throw new ApplicationContextException("No handler methods in class [" + getClass().getName() + "]");
		}
		
		// now look for exception handlers
		this.exceptionHandlerHash = new HashMap();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getReturnType().equals(ModelAndView.class) &&
					methods[i].getParameterTypes().length == 3) {
				Class[] params = methods[i].getParameterTypes();
				if (params[0].equals(HttpServletRequest.class) && 
					params[1].equals(HttpServletResponse.class) &&
					Throwable.class.isAssignableFrom(params[2])
				) {
					// Have an exception handler
					this.exceptionHandlerHash.put(params[2], methods[i]);
					if (logger.isInfoEnabled()) {
						logger.info("Found exception handler method [" + methods[i] + "]");
					}
				}
			}
		}
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of LastModified
	//---------------------------------------------------------------------

	/**
	 * Try to find an XXXXLastModified method, where XXXX is the name of a handler.
	 * Return -1, indicating that content must be updated, if there's no such handler.
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified(HttpServletRequest)
	 */
	public final long getLastModified(HttpServletRequest request) {
		try {
			String handlerMethodName = this.methodNameResolver.getHandlerMethodName(request);
			Method lastModifiedMethod = (Method) this.lastModifiedMethodHash.get(handlerMethodName);
			if (lastModifiedMethod != null) {
				try {
					// invoke the last-modified method
					Long wrappedLong = (Long) lastModifiedMethod.invoke(this.delegate, new Object[] { request });
					return wrappedLong.longValue();
				}
				catch (Exception ex) {
					// We encountered an error invoking the last-modified method.
					// We can't do anything useful except log this, as we can't throw an exception.
					logger.error("Failed to invoke lastModified method", ex);
				}
			}	// if we had a lastModified method for this request
		}
		catch (NoSuchRequestHandlingMethodException ex) {
			// No handler method for this request. This shouldn't happen, as this
			// method shouldn't be called unless a previous invocation of this class
			// has generated content. Do nothing, that's OK: We'll return default.
		}
		// the default if we didn't find a method
		return -1L;
	}


	//---------------------------------------------------------------------
	// Implementation of Controller
	//---------------------------------------------------------------------

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		try {
			String methodName = this.methodNameResolver.getHandlerMethodName(request);
			return invokeNamedMethod(methodName, request, response);
		}
		catch (NoSuchRequestHandlingMethodException ex) {
			pageNotFoundLogger.warn(ex.getMessage());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Invoke the named method.
	 * Use a custom exception handler if possible;
	 * otherwise, throw an unchecked exception;
	 * wrap a checked exception or Throwable
	 */
	protected final ModelAndView invokeNamedMethod(String method, HttpServletRequest request,
	                                               HttpServletResponse response) throws Exception {
		
		Method m = (Method) this.methodHash.get(method);
		if (m == null) {
			throw new NoSuchRequestHandlingMethodException(method, getClass());
		}

		try {
			// A real generic Collection! Parameters to method.
			List params = new ArrayList(4);
			params.add(request);
			params.add(response);
				
			if (m.getParameterTypes().length >= 3 && m.getParameterTypes()[2].equals(HttpSession.class) ){
				HttpSession session = request.getSession(false);
				if (session == null) {
					return handleException(
							request, response,
							new SessionRequiredException("Pre-existing session required for handler method '" + method + "'"));
				}
				params.add(session);
			}
			
			// If last parameter isn't of HttpSession type, it's a command.
			if (m.getParameterTypes().length >= 3 &&
					!m.getParameterTypes()[m.getParameterTypes().length - 1].equals(HttpSession.class)) {
				Object command = newCommandObject(m.getParameterTypes()[m.getParameterTypes().length - 1]);
				params.add(command);
				bind(request, command);
			}
			
			return (ModelAndView) m.invoke(this.delegate, params.toArray(new Object[params.size()]));
		}
		catch (InvocationTargetException ex) {
			// This is what we're looking for: the handler method threw an exception
			Throwable t = ex.getTargetException();
			return handleException(request, response, t);
		}
	}

	/**
	 * We've encountered an exception which may be recoverable
	 * (InvocationTargetException or SessionRequiredException).
	 * Allow the subclass a chance to handle it.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param t the exception that got thrown
	 * @return a ModelAndView to render the response
	 */
	private ModelAndView handleException(HttpServletRequest request, HttpServletResponse response, Throwable t)
			throws Exception {
		Method handler = getExceptionHandler(t);
		if (handler != null) {
			return invokeExceptionHandler(handler, request, response, t);
		}
		// If we get here, there was no custom handler
		if (t instanceof Exception) {
			throw (Exception) t;
		}
		if (t instanceof Error) {
			throw (Error) t;
		}
		// Shouldn't happen
		throw new ServletException("Unknown Throwable type encountered: " + t);
	}

	/**
	 * Create a new command object of the given class.
	 * Subclasses can override this implementation if they want.
	 * This implementation uses class.newInstance(), so commands need to have
	 * public no arg constructors.
	 */
	protected Object newCommandObject(Class clazz) throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Must create new command of class [" + clazz.getName() + "]");
		}
		try {
			return clazz.newInstance();
		}
		catch (Exception ex) {
			throw new ServletException("Cannot instantiate command of class [" + clazz.getName() +
			                           "]: Does it have a public no arg constructor?", ex);
		}
	}
	
	/**
	 * Bind request parameters onto the given command bean
	 * @param request request from which parameters will be bound
	 * @param command command object, that must be a JavaBean
	 */
	protected void bind(ServletRequest request, Object command) throws ServletException {
		logger.debug("Binding request parameters onto command");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(command, "command");
		binder.bind(request);
		binder.closeNoCatch();
	}
	
	/**
	 * Can return null if not found.
	 * @return a handler for the given exception type
	 * @param exception Won't be a ServletException or IOException
	 */
	protected Method getExceptionHandler(Throwable exception) {
		Class exceptionClass = exception.getClass();
		if (logger.isInfoEnabled()) {
			logger.info("Trying to find handler for exception class [" + exceptionClass.getName() + "]");
		}
		Method handler = (Method) this.exceptionHandlerHash.get(exceptionClass);
		while (handler == null && !exceptionClass.equals(Throwable.class)) {
			if (logger.isInfoEnabled()) {
				logger.info("Trying to find handler for exception superclass [" + exceptionClass.getName() + "]");
			}
			exceptionClass = exceptionClass.getSuperclass();
			handler = (Method) this.exceptionHandlerHash.get(exceptionClass);
		}
		return handler;
	}

	/**
	 * Invoke the selected exception handler.
	 * @param handler handler method to invoke
	 */
	private ModelAndView invokeExceptionHandler(Method handler, HttpServletRequest request,
																							HttpServletResponse response, Throwable ex) throws Exception {
		if (handler == null) {
			throw new ServletException("No handler for exception", ex);
		}

		// If we get here, we have a handler.
		if (logger.isInfoEnabled()) {
			logger.info("Invoking exception handler [" + handler + "] for exception [" + ex + "]");
		}
		try {
			ModelAndView mv = (ModelAndView) handler.invoke(this.delegate, new Object[] {request, response, ex});
			return mv;
		}
		catch (InvocationTargetException ex2) {
			Throwable targetEx = ex2.getTargetException();
			if (targetEx instanceof Exception) {
				throw (Exception) targetEx;
			}
			if (targetEx instanceof Error) {
				throw (Error) targetEx;
			}
			// shouldn't happen
			throw new ServletException("Unknown Throwable type encountered", targetEx);
		}
	}
	
}
