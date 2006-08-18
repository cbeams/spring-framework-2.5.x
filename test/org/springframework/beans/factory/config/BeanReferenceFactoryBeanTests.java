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

package org.springframework.beans.factory.config;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.mock.easymock.AbstractScalarMockTemplate;
import org.springframework.test.AssertThrows;

/**
 * @author Rick Evans
 */
public final class BeanReferenceFactoryBeanTests extends TestCase {

	private static final String TARGET_BEAN_NAME = "foo";
	private static final Class TARGET_BEAN_TYPE = String.class;
	private static final Object TARGET_BEAN = new Object();


	public void testChokesWhenMissingTargetBeanName() throws Exception {
		new AbstractScalarMockTemplate(BeanFactory.class) {
			public void doTest(Object mockObject) throws Exception {
				final BeanFactory factory = (BeanFactory) mockObject;
				new AssertThrows(IllegalArgumentException.class) {
					public void test() throws Exception {
						new BeanReferenceFactoryBean().setBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testChokesWhenBeanFactoryHasNotBeenSet() throws Exception {
		new AssertThrows(FactoryBeanNotInitializedException.class) {
			public void test() throws Exception {
				BeanReferenceFactoryBean bean = new BeanReferenceFactoryBean();
				bean.setTargetBeanName(TARGET_BEAN_NAME);
				bean.getObject();
			}
		}.runTest();
	}

	public void testChokesWhenTargetBeanNameCannotBeFoundInTheBeanFactory() throws Exception {
		new AbstractScalarMockTemplate(BeanFactory.class) {
			public void setupExpectations(MockControl mockControl, Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				factory.containsBean(TARGET_BEAN_NAME);
				mockControl.setReturnValue(false);
			}
			public void doTest(Object mockObject) throws Exception {
				final BeanFactory factory = (BeanFactory) mockObject;
				new AssertThrows(NoSuchBeanDefinitionException.class) {
					public void test() throws Exception {
						BeanReferenceFactoryBean bean = new BeanReferenceFactoryBean();
						bean.setTargetBeanName(TARGET_BEAN_NAME);
						bean.setBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testGetObjectTypeSimplyReturnsTheTypeOfTheNamedBeanAsReportedByTheBeanFactory() throws Exception {
		new AbstractScalarMockTemplate(BeanFactory.class) {
			public void setupExpectations(MockControl mockControl, Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				factory.containsBean(TARGET_BEAN_NAME);
				mockControl.setReturnValue(true);
				factory.getType(TARGET_BEAN_NAME);
				mockControl.setReturnValue(TARGET_BEAN_TYPE);
			}
			public void doTest(Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				BeanReferenceFactoryBean bean = new BeanReferenceFactoryBean();
				bean.setTargetBeanName(TARGET_BEAN_NAME);
				bean.setBeanFactory(factory);
				assertEquals(TARGET_BEAN_TYPE, bean.getObjectType());
			}
		}.test();
	}

	public void testIsSingletonSimplyReturnsTheScopeOfTheNamedBeanAsReportedByTheBeanFactory() throws Exception {
		new AbstractScalarMockTemplate(BeanFactory.class) {
			public void setupExpectations(MockControl mockControl, Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				factory.containsBean(TARGET_BEAN_NAME);
				mockControl.setReturnValue(true);
				factory.isSingleton(TARGET_BEAN_NAME);
				mockControl.setReturnValue(false);
			}
			public void doTest(Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				BeanReferenceFactoryBean bean = new BeanReferenceFactoryBean();
				bean.setTargetBeanName(TARGET_BEAN_NAME);
				bean.setBeanFactory(factory);
				assertFalse(bean.isSingleton());
			}
		}.test();
	}

	public void testGetObjectSunnyDay() throws Exception {
		new AbstractScalarMockTemplate(BeanFactory.class) {
			public void setupExpectations(MockControl mockControl, Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				factory.containsBean(TARGET_BEAN_NAME);
				mockControl.setReturnValue(true);
				factory.getBean(TARGET_BEAN_NAME);
				mockControl.setReturnValue(TARGET_BEAN);
			}
			public void doTest(Object mockObject) throws Exception {
				BeanFactory factory = (BeanFactory) mockObject;
				BeanReferenceFactoryBean bean = new BeanReferenceFactoryBean();
				bean.setTargetBeanName(TARGET_BEAN_NAME);
				bean.setBeanFactory(factory);
				assertSame(TARGET_BEAN, bean.getObject());
			}
		}.test();
	}

}
