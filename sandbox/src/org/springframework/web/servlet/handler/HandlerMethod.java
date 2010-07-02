package org.springframework.web.servlet.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.util.Assert;

/**
 * Represents a bean method that will be invoked as part of an incoming Web request.
 * <p/>
 * Consists of a {@link java.lang.reflect.Method}, and a bean {@link Object}.
 *
 * @author Arjen Poutsma
 */
public final class HandlerMethod {

    private Object bean;

    private Method method;

    /**
     * Constructs a new handler method with the given bean and method.
     *
     * @param bean   the object bean
     * @param method the method
     */
    public HandlerMethod(Object bean, Method method) {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(method, "method must not be null");
        this.bean = bean;
        this.method = method;
    }

    /**
     * Constructs a new handler method with the given bean, method name and parameters.
     *
     * @param bean           the object bean
     * @param methodName     the method name
     * @param parameterTypes the method parameter types
     */
    public HandlerMethod(Object bean, String methodName, Class[] parameterTypes) throws NoSuchMethodException {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(methodName, "method must not be null");
        this.bean = bean;
        method = bean.getClass().getMethod(methodName, parameterTypes);
    }

    /**
     * Returns the object bean for this handler method.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Returns the method for this handler method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Invokes this handler method with the given arguments.
     *
     * @param args the arguments
     * @return the invocation result
     * @throws IllegalAccessException when there is insufficient access to invoke the method
     * @throws java.lang.reflect.InvocationTargetException
     *                                when the method invocation results in an exception
     */
    public Object invoke(Object[] args) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(bean, args);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof HandlerMethod) {
            HandlerMethod other = (HandlerMethod) o;
            return bean.equals(other.bean) && method.equals(other.method);
        }
        return false;
    }

    public int hashCode() {
        return 31 * bean.hashCode() + method.hashCode();
    }

    public String toString() {
        return method.toString();
    }
}
