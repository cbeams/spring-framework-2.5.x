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

package org.springframework.beans.factory;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Rod Johnson
 */
public class Configurer {

    private BeanDefinitionRegistry bdi;
    
    private AbstractBeanFactory abf;

    private int defaultAutowireMode = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;

    public Configurer(BeanDefinitionRegistry bdi) {
        this.bdi = bdi;
        if (bdi instanceof AbstractBeanFactory) {
            abf = (AbstractBeanFactory) bdi;
        }
        else if (bdi instanceof ConfigurableApplicationContext) {
            BeanFactory bf = ((ConfigurableApplicationContext) bdi).getBeanFactory();
            if (bf instanceof AbstractBeanFactory) {
                abf = (AbstractBeanFactory) bf;
            }
        }
    }

    public void setDefaultAutowireMode(int mode) {
        this.defaultAutowireMode = mode;
    }
    
    // TODO resource!?
    public void properties(String location) {
        PropertyResourceConfigurer pc = (PropertyResourceConfigurer) add(PropertyPlaceholderConfigurer.class).
        	prop("location", location);
    }
    
    public void properties(Class clazz, String name) {
        String location = clazz.getName() + "." + name;
        location = StringUtils.replace(location, ".", "/");
        PropertyPlaceholderConfigurer pc = (PropertyPlaceholderConfigurer) add(PropertyPlaceholderConfigurer.class).
        	prop("location", location);
    }

    public int getDefaultAutowireMode() {
        return this.defaultAutowireMode;
    }

    public String addSingleton(Object o) {
        String generatedName = o.getClass().getName();
        addSingleton(generatedName, o);
        return generatedName;
    }

    public void addSingleton(String name, Object o) {
        if (abf != null) {
            abf.registerSingleton(name, o);
        } 
        else {
            throw new UnsupportedOperationException("Can't register singleton");
        }
    }

    // TODO what about adding an object and autowiring it!? But order will
    // matter
    // add and autowire (allows custom create)

    public Definition add(Class clazz) {
        // TODO counts?
        return add(clazz.getName(), clazz);
    }

    public Definition add(String name, Class clazz) {
        DefinitionImpl def = new DefinitionImpl(name, clazz,
                defaultAutowireMode);
        return add(def);
    }

    public Definition add(Definition def) {
        bdi.registerBeanDefinition(def.getBeanName(), def.getBeanDefinition());
        return instantiate(def);
    }

    public AdvisedSupport advise(Definition def) {
        Definition outerDefinition = new DefinitionImpl(def.getBeanName(),
                ProxyFactoryBean.class, AbstractBeanDefinition.AUTOWIRE_NO);

        outerDefinition.prop("target", new BeanDefinitionHolder(def
                .getBeanDefinition(), "(inner bean)"));

        bdi.registerBeanDefinition(outerDefinition.getBeanName(),
                outerDefinition.getBeanDefinition());
        return (AdvisedSupport) instantiate(outerDefinition);
    }

    /**
     * Last definition recordable WAS STATIC
     */
    private Definition instantiate(final Definition def) throws BeansException {
        Object target = org.springframework.beans.BeanUtils
                .instantiateClass(def.getBeanDefinition().getBeanClass());
        ProxyFactory pf = new ProxyFactory(target);
        pf.setProxyTargetClass(true);
        pf.addAdvice(new DelegatingIntroductionInterceptor(def));

        pf.addAdvisor(new DefaultPointcutAdvisor(Pointcuts.SETTERS,
                new RecordingInterceptor(def)));

        // ProxyFactoryBeans get a special interceptor to capture
        // the names of interceptors added by addAdvisor
        pf.addAdvisor(new DefaultPointcutAdvisor(
                new StaticMethodMatcherPointcut() {
                    public boolean matches(Method m, Class targetClass) {
                        return ProxyFactoryBean.class
                                .isAssignableFrom(targetClass)
                                && m.getName().startsWith("add");
                    }
                }, new InterceptorNameCaptureInterceptor(def)));

        // Other methods aren't config methods
        pf.addAdvice(new MethodInterceptor() {
            public Object invoke(MethodInvocation mi) throws Throwable {
                throw new UnsupportedOperationException(mi.getMethod()
                        .getName()
                        + " is not a setter or other config method: "
                        + "disallowing at config time");
            }
        });

        //System.err.println(pf.toProxyConfigString());
        Definition proxy = (Definition) pf.getProxy();
        return proxy;
    }

    /**
     * Interceptor for setter methods that saves the values to the backing
     * BeanDefinition. Supports references to other beans in the factory, as
     * well as simple types.
     */
    private static class RecordingInterceptor implements MethodInterceptor {
        private final Definition def;

        private RecordingInterceptor(Definition def) {
            this.def = def;
        }

        public Object invoke(MethodInvocation mi) throws Throwable {
            String propName = mi.getMethod().getName().substring(3);
            propName = propName.substring(0, 1).toLowerCase()
                    + propName.substring(1);
            Object value = mi.getArguments()[0];
            if (value instanceof Definition) {
                String refName = ((Definition) value).getBeanName();
                System.err.println("Recorded reference to name " + refName);
                value = new RuntimeBeanReference(refName);
            }
            // TODO complex types that are not references!? Should probably
            // allow them
            PropertyValue pv = new PropertyValue(propName, value);
            def.getBeanDefinition().getPropertyValues().addPropertyValue(pv);
            System.out.println("Added " + pv);
            return null;
        }
    }

    /**
     * Applies only to ProxyFactoryBeans. 
     */
    private final class InterceptorNameCaptureInterceptor implements
            MethodInterceptor {
        private List interceptorNames = new LinkedList();

        private final Definition def;

        private InterceptorNameCaptureInterceptor(Definition def) {
            this.def = def;
        }

        public Object invoke(MethodInvocation mi) throws Throwable {
            Object arg = mi.getArguments()[0];

            if (arg instanceof Definition) {
                Definition d = (Definition) arg;
                addInterceptorName(d.getBeanName());
            } else {
                // Register the bean and add its name
                addInterceptorName(addSingleton(arg));
            }
            return null;
        }

        private void addInterceptorName(String name) {
            System.out.println("Ref to interceptor with name=" + name);
            //((ProxyFactoryBean)
            // AopContext.currentProxy()).setInterceptorNames(new String[]
            // {name});
            interceptorNames.add(name);
            def.getBeanDefinition().getPropertyValues().addPropertyValue(
                    "interceptorNames", interceptorNames);
        }
    }

}