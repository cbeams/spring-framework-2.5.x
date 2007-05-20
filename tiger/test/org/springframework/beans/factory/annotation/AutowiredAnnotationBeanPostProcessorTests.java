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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
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
		assertSame(ntb, bean.getNestedTestBean());
		bf.destroySingletons();
	}

	public void testExtendedResourceInjectionWithOverriding() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		RootBeanDefinition annotatedBd = new RootBeanDefinition(ExtendedResourceInjectionBean.class);
		TestBean tb2 = new TestBean();
		annotatedBd.getPropertyValues().addPropertyValue("testBean2", tb2);
		bf.registerBeanDefinition("annotatedBean", annotatedBd);
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		NestedTestBean ntb = new NestedTestBean();
		bf.registerSingleton("nestedTestBean", ntb);

		ExtendedResourceInjectionBean bean = (ExtendedResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean());
		assertSame(tb2, bean.getTestBean2());
		assertSame(tb, bean.getTestBean3());
		assertSame(tb, bean.getTestBean4());
		assertSame(ntb, bean.getNestedTestBean());
		bf.destroySingletons();
	}

	public void testOptionalResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(OptionalResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		NestedTestBean ntb = new NestedTestBean();
		bf.registerSingleton("nestedTestBean", ntb);

		OptionalResourceInjectionBean bean = (OptionalResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean());
		assertSame(tb, bean.getTestBean2());
		assertSame(tb, bean.getTestBean3());
		assertSame(tb, bean.getTestBean4());
		assertSame(ntb, bean.getNestedTestBean());
		bf.destroySingletons();
	}

	public void testOptionalResourceInjectionWithIncompleteDependencies() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(OptionalResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);

		OptionalResourceInjectionBean bean = (OptionalResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean());
		assertSame(tb, bean.getTestBean2());
		assertSame(tb, bean.getTestBean3());
		assertNull(bean.getTestBean4());
		assertNull(bean.getNestedTestBean());
		bf.destroySingletons();
	}

	public void testOptionalResourceInjectionWithNoDependencies() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(OptionalResourceInjectionBean.class));

		OptionalResourceInjectionBean bean = (OptionalResourceInjectionBean) bf.getBean("annotatedBean");
		assertNull(bean.getTestBean());
		assertNull(bean.getTestBean2());
		assertNull(bean.getTestBean3());
		assertNull(bean.getTestBean4());
		assertNull(bean.getNestedTestBean());
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
		assertSame(ntb, bean.getNestedTestBean());
		bf.destroySingletons();
	}
	
	public void testConstructorResourceInjectionWithMultipleCandidates() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ConstructorsResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		NestedTestBean ntb = new NestedTestBean();
		bf.registerSingleton("nestedTestBean", ntb);

		ConstructorsResourceInjectionBean bean = (ConstructorsResourceInjectionBean) bf.getBean("annotatedBean");
		assertNull(bean.getTestBean3());
		assertSame(tb, bean.getTestBean4());
		assertSame(ntb, bean.getNestedTestBean());
		bf.destroySingletons();
	}

	public void testConstructorResourceInjectionWithMultipleCandidatesAndFallback() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ConstructorsResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);

		ConstructorsResourceInjectionBean bean = (ConstructorsResourceInjectionBean) bf.getBean("annotatedBean");
		assertSame(tb, bean.getTestBean3());
		assertNull(bean.getTestBean4());
		bf.destroySingletons();
	}

	public void testConstructorResourceInjectionWithMultipleCandidatesAndDefaultFallback() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ConstructorsResourceInjectionBean.class));

		ConstructorsResourceInjectionBean bean = (ConstructorsResourceInjectionBean) bf.getBean("annotatedBean");
		assertNull(bean.getTestBean3());
		assertNull(bean.getTestBean4());
		bf.destroySingletons();
	}

	public void testCustomAnnotationRequiredFieldResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationRequiredFieldResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		
		CustomAnnotationRequiredFieldResourceInjectionBean bean = 
				(CustomAnnotationRequiredFieldResourceInjectionBean) bf.getBean("customBean");
		assertSame(tb, bean.getTestBean());
		bf.destroySingletons();
	}

	public void testCustomAnnotationRequiredFieldResourceInjectionFailsWhenNoDependencyFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationRequiredFieldResourceInjectionBean.class));
		
		try {
			bf.getBean("customBean");
			fail("expected BeanCreationException; no dependency available for required field");
		}
		catch (BeanCreationException e) {
			// expected
		}
		bf.destroySingletons();
	}

	public void testCustomAnnotationRequiredFieldResourceInjectionFailsWhenMultipleDependenciesFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationRequiredFieldResourceInjectionBean.class));
		TestBean tb1 = new TestBean();
		bf.registerSingleton("testBean1", tb1);
		TestBean tb2 = new TestBean();
		bf.registerSingleton("testBean2", tb2);
		
		try {
			bf.getBean("customBean");
			fail("expected BeanCreationException; multiple beans of dependency type available");
		}
		catch (BeanCreationException e) {
			// expected
		}
		bf.destroySingletons();
	}
	
	public void testCustomAnnotationRequiredMethodResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationRequiredMethodResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		
		CustomAnnotationRequiredMethodResourceInjectionBean bean = 
				(CustomAnnotationRequiredMethodResourceInjectionBean) bf.getBean("customBean");
		assertSame(tb, bean.getTestBean());
		bf.destroySingletons();
	}
	
	public void testCustomAnnotationRequiredMethodResourceInjectionFailsWhenNoDependencyFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationRequiredMethodResourceInjectionBean.class));
		
		try {
			bf.getBean("customBean");
			fail("expected BeanCreationException; no dependency available for required method");
		}
		catch (BeanCreationException e) {
			// expected
		}
		bf.destroySingletons();
	}

	public void testCustomAnnotationRequiredMethodResourceInjectionFailsWhenMultipleDependenciesFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationRequiredMethodResourceInjectionBean.class));
		TestBean tb1 = new TestBean();
		bf.registerSingleton("testBean1", tb1);
		TestBean tb2 = new TestBean();
		bf.registerSingleton("testBean2", tb2);
		
		try {
			bf.getBean("customBean");
			fail("expected BeanCreationException; multiple beans of dependency type available");
		}
		catch (BeanCreationException e) {
			// expected
		}
		bf.destroySingletons();
	}
	
	public void testCustomAnnotationOptionalFieldResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationOptionalFieldResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		
		CustomAnnotationOptionalFieldResourceInjectionBean bean = 
				(CustomAnnotationOptionalFieldResourceInjectionBean) bf.getBean("customBean");
		assertSame(tb, bean.getTestBean3());
		assertNull(bean.getTestBean());
		assertNull(bean.getTestBean2());
		bf.destroySingletons();
	}

	public void testCustomAnnotationOptionalFieldResourceInjectionWhenNoDependencyFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationOptionalFieldResourceInjectionBean.class));
		
		CustomAnnotationOptionalFieldResourceInjectionBean bean = 
				(CustomAnnotationOptionalFieldResourceInjectionBean) bf.getBean("customBean");
		assertNull(bean.getTestBean3());
		assertNull(bean.getTestBean());
		assertNull(bean.getTestBean2());
		bf.destroySingletons();
	}

	public void testCustomAnnotationOptionalFieldResourceInjectionWhenMultipleDependenciesFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationOptionalFieldResourceInjectionBean.class));
		TestBean tb1 = new TestBean();
		bf.registerSingleton("testBean1", tb1);
		TestBean tb2 = new TestBean();
		bf.registerSingleton("testBean2", tb2);

		try {
			bf.getBean("customBean");
			fail("expected BeanCreationException; multiple beans of dependency type available");
		}
		catch (BeanCreationException e) {
			// expected
		}
		bf.destroySingletons();
	}

	public void testCustomAnnotationOptionalMethodResourceInjection() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationOptionalMethodResourceInjectionBean.class));
		TestBean tb = new TestBean();
		bf.registerSingleton("testBean", tb);
		
		CustomAnnotationOptionalMethodResourceInjectionBean bean = 
				(CustomAnnotationOptionalMethodResourceInjectionBean) bf.getBean("customBean");
		assertSame(tb, bean.getTestBean3());
		assertNull(bean.getTestBean());
		assertNull(bean.getTestBean2());
		bf.destroySingletons();
	}

	public void testCustomAnnotationOptionalMethodResourceInjectionWhenNoDependencyFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationOptionalMethodResourceInjectionBean.class));
		
		CustomAnnotationOptionalMethodResourceInjectionBean bean = 
				(CustomAnnotationOptionalMethodResourceInjectionBean) bf.getBean("customBean");
		assertNull(bean.getTestBean3());
		assertNull(bean.getTestBean());
		assertNull(bean.getTestBean2());
		bf.destroySingletons();
	}

	public void testCustomAnnotationOptionalMethodResourceInjectionWhenMultipleDependenciesFound() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setAutowiredAnnotationType(MyAutowired.class);
		bpp.setRequiredParameterName("optional");
		bpp.setRequiredParameterValue(false);
		bpp.setBeanFactory(bf);
		bf.addBeanPostProcessor(bpp);
		bf.registerBeanDefinition("customBean", 
				new RootBeanDefinition(CustomAnnotationOptionalMethodResourceInjectionBean.class));
		TestBean tb1 = new TestBean();
		bf.registerSingleton("testBean1", tb1);
		TestBean tb2 = new TestBean();
		bf.registerSingleton("testBean2", tb2);

		try {
			bf.getBean("customBean");
			fail("expected BeanCreationException; multiple beans of dependency type available");
		}
		catch (BeanCreationException e) {
			// expected
		}
		bf.destroySingletons();
	}


	public static class ResourceInjectionBean {

		@Autowired(required=false)
		private TestBean testBean;

		private TestBean testBean2;

		@Autowired
		public void setTestBean2(TestBean testBean2) {
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

		public ExtendedResourceInjectionBean() {
		}

		@Autowired
		public void setTestBean2(TestBean testBean2) {
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


	public static class OptionalResourceInjectionBean extends ResourceInjectionBean {

		@Autowired(required=false)
		protected ITestBean testBean3;

		private NestedTestBean nestedTestBean;

		private ITestBean testBean4;

		public OptionalResourceInjectionBean() {
		}

		@Autowired(required=false)
		public void setTestBean2(TestBean testBean2) {
			super.setTestBean2(testBean2);
		}

		@Autowired(required=false)
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
		public void setTestBean2(TestBean testBean2) {
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


	public static class ConstructorsResourceInjectionBean {

		protected ITestBean testBean3;

		private ITestBean testBean4;

		private NestedTestBean nestedTestBean;

		public ConstructorsResourceInjectionBean() {
		}

		@Autowired(required=false)
		public ConstructorsResourceInjectionBean(ITestBean testBean3) {
			this.testBean3 = testBean3;
		}

		@Autowired(required=false)
		public ConstructorsResourceInjectionBean(ITestBean testBean4, NestedTestBean nestedTestBean) {
			this.testBean4 = testBean4;
			this.nestedTestBean = nestedTestBean;
		}

		public ConstructorsResourceInjectionBean(NestedTestBean nestedTestBean) {
			throw new UnsupportedOperationException();
		}

		public ConstructorsResourceInjectionBean(ITestBean testBean3, ITestBean testBean4, NestedTestBean nestedTestBean) {
			throw new UnsupportedOperationException();
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


	public static class CustomAnnotationRequiredFieldResourceInjectionBean {
		
		@MyAutowired(optional=false)
		private TestBean testBean;
		
		public TestBean getTestBean() {
			return testBean;
		}
	}
	

	public static class CustomAnnotationRequiredMethodResourceInjectionBean {

		private TestBean testBean;
		
		@MyAutowired(optional=false)
		public void setTestBean(TestBean testBean) {
			this.testBean = testBean;
		}

		public TestBean getTestBean() {
			return testBean;
		}
	}


	public static class CustomAnnotationOptionalFieldResourceInjectionBean extends ResourceInjectionBean {

		@MyAutowired(optional=true)
		private TestBean testBean3;

		public TestBean getTestBean3() {
			return testBean3;
		}
	}
	

	public static class CustomAnnotationOptionalMethodResourceInjectionBean extends ResourceInjectionBean {

		private TestBean testBean3;
		
		@MyAutowired(optional=true)
		protected void setTestBean3(TestBean testBean3) {
			this.testBean3 = testBean3;
		}

		public TestBean getTestBean3() {
			return testBean3;
		}
	}


	@Target({ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MyAutowired {

		boolean optional() default false;
	}

}
