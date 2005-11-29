package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.aspectj.AbstractAtAspectJAdvisorFactoryTest.TwoAdviceAspect;
import org.springframework.beans.ITestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class AspectJAutoProxyCreatorTests extends TestCase {
	
	public void testAspectsAreApplied() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/framework/autoproxy/aspects.xml");

		ITestBean adrian = (ITestBean) bf.getBean("adrian");
		
		assertTrue(AopUtils.isAopProxy(adrian));
		
		Advised advised = (Advised) adrian;
		System.out.println(advised.toProxyConfigString());
		
		assertEquals(68, adrian.getAge());
	}
	
	public void testAspectsAndAdvisorAreApplied() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/framework/autoproxy/aspectsPlusAdvisor.xml");

		TestBeanAdvisor tba = (TestBeanAdvisor) bf.getBean(TestBeanAdvisor.class.getName());
		
		MultiplyReturnValue mrv = (MultiplyReturnValue) bf.getBean(MultiplyReturnValue.class.getName());
		assertEquals(3, mrv.getMultiple());
		
		tba.count = 0;
		mrv.invocations = 0;
		ITestBean adrian = (ITestBean) bf.getBean("adrian");
		assertTrue(AopUtils.isAopProxy(adrian));
		assertEquals("Adrian", adrian.getName());
		assertEquals(0, mrv.invocations);
		assertEquals(34 * mrv.getMultiple(), adrian.getAge());
		assertEquals("Spring advisor must be invoked", 2, tba.count);
		assertEquals("Must be able to hold state in aspect", 1, mrv.invocations);
	}
	
	public void testPerThisAspect() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/framework/autoproxy/perthis.xml");

		ITestBean adrian1 = (ITestBean) bf.getBean("adrian");
		assertTrue(AopUtils.isAopProxy(adrian1));
		Advised advised1 = (Advised) adrian1;
		assertEquals(0, adrian1.getAge());
		assertEquals(1, adrian1.getAge());
		
		ITestBean adrian2 = (ITestBean) bf.getBean("adrian");
		assertNotSame(adrian1, adrian2);
		assertTrue(AopUtils.isAopProxy(adrian1));
		assertEquals(0, adrian2.getAge());
		assertEquals(1, adrian2.getAge());
		assertEquals(2, adrian2.getAge());
		assertEquals(3, adrian2.getAge());
		assertEquals(2, adrian1.getAge());
	}
	

	public void testTwoAdviceAspectSingleton() {
		testTwoAdviceAspectWith("twoAdviceAspect.xml");
	}
	
	public void testTwoAdviceAspectPrototype() {
		testTwoAdviceAspectWith("twoAdviceAspectPrototype.xml");
	}
	
	private void testTwoAdviceAspectWith(String location) {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/framework/autoproxy/" + location);
		boolean aspectSingleton = bf.isSingleton(TwoAdviceAspect.class.getName());

		ITestBean adrian1 = (ITestBean) bf.getBean("adrian");
		testPrototype(adrian1, 0);
		ITestBean adrian2 = (ITestBean) bf.getBean("adrian");
		assertNotSame(adrian1, adrian2);
		testPrototype(adrian2, aspectSingleton ? 2 : 0);
	}

	private void testPrototype(ITestBean adrian1, int start) {
		assertTrue(AopUtils.isAopProxy(adrian1));
		//TwoAdviceAspect twoAdviceAspect = (TwoAdviceAspect) bf.getBean(TwoAdviceAspect.class.getName());
		adrian1.setName("");
		assertEquals(start++, adrian1.getAge());
		int newAge = 32;
		adrian1.setAge(newAge);
		assertEquals(start++, adrian1.getAge());
		adrian1.setAge(0);
		assertEquals(start++, adrian1.getAge());
	}
}
