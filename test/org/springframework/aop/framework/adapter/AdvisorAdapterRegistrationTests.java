package org.springframework.aop.framework.adapter;

import org.springframework.aop.Advisor;
import org.springframework.aop.SimpleBeforeAdviceImpl;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.ITestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * TestCase for AdvisorAdapterRegistrationManager mechanism
 * @author Dmitriy Kopylenko
 * @version $Id: AdvisorAdapterRegistrationTests.java,v 1.1 2004-02-27 14:28:27 dkopylenko Exp $
 */
public class AdvisorAdapterRegistrationTests extends TestCase {

	/**
	 * @param arg0
	 */
	public AdvisorAdapterRegistrationTests(String arg0) {
		super(arg0);
	}

	public void testAdvisorAdapterRegistrationManagerNotPresentInContext() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("/org/springframework/aop/framework/adapter/withoutBPPContext.xml");
		ITestBean tb = (ITestBean) ctx.getBean("testBean");

		//Just invoke any method to see if advice fired
		try {
			tb.getName();
			fail("Should throw UnknownAdviceTypeException");
		}
		catch (UnknownAdviceTypeException ex) {
			//expected
			assertEquals(0, getAdviceImpl(tb).getInvocationCounter());
		}
	}

	public void testAdvisorAdapterRegistrationManagerPresentInContext() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("/org/springframework/aop/framework/adapter/withBPPContext.xml");
		ITestBean tb = (ITestBean) ctx.getBean("testBean");

		//Just invoke any method to see if advice fired
		try {
			tb.getName();
			assertEquals(1, getAdviceImpl(tb).getInvocationCounter());
		}
		catch (UnknownAdviceTypeException ex) {
			fail("Should not throw UnknownAdviceTypeException");
		}
	}

	private SimpleBeforeAdviceImpl getAdviceImpl(ITestBean tb) {
		Advised advised = (Advised) tb;
		Advisor advisor = advised.getAdvisors()[0];
		return (SimpleBeforeAdviceImpl) advisor.getAdvice();
	}
}
