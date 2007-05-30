package org.springframework.aop.aspectj.autoproxy;

import org.aspectj.lang.annotation.Aspect;

@Aspect
public aspect CodeStyleAspect {
	
	private String foo;
	
	pointcut somePC() : call(* someMethod());
	
	before() : somePC() {
		System.out.println("match");
	}
	
	public void setFoo(String foo) {
		this.foo = foo;
	}
}