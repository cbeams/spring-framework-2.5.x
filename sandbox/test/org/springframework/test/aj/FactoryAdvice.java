package org.springframework.test.aj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.test.CallMonitor;

@Aspect
public class FactoryAdvice {
    public static final String FACTORY_NEW_OPERATION = "Factory.new";

    @Before("execution(Factory.new())")
    public void callToConstruct() {
        CallMonitor.increment(FACTORY_NEW_OPERATION);
    }
}
