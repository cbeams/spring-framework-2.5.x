/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class CustomAutowireConfigurerTests extends TestCase {

	private static final String CONFIG_LOCATION =
			"classpath:org/springframework/beans/factory/config/customAutowireConfigurer.xml";


	public void testCustomResolver() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
		reader.loadBeanDefinitions(CONFIG_LOCATION);
		CustomAutowireConfigurer cac = new CustomAutowireConfigurer();
		CustomResolver customResolver = new CustomResolver();
		bf.setAutowireCandidateResolver(customResolver);
		cac.postProcessBeanFactory(bf);
		TestBean testBean = (TestBean) bf.getBean("testBean");
		assertEquals("#1!", testBean.getName());
	}

	public void testCustomTypesRegistered() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		CustomAutowireConfigurer cac = new CustomAutowireConfigurer();
		CustomResolver customResolver = new CustomResolver();
		bf.setAutowireCandidateResolver(customResolver);
		Set customQualifierTypes = new HashSet();
		customQualifierTypes.add("java.lang.Integer");
		cac.setCustomQualifierTypes(customQualifierTypes);
		cac.postProcessBeanFactory(bf);
		assertEquals(1, customResolver.getCustomTypes().size());
		assertTrue(customResolver.getCustomTypes().contains(Integer.class));
	}


	public static class TestBean {

		private String name;

		public TestBean(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

	}


	public static class CustomResolver implements AutowireCandidateResolver {

		private Set customTypes = new HashSet();

		public void addQualifierType(Class qualifierType) {
			customTypes.add(qualifierType);
		}

		public Set getCustomTypes() {
			return this.customTypes;
		}

		public boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor,
				TypeConverter typeConverter) {
			if (!mbd.isAutowireCandidate()) {
				return false;
			}
			if (!beanName.matches("[a-z-]+")) {
				return false;
			}
			if (mbd.getAttribute("priority").equals("1")) {
				return true;
			}
			return false;
		}

		public String determinePrimaryCandidate(Map candidateBeans, Class type, ConfigurableListableBeanFactory beanFactory) {
			return null;
		}

	}

}
