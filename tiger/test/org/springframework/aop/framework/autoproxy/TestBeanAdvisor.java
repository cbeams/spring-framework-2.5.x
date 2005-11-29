package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.ITestBean;

public class TestBeanAdvisor extends StaticMethodMatcherPointcutAdvisor {
	
	public int count;
	
	public TestBeanAdvisor() {
		setAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				++count;
			}
		});
	}

	public boolean matches(Method method, Class targetClass) {
		return ITestBean.class.isAssignableFrom(targetClass);
	}

}
