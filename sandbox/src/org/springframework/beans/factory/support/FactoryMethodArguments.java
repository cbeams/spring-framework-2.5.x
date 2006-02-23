package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: stevend
 * Date: 23-feb-2006
 * Time: 17:32:16
 * To change this template use File | Settings | File Templates.
 */
public class FactoryMethodArguments {
    private static ThreadLocal argumentListThreadLocal = new ThreadLocal();
    private static ThreadLocal methodInvocationThreadLocal = new ThreadLocal();

    private FactoryMethodArguments() {
        throw new UnsupportedOperationException();
    }

    public static void setMethodThatsBeenInvoked(Method method) {
        methodInvocationThreadLocal.set(method);
    }

    public static void addArgument(Object argument) {
        initializeArgumentListIfNecessary();
        List argumentList = getArgumentList();
        argumentList.add(argument);
    }

    private static List getArgumentList() {
        return (List)argumentListThreadLocal.get();
    }

    private static void initializeArgumentListIfNecessary() {
        if (argumentListThreadLocal.get() == null) {
            argumentListThreadLocal.set(new ArrayList());
        }
    }

    private static Method getMethodThatsBeenInvoked() {
        return (Method)methodInvocationThreadLocal.get();
    }

    public static Object getArgument(int i) {
        if (getArgumentList() == null) {
            throw new IllegalStateException("Make sure the domain class bean definition is configured as a prototype!");
        }
        if (i >= getArgumentList().size() | i < 0) {
            throw new IllegalArgumentException("Argument in position [" + i + "] is not available for method invocation [" + getMethodThatsBeenInvoked() + "]!");
        }
        return getArgumentList().get(i);
    }

    public static void cleanUp() {
        argumentListThreadLocal.remove();
        methodInvocationThreadLocal.remove();
    }

    public static Object getFirstArgument() {
        return getArgument(0);
    }

    public static Object getSecondArgument() {
        return getArgument(1);
    }

    public static Object getThirdArgument() {
        return getArgument(2);
    }

    public static Object getFourthArgument() {
        return getArgument(3);
    }

    public static Object getFifthArgument() {
        return getArgument(4);
    }
}
