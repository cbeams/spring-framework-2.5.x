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

package org.springframework.dao.support;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.dao.support.PersistenceExceptionTranslationAdvisorTests.RepositoryInterface;
import org.springframework.dao.support.PersistenceExceptionTranslationAdvisorTests.RepositoryInterfaceImpl;
import org.springframework.dao.support.PersistenceExceptionTranslationAdvisorTests.StereotypedRepositoryInterfaceImpl;
import org.springframework.stereotype.Repository;

/**
 * Unit tests for PersistenceExceptionTranslationPostProcessor. Does not test
 * translation--there are unit tests for the Spring AOP Advisor. Just checks
 * whether proxying occurs correctly, as a unit test should.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class PersistenceExceptionTranslationPostProcessorTests extends TestCase {
	
	public void testFailsWithNoPersistenceExceptionTranslators() {
		GenericApplicationContext gac = new GenericApplicationContext();
		BeanDefinitionRegistryBuilder bdrb = new BeanDefinitionRegistryBuilder(gac);
		bdrb.register(BeanDefinitionBuilder.rootBeanDefinition(PersistenceExceptionTranslationPostProcessor.class));
		
		bdrb.register("proxied", BeanDefinitionBuilder.rootBeanDefinition(StereotypedRepositoryInterfaceImpl.class));
		
		try {
			gac.refresh();
			fail("Should fail with no translators");
		}
		catch (BeansException ex) {
			// Ok
		}
	}
	
	public void testProxiesCorrectly() {
		GenericApplicationContext gac = new GenericApplicationContext();
		BeanDefinitionRegistryBuilder bdrb = new BeanDefinitionRegistryBuilder(gac);
		bdrb.register(BeanDefinitionBuilder.rootBeanDefinition(PersistenceExceptionTranslationPostProcessor.class));
		
		bdrb.register("notProxied", BeanDefinitionBuilder.rootBeanDefinition(RepositoryInterfaceImpl.class));
		bdrb.register("proxied", BeanDefinitionBuilder.rootBeanDefinition(StereotypedRepositoryInterfaceImpl.class));
		bdrb.register("classProxied", BeanDefinitionBuilder.rootBeanDefinition(RepositoryWithoutInterface.class));
		bdrb.register(BeanDefinitionBuilder.rootBeanDefinition(ChainedPersistenceExceptionTranslator.class));
		
		gac.refresh();
		
		RepositoryInterface shouldNotBeProxied = (RepositoryInterface) gac.getBean("notProxied");
		assertFalse(AopUtils.isAopProxy(shouldNotBeProxied));
		RepositoryInterface shouldBeProxied = (RepositoryInterface) gac.getBean("proxied");
		assertTrue(AopUtils.isAopProxy(shouldBeProxied));
		RepositoryWithoutInterface rwi = (RepositoryWithoutInterface) gac.getBean("classProxied");
		assertTrue(AopUtils.isAopProxy(rwi));
	}
	
	
	@Repository
	public static class RepositoryWithoutInterface {
		public void nameDoesntMatter() {			
		}
	}

}
