package org.springframework.web.servlet;

/**
 * Interface to be implemented by objects than can resolve exceptions thrown
 * by handlers to error views. Implementors are typically registered as beans
 * in the application context.
 *
 * <p>Error views are analogous to the error page JSPs, but can be used with
 * any kind of exception including any checked exception, with potentially
 * fine-granular mappings for specific handlers.
 *
 * @author Juergen Hoeller
 * @since 22.11.2003
 */
public interface HandlerExceptionResolver {

	/**
	 * Try to resolve the given exception that got thrown during on handler execution,
	 * returning a ModelAndView that represents a specific error page if appropriate.
	 * @param ex the exception that got thrown during handler execution
	 * @param handler the executed handler
	 * @return a matching ModelAndView to forward to, or null for default processing
	 */
	ModelAndView resolveException(Exception ex, Object handler);

}
