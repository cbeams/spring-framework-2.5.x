package org.springframework.aop.aspectj.autoproxy;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class AutoProxyWithCodeStyleAspectsTests extends TestCase {

	public void testNoAutoproxyingOfAjcCompiledAspects() {
		new ClassPathXmlApplicationContext("org/springframework/aop/aspectj/autoproxy/ajcAutoproxyTests.xml");		
	}
	
}
