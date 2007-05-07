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

package org.springframework.beans.factory.annotation;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Juergen Hoeller
 */
public class AutowiredAnnotationBeanPostProcessorTests extends TestCase {

	public void testResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);

		ResourceInjectionBean bean = (ResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean());
		assertSame(tb, bean.getTestBean2());
		bf.destroySingletons();
	}

	public void testExtendedResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ExtendedResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		NestedTestBean ntb = new NestedTestBean();
		bf.registerSingleton("nestedTestBean", ntb);

		ExtendedResourceInjectionBean bean = (ExtendedResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean());
		assertSame(tb, bean.getTestBean2());
		assertSame(tb, bean.getTestBean3());
		assertSame(tb, bean.getTestBean4());
		bf.destroySingletons();
	}

	public void testConstructorResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ConstructorResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		NestedTestBean ntb = new NestedTestBean();
		bf.registerSingleton("nestedTestBean", ntb);

		ConstructorResourceInjectionBean bean = (ConstructorResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean());
		assertSame(tb, bean.getTestBean2());
		assertSame(tb, bean.getTestBean3());
		assertSame(tb, bean.getTestBean4());
		bf.destroySingletons();
	}


	public static class ResourceInjectionBean {

		@Autowired
		private TestBean testBean;

		private TestBean testBean2;

		@Autowired
		protected void setTestBean2(TestBean testBean2) {
			if (this.testBean2 != null) {
				throw new IllegalStateException("Already called");
			}
			this.testBean2 = testBean2;
		}

		public TestBean getTestBean() {
			return testBean;
		}

		public TestBean getTestBean2() {
			return testBean2;
		}
	}


	public static class ExtendedResourceInjectionBean extends ResourceInjectionBean {

		@Autowired
		protected ITestBean testBean3;

		private NestedTestBean nestedTestBean;

		private ITestBean testBean4;


		@Autowired
		public ExtendedResourceInjectionBean() {
		}


		@Autowired
		protected void setTestBean2(TestBean testBean2) {
			super.setTestBean2(testBean2);
		}

		@Autowired
		private void inject(ITestBean testBean4, NestedTestBean nestedTestBean) {
			this.testBean4 = testBean4;
			this.nestedTestBean = nestedTestBean;
		}

		public ITestBean getTestBean3() {
			return testBean3;
		}

		public ITestBean getTestBean4() {
			return testBean4;
		}

		public NestedTestBean getNestedTestBean() {
			return nestedTestBean;
		}
	}


	public static class ConstructorResourceInjectionBean extends ResourceInjectionBean {

		@Autowired
		protected ITestBean testBean3;

		private ITestBean testBean4;

		private NestedTestBean nestedTestBean;


		public ConstructorResourceInjectionBean() {
			throw new UnsupportedOperationException();
		}

		public ConstructorResourceInjectionBean(ITestBean testBean3) {
			throw new UnsupportedOperationException();
		}

		@Autowired
		public ConstructorResourceInjectionBean(ITestBean testBean4, NestedTestBean nestedTestBean) {
			this.testBean4 = testBean4;
			this.nestedTestBean = nestedTestBean;
		}

		public ConstructorResourceInjectionBean(NestedTestBean nestedTestBean) {
			throw new UnsupportedOperationException();
		}

		public ConstructorResourceInjectionBean(ITestBean testBean3, ITestBean testBean4, NestedTestBean nestedTestBean) {
			throw new UnsupportedOperationException();
		}


		@Autowired
		protected void setTestBean2(TestBean testBean2) {
			super.setTestBean2(testBean2);
		}

		public ITestBean getTestBean3() {
			return testBean3;
		}

		public ITestBean getTestBean4() {
			return testBean4;
		}

		public NestedTestBean getNestedTestBean() {
			return nestedTestBean;
		}
	}

}
