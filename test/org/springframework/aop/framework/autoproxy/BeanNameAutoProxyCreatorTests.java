/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy;

import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.aop.framework.CountingBeforeAdvice;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.TimeStamped;

/**
 * EnterpriseServices test that ources attributes from source-level metadata.
 * @author Rod Johnson
 * @version $Id: BeanNameAutoProxyCreatorTests.java,v 1.1 2003-12-12 16:50:43 johnsonr Exp $
 */
public class BeanNameAutoProxyCreatorTests extends TestCase {

	private BeanFactory bf;
	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public BeanNameAutoProxyCreatorTests(String arg0) {
		super(arg0);
	}
	
	protected void setUp() throws IOException {
		// Note that we need an application context, not just a bean factory,
		// for post processing and hence auto proxying to work
		this.bf = new ClassPathXmlApplicationContext("/org/springframework/aop/framework/autoproxy/beanNameAutoProxyCreatorTests.xml");
	}
	
	public void testNoProxy() {
		TestBean tb = (TestBean) bf.getBean("noproxy");
		assertFalse(AopUtils.isAopProxy(tb));
		assertEquals("noproxy", tb.getName());
	}
	
	public void testJdkProxyWithExactNameMatch() {
		ITestBean tb = (ITestBean) bf.getBean("onlyJdk");
		jdkAssertions(tb);
		assertEquals("onlyJdk", tb.getName());
	}
	
	public void testJdkIntroduction() {
		ITestBean tb = (ITestBean) bf.getBean("introductionUsingJdk");
		NopInterceptor nop = (NopInterceptor) bf.getBean("introductionNopInterceptor");
		assertEquals(0, nop.getCount());
		assertTrue(AopUtils.isJdkDynamicProxy(tb));
		int age = 5;
		tb.setAge(age);
		assertEquals(age, tb.getAge());
		assertTrue("Introduction was made", tb instanceof TimeStamped);
		assertEquals(0, ((TimeStamped) tb).getTimeStamp());
		assertEquals(3, nop.getCount());		
		assertEquals("introductionUsingJdk", tb.getName());
	}
	
	public void testJdkProxyWithWildcardMatch() {
		ITestBean tb = (ITestBean) bf.getBean("jdk1");
		jdkAssertions(tb);
		assertEquals("jdk1", tb.getName());
	}
	
	public void testCglibProxyWithWildcardMatch() {
		TestBean tb = (TestBean) bf.getBean("cglib1");
		cglibAssertions(tb);
		assertEquals("cglib1", tb.getName());
	}
	
	private void jdkAssertions(ITestBean tb)  {
		NopInterceptor nop = (NopInterceptor) bf.getBean("nopInterceptor");
		assertEquals(0, nop.getCount());
		assertTrue(AopUtils.isJdkDynamicProxy(tb));
		int age = 5;
		tb.setAge(age);
		assertEquals(age, tb.getAge());
		assertEquals(2, nop.getCount());
	}
	
	/**
	 * Also has counting before advice
	 * @param tb
	 * @throws Exception
	 */
	private void cglibAssertions(TestBean tb) {
		CountingBeforeAdvice cba = (CountingBeforeAdvice) bf.getBean("countingBeforeAdvice");
		NopInterceptor nop = (NopInterceptor) bf.getBean("nopInterceptor");
		assertEquals(0, cba.getCalls());
		assertEquals(0, nop.getCount());
		assertTrue(AopUtils.isCglibProxy(tb));
		int age = 5;
		tb.setAge(age);
		assertEquals(age, tb.getAge());
		assertEquals(2, nop.getCount());
		assertEquals(2, cba.getCalls());		
	}
}
