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

import org.springframework.aop.TargetSource;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.SerializablePerson;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.SerializationTestUtils;

import junit.framework.TestCase;

/**
 * Unit tests relating to the abstract AbstractPrototypeBasedTargetSource
 * and not subclasses.
 * @author Rod Johnson
 *          16:20:14 trisberg Exp $
 */
public class PrototypeBasedTargetSourceTests extends TestCase {

	public void testSerializability() throws Exception {
		MutablePropertyValues tsPvs = new MutablePropertyValues();
		tsPvs.addPropertyValue("targetBeanName", "person");
		RootBeanDefinition tsBd = new RootBeanDefinition(
				TestTargetSource.class, tsPvs);

		MutablePropertyValues beanPvs = new MutablePropertyValues();
		RootBeanDefinition beanBd = new RootBeanDefinition(
				SerializablePerson.class, beanPvs, false);

		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerBeanDefinition("ts", tsBd);
		bf.registerBeanDefinition("person", beanBd);

		TestTargetSource cpts = (TestTargetSource) bf
				.getBean("ts");
		TargetSource serialized = (TargetSource) SerializationTestUtils
				.serializeAndDeserialize(cpts);
		assertTrue("Changed to SingletonTargetSource on deserialization",
				serialized instanceof SingletonTargetSource);
		SingletonTargetSource sts = (SingletonTargetSource) serialized;
		assertNotNull(sts.getTarget());
	}

	
	private static class TestTargetSource extends AbstractPrototypeBasedTargetSource {
		
		/**
		 * Nonserializable test field to check that subclass
		 * state can't prevent serialization from working
		 */
		private TestBean thisFieldIsNotSerializable = new TestBean();

		/**
		 * @see org.springframework.aop.TargetSource#getTarget()
		 */
		public Object getTarget() throws Exception {
			return newPrototypeInstance();
		}

		/**
		 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
		 */
		public void releaseTarget(Object target) throws Exception {
			// Do nothing
		}
	}

}