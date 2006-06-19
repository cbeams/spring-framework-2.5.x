package org.springframework.context.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ContextClassLoaderShadowingAspectForHibernate {
    @Around(
        "execution(public void org.springframework.orm.hibernate3.LocalSessionFactoryBean+.afterPropertiesSet())" +
        " || execution(public * org.hibernate.Session+.*(..))" +
        " || execution(public void org.springframework.orm.hibernate.LocalSessionFactoryBean+.afterPropertiesSet())" +
        " || execution(public * net.sf.hibernate.Session+.*(..))"
    )
    public void swapContextClassLoaderWithBeanClassLoaderAndBack(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Thread.currentThread().getContextClassLoader() == null) {
            joinPoint.proceed();
            return;
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        try {
            joinPoint.proceed();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
}
