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

import java.util.LinkedList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.Pointcuts;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 
 * @author Rod Johnson
 */
public class Configurer {

    private BeanDefinitionRegistry bdi;
    
    private List definitions = new LinkedList();
    
    private int defaultAutowireMode = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
    
    public Configurer(BeanDefinitionRegistry bdi) {
        this.bdi = bdi;
    }
    
    public void setDefaultAutowireMode(int mode) {
        this.defaultAutowireMode = mode;
    }

    public Definition add(String name, Class clazz) {
        DefinitionImpl def = new DefinitionImpl(name, clazz, defaultAutowireMode);
        definitions.add(def);
        bdi.registerBeanDefinition(name, def.getBeanDefinition());
        
        //return def;
        return recordable();
    }
    
    
    /**
     * Last definition recordable
     */
    public Definition recordable() throws BeansException {
        final Definition def = (Definition) definitions.get(definitions.size() - 1);
        Object target = org.springframework.beans.BeanUtils.instantiateClass(def.getBeanDefinition().getBeanClass());
        ProxyFactory pf = new ProxyFactory(target);
        pf.setProxyTargetClass(true);
        DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(def);
        pf.addAdvice(dii);
        pf.addAdvisor(new DefaultPointcutAdvisor(Pointcuts.SETTERS, new RecordingInterceptor(def)));
//        pf.addAdvice(new MethodInterceptor() {
//            public Object invoke(MethodInvocation mi) throws Throwable {
//                if ()
//                throw new UnsupportedOperationException(mi.getMethod().getName());
//            }
//        });
        System.err.println(pf.toProxyConfigString());
        Definition proxy = (Definition) pf.getProxy();
        return proxy;
    }
   
    /**
     * Interceptor for setter methods that saves the values to the backing BeanDefinition.
     * Supports references to other beans in the factory, as well as
     * simple types.
     */
    private class RecordingInterceptor implements MethodInterceptor {
        private final Definition def;

        private RecordingInterceptor(Definition def) {
            this.def = def;
        }

        public Object invoke(MethodInvocation mi) throws Throwable {
            String propName = mi.getMethod().getName().substring(3);
            propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);
            Object value = mi.getArguments()[0];
            if (value instanceof Definition) {
                String refName = ((Definition) value).getBeanName();
                System.err.println("Recorded reference to name " + refName);
                value = new RuntimeBeanReference(refName);
            }
            // TODO complex types that are not references!? Should probably allow them
            PropertyValue pv = new PropertyValue(propName, value);
            def.getBeanDefinition().getPropertyValues().addPropertyValue(pv);
            System.out.println("Added " + pv);
            return null;
        }
    }
    
    /**
     * Apply all configuration to the factory
     *
     */
//    public void apply() {
//        for (int i = 0; i < definitions.size(); i++) {
//            Definition def = (Definition) definitions.get(i);
//            bdi.registerBeanDefinition(def.getName(), def.getBeanDefinition());
//        }
//    }
    
}
