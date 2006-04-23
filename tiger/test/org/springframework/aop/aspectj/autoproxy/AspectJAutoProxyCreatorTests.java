/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.aspectj.autoproxy;

import junit.framework.TestCase;

import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactoryTests.PerTargetAspect;
import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactoryTests.TwoAdviceAspect;
import org.springframework.aop.aspectj.annotation.AspectMetadata;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Tests for AspectJ auto proxying. Includes mixing with Spring AOP 
 * Advisors to demonstrate that existing autoproxying contract is honoured.
 * 
 * @author Rod Johnson
 */
public class AspectJAutoProxyCreatorTests extends TestCase {
	
	public void testAspectsAreApplied() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/aspectj/autoproxy/aspects.xml");

		ITestBean adrian = (ITestBean) bf.getBean("adrian");
		
		assertTrue(AopUtils.isAopProxy(adrian));
		assertEquals(68, adrian.getAge());
	}
	
	public void testAspectsAndAdvisorAreApplied() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(
			"/org/springframework/aop/aspectj/autoproxy/aspectsPlusAdvisor.xml");
		ITestBean shouldBeWeaved = (ITestBean) ac.getBean("adrian");
		testAspectsAndAdvisorAreApplied(ac, shouldBeWeaved);
	}
	
	public void testAspectsAndAdvisorAreAppliedEvenIfComingFromParentFactory() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(
			"/org/springframework/aop/aspectj/autoproxy/aspectsPlusAdvisor.xml");
		GenericApplicationContext childAc = new GenericApplicationContext(ac);
		// Create a child factory with a bean that should be weaved                                              
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, true);
		bd.getPropertyValues().addPropertyValue(new PropertyValue("name", "Adrian")).
			addPropertyValue(new PropertyValue("age", new Integer(34)));
		childAc.registerBeanDefinition("adrian2", bd);
		// Register the advisor auto proxy creator with subclass
		childAc.registerBeanDefinition(AnnotationAwareAspectJAutoProxyCreator.class.getName(), new RootBeanDefinition(AnnotationAwareAspectJAutoProxyCreator.class));
		childAc.refresh();
		
		ITestBean beanFromChildContextThatShouldBeWeaved = (ITestBean) childAc.getBean("adrian2");
		//testAspectsAndAdvisorAreApplied(childAc, (ITestBean) ac.getBean("adrian"));
		testAspectsAndAdvisorAreApplied(childAc, beanFromChildContextThatShouldBeWeaved);
	}
	
	protected void testAspectsAndAdvisorAreApplied(ApplicationContext ac, ITestBean shouldBeWeaved) {
		TestBeanAdvisor tba = (TestBeanAdvisor) ac.getBean(TestBeanAdvisor.class.getName());
		
		MultiplyReturnValue mrv = (MultiplyReturnValue) ac.getBean(MultiplyReturnValue.class.getName());
		assertEquals(3, mrv.getMultiple());
		
		tba.count = 0;
		mrv.invocations = 0;
		
		assertTrue("Autoproxying must apply from @AspectJ aspect", AopUtils.isAopProxy(shouldBeWeaved));
		assertEquals("Adrian", shouldBeWeaved.getName());
		assertEquals(0, mrv.invocations);
		assertEquals(34 * mrv.getMultiple(), shouldBeWeaved.getAge());
		assertEquals("Spring advisor must be invoked", 2, tba.count);
		assertEquals("Must be able to hold state in aspect", 1, mrv.invocations);
	}
	
	public void testPerThisAspect() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/aspectj/autoproxy/perthis.xml");

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
	
	public void testPerTargetAspect() throws SecurityException, NoSuchMethodException {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/aspectj/autoproxy/pertarget.xml");

		ITestBean adrian1 = (ITestBean) bf.getBean("adrian");
		assertTrue(AopUtils.isAopProxy(adrian1));
		
		// Does not trigger advice or count
		int explicitlySetAge = 25;
		adrian1.setAge(explicitlySetAge);
		
		assertEquals("Setter does not initiate advice", explicitlySetAge, adrian1.getAge());
		// Fire aspect
		
		AspectMetadata am = new AspectMetadata(PerTargetAspect.class,"someBean");
		assertTrue(am.getPerClausePointcut().getMethodMatcher().matches(TestBean.class.getMethod("getSpouse"), null));
		
		adrian1.getSpouse();
		
		assertEquals("Advice has now been instantiated", 0, adrian1.getAge());
		adrian1.setAge(11);
		assertEquals("Any int setter increments", 2, adrian1.getAge());
		adrian1.setName("Adrian");
		//assertEquals("Any other setter does not increment", 2, adrian1.getAge());
		
		ITestBean adrian2 = (ITestBean) bf.getBean("adrian");
		assertNotSame(adrian1, adrian2);
		assertTrue(AopUtils.isAopProxy(adrian1));
		assertEquals(34, adrian2.getAge());
		adrian2.getSpouse();
		assertEquals("Aspect now fired", 0, adrian2.getAge());
		assertEquals(1, adrian2.getAge());
		assertEquals(2, adrian2.getAge());
		assertEquals(3, adrian1.getAge());
	}
	

	public void testTwoAdviceAspectSingleton() {
		testTwoAdviceAspectWith("twoAdviceAspect.xml");
	}
	
	public void testTwoAdviceAspectPrototype() {
		testTwoAdviceAspectWith("twoAdviceAspectPrototype.xml");
	}
	
	private void testTwoAdviceAspectWith(String location) {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/aspectj/autoproxy/" + location);
		boolean aspectSingleton = bf.isSingleton(TwoAdviceAspect.class.getName());

		ITestBean adrian1 = (ITestBean) bf.getBean("adrian");
		testPrototype(adrian1, 0);
		ITestBean adrian2 = (ITestBean) bf.getBean("adrian");
		assertNotSame(adrian1, adrian2);
		testPrototype(adrian2, aspectSingleton ? 2 : 0);
	}
	
	public void testAdviceUsingJoinPoint() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/aop/aspectj/autoproxy/usesJoinPointAspect.xml");

		ITestBean adrian1 = (ITestBean) bf.getBean("adrian");
		adrian1.getAge();
		AdviceUsingThisJoinPoint aspectInstance = (AdviceUsingThisJoinPoint) bf.getBean(AdviceUsingThisJoinPoint.class.getName());
			//(AdviceUsingThisJoinPoint) Aspects.aspectOf(AdviceUsingThisJoinPoint.class);
		//assertEquals("method-execution(int TestBean.getAge())",aspectInstance.getLastMethodEntered());		
		assertTrue(aspectInstance.getLastMethodEntered().indexOf("TestBean.getAge())") != 0);		
	}

	public void testIncludeMechanism() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
		"/org/springframework/aop/aspectj/autoproxy/usesInclude.xml");
		ITestBean adrian = (ITestBean) bf.getBean("adrian");
		
		assertTrue(AopUtils.isAopProxy(adrian));
		assertEquals(68, adrian.getAge());
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
