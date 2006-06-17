package org.springframework.test.aj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.test.CallMonitor;

@Aspect
public class TestAspect {

    @Before("target(AbstractAdvicedTests) && execution(void testIncrementOnSelf())")
    public void increment() {
        CallMonitor.increment("testIncrementOnSelf");
    }

    @Before("execution(org.springframework.jdbc.core.JdbcTemplate.new())")
    public void increment2() {
        CallMonitor.increment("new JdbcTemplate");
    }

    @Before("execution(* org.springframework.util.ClassUtils.getDefaultClassLoader())")
    public void increment3() {
        CallMonitor.increment("ClassUtils.getDefaultClassLoader");
    }


}
