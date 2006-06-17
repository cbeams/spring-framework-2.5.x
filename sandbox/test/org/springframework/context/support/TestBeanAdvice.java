package org.springframework.context.support;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.test.CallMonitor;

@Aspect
public class TestBeanAdvice {
    @Before("execution(public void org.springframework.beans.TestBean.setBeanFactory(..))")
    public void onSetBeanFactory() {
        CallMonitor.increment("TestBean.setBeanFactory");
    }
}
