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

package org.springframework.aop.target;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for pooling invoker interceptor
 * TODO need to make these tests stronger: it's hard to
 * make too many assumptions about a pool
 * @author Rod Johnson
 * @version $Id: CommonsPoolTargetSourceTests.java,v 1.4 2004-03-18 03:01:18 trisberg Exp $
 */
public class CommonsPoolTargetSourceTests extends TestCase {

	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;

	private XmlBeanFactory beanFactory;
	
	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("commonsPoolTests.xml", getClass()));
	}
	
	/**
	 * We must simulate container shutdown, which should clear threads.
	 */
	protected void tearDown() {
		// Will call pool.close()
		this.beanFactory.destroySingletons();
	}

	private void testFunctionality(String name) {
		SideEffectBean pooled = (SideEffectBean) beanFactory.getBean(name);
		assertEquals(INITIAL_COUNT, pooled.getCount() );
		pooled.doWork();
		assertEquals(INITIAL_COUNT + 1, pooled.getCount() );
		
		pooled = (SideEffectBean) beanFactory.getBean(name);
		// Just check that it works--we can't make assumptions
		// about the count
		pooled.doWork();
		//assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
	}
	
	public void testFunctionality() {
		testFunctionality("pooled");
	}
	
	public void testFunctionalityWithNoInterceptors() {
		testFunctionality("pooledNoInterceptors");
	}
	
	public void testConfigMixin() {
		SideEffectBean pooled = (SideEffectBean) beanFactory.getBean("pooledWithMixin");
		assertEquals(INITIAL_COUNT, pooled.getCount() );
		PoolingConfig conf = (PoolingConfig) beanFactory.getBean("pooledWithMixin");
		// TODO one invocation from setup
		//assertEquals(1, conf.getInvocations());
		pooled.doWork();
	//	assertEquals("No objects active", 0, conf.getActive());
		assertEquals("Correct target source", 25, conf.getMaxSize());
//		assertTrue("Some free", conf.getFree() > 0);
		//assertEquals(2, conf.getInvocations());
		assertEquals(25, conf.getMaxSize());
	}

}
