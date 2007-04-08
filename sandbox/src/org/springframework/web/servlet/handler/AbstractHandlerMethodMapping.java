package org.springframework.web.servlet.handler;

import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.JdkVersion;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerAdapter;

/**
 * Abstract base class for {@link HandlerAdapter} implementations that support {@link HandlerMethod} objects. Contains
 * template methods for handling these method.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractHandlerMethodMapping extends AbstractUrlHandlerMapping {

    /**
     * Registers the methods of the given bean. This method iterates over the methods of the bean, and calls {@link
     * #getUrlPaths(java.lang.reflect.Method)} for each. The method is registered for each returned URL.
     *
     * @see #getUrlPaths(java.lang.reflect.Method)
     */
    protected void registerHandlerMethods(Object handler) {
        Method[] methods = getHandlerClass(handler).getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (JdkVersion.isAtLeastJava15() && methods[i].isSynthetic() ||
                    methods[i].getDeclaringClass().equals(Object.class)) {
                continue;
            }
            String[] urlPaths = getUrlPaths(methods[i]);
            if (!ObjectUtils.isEmpty(urlPaths)) {
                HandlerMethod handlerMethod = new HandlerMethod(handler, methods[i]);
                for (int j = 0; j < urlPaths.length; j++) {
                    registerHandler(urlPaths[j], handlerMethod);
                }
            }
        }
    }

    /**
     * Return the class or interface to use for method reflection.
     * <p/>
     * Default implementation returns the target class for a CGLIB proxy, and the class of the given bean else (for a
     * JDK proxy or a plain bean class).
     *
     * @param endpoint the bean instance (might be an AOP proxy)
     * @return the bean class to expose
     */
    protected Class getHandlerClass(Object endpoint) {
        if (AopUtils.isCglibProxy(endpoint)) {
            return endpoint.getClass().getSuperclass();
        }
        return endpoint.getClass();
    }

    /**
     * Returns the the url paths for the given method.
     */
    protected abstract String[] getUrlPaths(Method method);
}
