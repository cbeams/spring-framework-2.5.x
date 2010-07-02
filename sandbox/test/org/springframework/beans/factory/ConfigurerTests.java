/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.CountingBeforeAdvice;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 
 * @author Rod Johnson
 */
public class ConfigurerTests extends TestCase {

	public void testOnBeanFactoryNoProcessors() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		Configurer cfg = new Configurer(bf);
		cfg.add("testBean", TestBean.class).prop("name", "tom");

		System.out.println(bf);
		ITestBean tb = (ITestBean) bf.getBean("testBean");
		assertEquals("tom", tb.getName());
	}
	
	public void testXmlAutoProxyCreator() {
		GenericApplicationContext gac = new GenericApplicationContext();
		Configurer cfg = new Configurer(gac);
		cfg.xml(getClass(), "test.xml");
		cfg.add("testBean", TestBean.class).prop("name", "tom");
		
		// Not picked up by get beans of type
		
		// TODO register that does factory bean or factory method!?
		// how to parameterize? would need to add class!?
		
		//cfg.addSingleton("nopAdvisor", new DefaultPointcutAdvisor(new NopInterceptor()));
		
		((DefaultPointcutAdvisor) cfg.add("nopAdvisor", DefaultPointcutAdvisor.class))
			.setAdvice(new NopInterceptor());
		
		gac.refresh();
		
		DefaultPointcutAdvisor a = (DefaultPointcutAdvisor) gac.getBean("nopAdvisor");
		NopInterceptor ni = (NopInterceptor) a.getAdvice();
		
		DefaultAdvisorAutoProxyCreator apc = (DefaultAdvisorAutoProxyCreator) gac.getBean("autoproxy");

		System.out.println(gac);
		assertEquals(0, ni.getCount());
		ITestBean tb = (ITestBean) gac.getBean("testBean");
		assertEquals("tom", tb.getName());
		assertEquals(1, ni.getCount());
		assertTrue(tb instanceof Advised);
	}
	
	/*public void testGroovyScript() {
		GenericApplicationContext bf = new GenericApplicationContext();
		Configurer cfg = new Configurer(bf);
		
		String propVal = "zoe";
		
		cfg.add("gsf", GroovyScriptFactory.class);
		
		// TODO doesn't support DI here
		cfg.addFactoryBean("hello", "gsf", "create")
			.carg("org/springframework/beans/factory/script/groovy/PropertyHello.groovy")
			.prop("message", propVal);
		
		bf.refresh();
		
		Hello hello = (Hello) bf.getBean("hello");
		
		assertTrue("Not a script", hello instanceof DynamicScript);
		
		assertEquals(propVal, hello.sayHello());
	}*/

	public void testGetter() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		Configurer cfg = new Configurer(bf);
		TestBean tb = (TestBean) cfg.add("testBean", TestBean.class);
		tb.setAge(25);
		assertEquals(25, tb.getAge());
		assertNull(tb.getSpouse());
//		try {
//			tb.getAge();
//			fail();
//		}
//		catch (UnsupportedOperationException ex) {
//			System.out.println(ex.getMessage());
//		}
	}

	public void testFactoryBean() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		Configurer cfg = new Configurer(bf);
		cfg.add("testBean", MyFactory.class).prop("myString", "myString");

		System.out.println(bf);
		ITestBean tb = (ITestBean) bf.getBean("testBean");
		assertEquals("myString", tb.getName());
	}

	public static class MyFactory implements FactoryBean {

		private String myString;

		public void setMyString(String myString) {
			this.myString = myString;
		}

		public String getMyString() {
			return myString;
		}

		/**
		 * @see org.springframework.beans.factory.FactoryBean#getObject()
		 */
		public Object getObject() throws Exception {
			TestBean tb = new TestBean();
			tb.setName(myString);
			return tb;
		}

		/**
		 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
		 */
		public Class getObjectType() {
			return TestBean.class;
		}

		/**
		 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
		 */
		public boolean isSingleton() {
			return true;
		}
	}

	// TODO SHOULD be able to run same tests on ac and bf

	public void testAdvisedOnBeanFactory() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		testAdvised(bf);
	}

	public void testAdvisedOnApplicationContext() {
		GenericApplicationContext gac = new GenericApplicationContext();
		testAdvised(gac);
	}

	public void testProperties() {
		GenericApplicationContext gac = new GenericApplicationContext();
		Configurer cfg = new Configurer(gac);
		cfg.properties(getClass(), "test.properties");

		String tomName = "tom";
		String beanName = "tom";
		Definition def = cfg.add(beanName, TestBean.class).prop("name", tomName);

		gac.refresh();
		
		TestBean tb = (TestBean) gac.getBean(beanName);
		assertEquals(tomName, tb.getName());
		assertEquals("Properties were applied", 38, tb.getAge());
	}

	// TFPB

	private void testAdvised(BeanDefinitionRegistry bdr) {

		Configurer cfg = new Configurer(bdr);
		//cfg.properties(getClass(), "test.properties").setIgnoreResourceNotFound(false);

		NopInterceptor nop = (NopInterceptor) cfg.add("nop", NopInterceptor.class);

		String tomName = "tom";
		String beanName = "tom";
		Definition def = cfg.add(beanName, TestBean.class).prop("name", tomName);
		AdvisedSupport pfb = cfg.advise(def);

		// Add a named bean here
		pfb.addAdvice(nop);
		// Add this guy to the bean factory
		pfb.addAdvice(new CountingBeforeAdvice());
		pfb.setExposeProxy(true);

		if (bdr instanceof AbstractApplicationContext) {
			System.out.println("----------- refresh ----------------");
			((AbstractApplicationContext) bdr).refresh();
		}

		BeanFactory bf = (BeanFactory) bdr;
		ITestBean tb = (ITestBean) bf.getBean(beanName);
		assertTrue(AopUtils.isAopProxy(tb));
		nop = (NopInterceptor) bf.getBean("nop");
		assertEquals(0, nop.getCount());
		assertEquals(tomName, tb.getName());

		assertEquals(1, nop.getCount());
		Advised advised = (Advised) tb;
		assertTrue(advised.isExposeProxy());
		assertEquals(2, advised.getAdvisors().length);
		CountingBeforeAdvice cba = (CountingBeforeAdvice) bf.getBean(CountingBeforeAdvice.class.getName());
		assertEquals(1, cba.getCalls());

		//assertEquals("Properties were applied", 38, tb.getAge());
	}

	// TODO how to set dependency on factory bean!?

	public void testOnBeanFactoryNoProcessorsWithRecording() {
		String beckyName = "becky";

		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		Configurer cfg = new Configurer(bf);
		//cfg.setDefaultAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);

		// TODO possibility of infinite loop...
		TestBean becky = (TestBean) cfg.add("becky", TestBean.class).singleton(false).prop("name", beckyName)
				.ref("spouse", "tom");

		System.err.println(((Advised) becky).toProxyConfigString());

		// TestBean becky = (TestBean) cfg.recordable();
		//becky.setName(beckyName);

		TestBean tom = (TestBean) cfg.add("tom", TestBean.class);
		tom.setName("tom");
		tom.setSpouse(becky);
		tom.setAge(24);

		//cfg.apply();

		System.out.println(bf);
		ITestBean tb = (ITestBean) bf.getBean("tom");
		assertEquals(24, tb.getAge());
		assertEquals("tom", tb.getName());
		assertEquals(beckyName, tb.getSpouse().getName());

		ITestBean becky1 = (ITestBean) bf.getBean("becky");
		ITestBean becky2 = (ITestBean) bf.getBean("becky");
		assertNotSame(becky1, becky2);
		assertEquals(beckyName, becky1.getName());
		assertEquals(beckyName, becky2.getName());
		assertSame(tb, becky1.getSpouse());
		assertSame(tb, becky2.getSpouse());
	}

	//    public void testOnApplicationContextWithPostProcessors() {
	//        AbstractApplicationContext ac = new ParameterizableApplicationContext();
	//        NopInterceptor nop = new NopInterceptor();
	//        ac.getBeanFactory().registerSingleton("nopInterceptor", nop);
	//        BeanNameAutoProxyCreator bnapc = new BeanNameAutoProxyCreator();
	//        bnapc.setInterceptorNames(new String[] { "nopInterceptor"});
	//        bnapc.setBeanNames(new String[] { "test*" });
	//        bnapc.setBeanFactory(ac.getBeanFactory());
	//        
	//        ac.getBeanFactory().addBeanPostProcessor(bnapc);
	//        JavaBeanDefinitionReader jbr = new JavaBeanDefinitionReader((BeanDefinitionRegistry)
	// ac.getBeanFactory());
	//        assertEquals(1, jbr.addDefinitions(MyBeans.class));
	//        System.out.println(ac);
	//        ITestBean tb = (ITestBean) ac.getBean("testBean");
	//        assertEquals("tom", tb.getName());
	//        
	//        assertTrue(AopUtils.isAopProxy(tb));
	//        assertEquals(1, nop.getCount());
	//    }

}