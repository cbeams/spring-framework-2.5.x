package org.springframework.web.servlet.mvc;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerMethod;

/**
 * Abstract base class for {@link HandlerAdapter} implementations that support {@link HandlerMethod} objects.
 * <p/>
 * Contains template methods for these endpoints.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractHandlerMethodAdapter implements HandlerAdapter {

    public final boolean supports(Object handler) {
        return handler instanceof HandlerMethod && supportsInternal(((HandlerMethod) handler).getMethod());
    }

    public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return handlerInternal(request, response, (HandlerMethod) handler);
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }

    /**
     * Given a handler method, return whether or not this adapter can support it.
     *
     * @param method handler method to check
     * @return whether or not this adapter can adapt the given method method
     */
    protected abstract boolean supportsInternal(Method method);

    /**
     * Use the given handler method endpoint to handle the request.
     *
     * @param request       current HTTP request
     * @param response      current HTTP response
     * @param handlerMethod handler method to invoke
     * @return ModelAndView object with the name of the view and the required model data, or <code>null</code> if the
     *         request has been handled directly
     * @throws Exception in case of errors
     */
    protected abstract ModelAndView handlerInternal(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    HandlerMethod handlerMethod) throws Exception;

}
