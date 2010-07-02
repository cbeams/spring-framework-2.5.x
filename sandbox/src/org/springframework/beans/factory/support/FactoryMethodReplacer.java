package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: stevend
 * Date: 23-feb-2006
 * Time: 18:04:04
 * To change this template use File | Settings | File Templates.
 */
public class FactoryMethodReplacer implements MethodReplacer {
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        FactoryMethodArguments.cleanUp();
        FactoryMethodArguments.setMethodThatsBeenInvoked(method);
        for (int i = 0; i < args.length; i++) {
            FactoryMethodArguments.addArgument(args[i]);
        }
        try {
            return getBean();
        } finally {
            FactoryMethodArguments.cleanUp();
        }
    }

    public Object getBean() {
        throw new UnsupportedOperationException("The getBean() method on " + getClass().getName() + " is a lookup method. Please configure the <lookup-method/> XML element for this bean definition!");
    }


}
