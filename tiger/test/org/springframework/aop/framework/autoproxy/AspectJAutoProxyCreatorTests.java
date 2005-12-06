package org.springframework.aop.framework.autoproxy;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.aspectj.AspectMetadata;
import org.springframework.aop.support.aspectj.AbstractAtAspectJAdvisorFactoryTests.PerTargetAspect;
import org.springframework.aop.support.aspectj.AbstractAtAspectJAdvisorFactoryTests.TwoAdviceAspect;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
	
	// TODO
	public void xtestPerTargetAspect() throws SecurityException, NoSuchMethodException {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/framework/autoproxy/pertarget.xml");

		ITestBean adrian1 = (ITestBean) bf.getBean("adrian");
		assertTrue(AopUtils.isAopProxy(adrian1));
		
		// Does not trigger advice or count
		adrian1.setAge(25);
		
		assertEquals("Setter does not initiate advice", 25, adrian1.getAge());
		// Fire aspect
		
		AspectMetadata am = new AspectMetadata(PerTargetAspect.class);
		assertTrue(am.getPerClausePointcut().getMethodMatcher().matches(TestBean.class.getMethod("getSpouse"), null));
		
		adrian1.getSpouse();
		
		assertEquals("Advice has now fired", 0, adrian1.getAge());
		adrian1.setAge(11);
		assertEquals(1, adrian1.getAge());
		
		ITestBean adrian2 = (ITestBean) bf.getBean("adrian");
		assertNotSame(adrian1, adrian2);
		assertTrue(AopUtils.isAopProxy(adrian1));
		assertEquals(0, adrian2.getAge());
		assertEquals(1, adrian2.getAge());
		assertEquals(2, adrian2.getAge());
		assertEquals(3, adrian2.getAge());
		assertEquals(2, adrian1.getAge());
		
		ITestBean juergen1 = (ITestBean) bf.getBean("juergen");
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
