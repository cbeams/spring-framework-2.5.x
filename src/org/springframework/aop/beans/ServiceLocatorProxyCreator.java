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

package org.springframework.aop.beans;

import java.lang.reflect.Method;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * 2004-12-30: Work in progress, but I need to check in to check out on another machine.
 * Will be complete (polished) in a day or two.
 * 
 * <p>FactoryBean which, given an interface which must have one or more methods
 * with the signatures:
 * <code>
 * <type> getXXX()
 * <code>
 * or
 * <code>
 * <type> getXXX(String id)
 * <code>
 * creates a proxy which implements that interface.</p>
 * On invocation of one of these factory methods on the proxy, the proxy returns
 * the result of a beanfactory.getBean(id) call. If id is null, then if one bean
 * in the factory matches the return type of the 'get' method, that is returned.
 * 
 * If an id is specified, and no bean matches the specified id, or the id is
 * null and there is not exactly one bean matching the return type of the factory
 * interface 'get' method being called, a NoSuchBeanDefinitionException is thrown.
 * 
 * @author Colin Sampaleanu
 */

public class ServiceLocatorProxyCreator implements FactoryBean, InitializingBean,
        BeanFactoryAware {

    private Class serviceLocatorInterface;

    private ListableBeanFactory beanFactory;

    private Object proxy;

    public void setServiceLocatorInterface(Class interfaceName) {
        this.serviceLocatorInterface = interfaceName;
    }

    // javadoc in superclass!
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ListableBeanFactory))
            throw new FatalBeanException(
                    "This class must be used in BeanFactory that is at least a ListableBeanFactory");
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    // javadoc in superclass!
    public void afterPropertiesSet() throws BeansException {
        if (serviceLocatorInterface == null)
            throw new FatalBeanException("'factoryInterface' not set");

        // create proxy which will delegate getXXX(String id) method to
        // getBean() call

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addInterface(serviceLocatorInterface);
        // add a target which will be called for base Object methods
        // (toString(), etc.)
        proxyFactory.setTarget(new TypedFactoryTarget());

        proxyFactory.addAdvice(new MethodInterceptor() {
            public Object invoke(MethodInvocation mi) throws Throwable {

                Method method = mi.getMethod();
                Class[] paramTypes = method.getParameterTypes();

                try {
                    Method interfaceMethod = serviceLocatorInterface.getMethod(method
                            .getName(), paramTypes);

                    if (!interfaceMethod.getName().startsWith("get")
                            || (paramTypes.length != 1 && !String.class.equals(paramTypes[0]))) {
                        throw new UnsupportedOperationException(
                                "May only call methods with signature '<type> getXXXXXX(String id)' on factory interface, but tried to call: "
                                        + interfaceMethod);
                    }
                    Class factoryReturnType = interfaceMethod.getReturnType();

                    Object[] args = mi.getArguments();
                    if (args.length != 1 && !String.class.equals(args[0])) {
                        // should actually be impossible to get here
                        throw new IllegalArgumentException(
                                "Expected one argument of type String");
                    }
                    String beanName = (String) args[0];

                    if (beanName != null) {
                        return beanFactory.getBean(beanName);
                    }

                    Map beans = beanFactory.getBeansOfType(factoryReturnType);
                    if (beans.size() != 1) {
                        throw new NoSuchBeanDefinitionException(
                                factoryReturnType,
                                "No bean named '"
                                        + beanName
                                        + "' found in factory, and there wasn't exactly one instance of a bean of type '"
                                        + factoryReturnType.getClass() + "'");
                    }
                    return beans.values().iterator().next();
                }
                catch (NoSuchMethodException e) {
                    // non factory interface method call (toString(), equals,
                    // etc.)
                    return mi.proceed();
                }
            }
        });
        proxy = proxyFactory.getProxy();
    }

    // javadoc in superclass!
    public Object getObject() throws Exception {
        return proxy;
    }

    // javadoc in superclass!
    public Class getObjectType() {
        return serviceLocatorInterface;
    }

    // javadoc in superclass!
    public boolean isSingleton() {
        return false;
    }

    // just a type to use as the target for invocations which are not against
    // factory interface
    class TypedFactoryTarget {
    }
}
