package org.springframework.aop.framework.autoproxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class MultiplyReturnValue {
	
	private int multiple = 2;
	
	public int invocations;
	
	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}
	
	public int getMultiple() {
		return this.multiple;
	}
	
	@Around("execution(int *.getAge())")
	public Object doubleReturnValue(ProceedingJoinPoint pjp) throws Throwable {
		++invocations;
		int result = (Integer) pjp.proceed();
		return result * this.multiple;
	}


}
