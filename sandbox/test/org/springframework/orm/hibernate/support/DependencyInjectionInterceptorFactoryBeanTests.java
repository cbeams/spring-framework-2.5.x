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

package org.springframework.orm.hibernate.support;

import junit.framework.TestCase;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.metadata.ClassMetadata;

import org.easymock.MockControl;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.DependencyInjectionAspectSupportTests;

import bsh.Interpreter;

/**
 * 
 * @author Rod Johnson
 */
public class DependencyInjectionInterceptorFactoryBeanTests extends TestCase {
	
	public void testSetsNextInterceptorNonNull() throws Exception {		
		DependencyInjectionInterceptorFactoryBean dias = new DependencyInjectionInterceptorFactoryBean();
		Interceptor next = new ChainedInterceptorSupport() {};
		dias.setNextInterceptor(next);
		assertSame(next, dias.getNextInterceptor());
		ChainedInterceptorSupport interceptor = (ChainedInterceptorSupport) dias.getObject();
		assertSame("Enclosing FactoryBean populated nextInterceptor property on created ChainedInterceptorSupport",
				next, interceptor.getNextInterceptor());
	}
	
	public void testNoChainNoRegistration() throws Exception {
		DependencyInjectionInterceptorFactoryBean dias = new DependencyInjectionInterceptorFactoryBean();
		// We'll ask for a different class
		dias.addAutowireByNameClass(NestedTestBean.class);
		dias.setSessionFactory(mockSessionFactory(TestBean.class));
		dias.setBeanFactory(new DefaultListableBeanFactory());
		dias.afterPropertiesSet();
		
		Interceptor interceptor = (Interceptor) dias.getObject();
		assertNull("Doesn't know about this class", interceptor.instantiate(TestBean.class, new Integer(25)));
	}
	
	public void testAutowireByType() throws Exception {
		DependencyInjectionInterceptorFactoryBean dias = new DependencyInjectionInterceptorFactoryBean();
		DefaultListableBeanFactory bf = DependencyInjectionAspectSupportTests.beanFactoryWithTestBeanSingleton();
		
		dias.addAutowireByTypeClass(TestBean.class);
		dias.setSessionFactory(mockSessionFactory(TestBean.class));
		dias.setBeanFactory(bf);
		dias.afterPropertiesSet();
		
		ChainedInterceptorSupport interceptor = (ChainedInterceptorSupport) dias.getObject();
		assertNull("Next interceptor in chain is null by default", interceptor.getNextInterceptor());
		
		TestBean tb = (TestBean) interceptor.instantiate(TestBean.class, new Integer(12));
		assertNull("Doesn't know about this class", interceptor.instantiate(NestedTestBean.class, new Integer(25)));
		assertNotNull("Knows about this class", tb);
		assertSame("Spouse dependency was populated", bf.getBean("kerry"), tb.getSpouse() );
		assertEquals("Id was set", 12, tb.getAge());
	}
	
	public void testAutowirePrototypeUsingNamedSessionFactory() throws Exception {
		testAutowirePrototype(true);
	}
	
	public void testAutowirePrototypeUsingSessionFactorySetter() throws Exception {
		testAutowirePrototype(false);
	}
	
	private void testAutowirePrototype(boolean useSessionFactoryName) throws Exception {
		DependencyInjectionInterceptorFactoryBean dias = new DependencyInjectionInterceptorFactoryBean();
		DefaultListableBeanFactory bf = DependencyInjectionAspectSupportTests.beanFactoryWithTestBeanSingleton();
		String expectedName = "cherie";
		String prototypeName = "tb";
		bf.registerBeanDefinition(prototypeName, DependencyInjectionAspectSupportTests.prototypeSpouseBeanDefinition(expectedName));
		
		dias.addManagedClassToPrototypeMapping(TestBean.class, prototypeName);
		if (useSessionFactoryName) {
			// Use SessionFactory bean name property to avoid circular dependency
			String sessionFactoryBeanName = "sf";
			bf.registerSingleton(sessionFactoryBeanName, mockSessionFactory(TestBean.class));
			dias.setSessionFactoryName(sessionFactoryBeanName);
		}
		else {
			dias.setSessionFactory(mockSessionFactory(TestBean.class));
		}
		dias.setBeanFactory(bf);
		dias.afterPropertiesSet();
		
		Interceptor interceptor = (Interceptor) dias.getObject();
		TestBean tb1 = (TestBean) interceptor.instantiate(TestBean.class, new Integer(25));
		assertNull("Doesn't know about this class", interceptor.instantiate(NestedTestBean.class, new Integer(25)));
		assertNotNull("Knows about this class", tb1);
		assertSame("Spouse dependency was populated", bf.getBean("kerry"), tb1.getSpouse() );
		assertEquals("Name was populated", expectedName, tb1.getName());
		assertEquals("Id was set", 25, tb1.getAge());
		
		TestBean tb2 = (TestBean) interceptor.instantiate(TestBean.class, new Integer(26));
		assertEquals("Name was populated", expectedName, tb2.getName());
		assertEquals("Id was set", 26, tb2.getAge());
		assertNotSame("Prototypes are distinct", tb1, tb2);
		assertSame("Two prototypes reference same singleton", tb1.getSpouse(), tb2.getSpouse());
	}

	protected SessionFactory mockSessionFactory(Class clazz) throws Exception {
		MockControl mc = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) mc.getMock();
		sf.getClassMetadata(clazz);
		
		// Beanshell's an easy way to mock just one method of many...
		Interpreter bsh = new Interpreter();
		bsh.eval("public String getIdentifierPropertyName() { return \"age\"; }");
		ClassMetadata cm = (ClassMetadata) bsh.eval("return (" + ClassMetadata.class.getName() + ") this");
		
		// We don't validate that the calls were made
		mc.setReturnValue(cm, 2);
		mc.replay();
		return sf;
	}
}
