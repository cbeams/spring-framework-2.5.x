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

package org.springframework.beans.factory.config;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;

/**
 * This test will go away once a real lifecycle test is created for AppContexts
 * 
 * @author Colin Sampaleanu
 * @since 02.10.2003
 */
public class BeanFactoryPostProcessorTests extends TestCase {

	public void testBeanFactoryPostProcessorExecutedByAppContext() {
		
		StaticApplicationContext ac = new StaticApplicationContext();
		ac.registerSingleton("tb1", TestBean.class, new MutablePropertyValues());
		ac.registerSingleton("tb2", TestBean.class, new MutablePropertyValues());
		TestBeanFactoryPostProcessor bfpp = new TestBeanFactoryPostProcessor();
		ac.addBeanFactoryPostProcessor(bfpp);
		assertTrue(bfpp.wasCalled == false);
		ac.refresh();
		assertTrue(bfpp.wasCalled == true);
	}
	
	public void testBeanFactoryPostProcessorExecutedByAppContext2() {
		
		StaticApplicationContext ac = new StaticApplicationContext();
		ac.registerSingleton("tb1", TestBean.class, new MutablePropertyValues());
		ac.registerSingleton("tb2", TestBean.class, new MutablePropertyValues());
		ac.registerSingleton("bfpp", TestBeanFactoryPostProcessor.class, new MutablePropertyValues());
		ac.refresh();
		TestBeanFactoryPostProcessor bfpp = (TestBeanFactoryPostProcessor) ac.getBean("bfpp");
		assertTrue(bfpp.wasCalled == true);
	}


	public void testBeanFactoryPostProcessorNotExecutedByBeanFactory() {
		
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("tb1",
				new RootBeanDefinition(TestBean.class, new MutablePropertyValues()));
        bf.registerBeanDefinition("tb2",
				new RootBeanDefinition(TestBean.class, new MutablePropertyValues()));
        bf.registerBeanDefinition("bfpp",
				new RootBeanDefinition(TestBeanFactoryPostProcessor.class, new MutablePropertyValues()));
		
		TestBeanFactoryPostProcessor bfpp = (TestBeanFactoryPostProcessor) bf.getBean("bfpp");
		assertTrue(bfpp.wasCalled == false);
	}

	
	public static class TestBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
		
		public boolean wasCalled = false;
		
		public TestBeanFactoryPostProcessor() {
		}
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			wasCalled = true;
		}
	}
	
}
