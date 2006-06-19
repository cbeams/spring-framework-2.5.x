package org.springframework.context.support;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.test.CallMonitor;
import org.springframework.beans.factory.InitializingBean;

@Aspect
public class TestBeanAdvice implements InitializingBean {

    public void afterPropertiesSet() throws Exception {
        CallMonitor.increment("TestBeanAdvice.afterPropertiesSet");
    }

    // Temporarily commented out until AspectJ bug 147701 is fixed.
//    public static class ApplicationContextAwareImpl implements ApplicationContextAware {
//        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//
//        }
//    }
//
//    @DeclareParents(value="org.springframework.beans.TestBean", defaultImpl=TestBeanAdvice.ApplicationContextAwareImpl.class)
//    private ApplicationContextAware implementationInterface;

    @Before("execution(public void org.springframework.beans.TestBean.setBeanFactory(..))")
    public void onSetBeanFactory() {
        CallMonitor.increment("TestBean.setBeanFactory");
    }
}
