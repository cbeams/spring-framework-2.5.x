package org.springframework.jmx.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.aop.TargetSource;

/**
 * @author robh
 */
public class LazyInitInterceptor implements MethodInterceptor{

    private final BeanFactory beanFactory;

    private final String beanName;

    private Object target;

    public LazyInitInterceptor(BeanFactory beanFactory, String beanName) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if(target == null) {
            target = beanFactory.getBean(beanName);
        }

        return methodInvocation.getMethod().invoke(target, methodInvocation.getArguments());
    }

}
