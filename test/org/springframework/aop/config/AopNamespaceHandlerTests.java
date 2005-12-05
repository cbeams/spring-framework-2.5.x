package org.springframework.aop.config;

import junit.framework.TestCase;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.CountingBeforeAdvice;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author robh
 */
public class AopNamespaceHandlerTests extends TestCase {

	private ApplicationContext context;

	public void setUp() {
		this.context = new ClassPathXmlApplicationContext("org/springframework/aop/config/aopNamespaceHandlerTests.xml");
	}

	public void testIsProxy() throws Exception {
		ITestBean bean = (ITestBean) this.context.getBean("testBean");

		assertTrue("Bean is not a proxy", AopUtils.isAopProxy(bean));

		// check the advice details
		Advised advised = (Advised) bean;
		Advisor[] advisors = advised.getAdvisors();

		assertEquals("Incorrect number of advisors applied.", 2, advisors.length);
	}

	public void testAdviceInvokedCorrectly() throws Exception {
		CountingBeforeAdvice getAgeCounter = (CountingBeforeAdvice) this.context.getBean("getAgeCounter");
		CountingBeforeAdvice getNameCounter = (CountingBeforeAdvice) this.context.getBean("getNameCounter");

		ITestBean bean = (ITestBean) this.context.getBean("testBean");

		assertEquals("Incorrect initial getAge count", 0, getAgeCounter.getCalls("getAge"));
		assertEquals("Incorrect initial getName count", 0, getNameCounter.getCalls("getName"));

		bean.getAge();

		assertEquals("Incorrect getAge count on getAge counter", 1, getAgeCounter.getCalls("getAge"));
		assertEquals("Incorrect getAge count on getName counter", 0, getNameCounter.getCalls("getAge"));

		bean.getName();

		assertEquals("Incorrect getName count on getName counter", 1, getNameCounter.getCalls("getName"));
		assertEquals("Incorrect getName count on getAge counter", 0, getAgeCounter.getCalls("getName"));


	}

}
