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

package org.springframework.context.annotation;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.AnnotationConfigUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * @author Mark Fisher
 */
public class ClassPathBeanDefinitionScannerTests extends TestCase {

	private static final String BASE_PACKAGE = 
			ClassPathBeanDefinitionScannerTests.class.getPackage().getName();
	

	public void testSimpleScanWithDefaultFiltersAndPostProcessors() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(8, beanCount);
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testSimpleScanWithDefaultFiltersAndNoPostProcessors() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(5, beanCount);
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
	}

	public void testSimpleScanWithDefaultFiltersAndOverriddenBean() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("stubFooDao", new RootBeanDefinition(TestBean.class));
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(5, beanCount);
		assertEquals(6, context.getBeanDefinitionCount());
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
	}

	public void testSimpleScanWithDefaultFiltersAndOverriddenEqualNamedBean() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("myNamedDao", new RootBeanDefinition(NamedStubDao.class));
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(4, beanCount);
		assertEquals(5, context.getBeanDefinitionCount());
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
	}

	public void testSimpleScanWithDefaultFiltersAndOverriddenCompatibleNamedBean() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("myNamedDao", new RootBeanDefinition(NamedStubDao.class, false));
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(4, beanCount);
		assertEquals(5, context.getBeanDefinitionCount());
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
	}

	public void testSimpleScanWithDefaultFiltersAndOverriddenIncompatibleNamedBean() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("myNamedDao", new RootBeanDefinition(TestBean.class));
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		try {
			scanner.scan(BASE_PACKAGE);
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
			assertTrue(ex.getMessage().contains("myNamedDao"));
			assertTrue(ex.getMessage().contains(NamedStubDao.class.getName()));
			assertTrue(ex.getMessage().contains(TestBean.class.getName()));
		}
	}

	public void testCustomIncludeFilterWithoutDefaultsButIncludingPostProcessors() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
		includeFilters.add(new AnnotationTypeFilter(CustomComponent.class));
		int beanCount = scanner.scan(BASE_PACKAGE, false, null, includeFilters);
		assertEquals(4, beanCount);
		assertTrue(context.containsBean("messageBean"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testCustomIncludeFilterWithoutDefaultsAndNoPostProcessors() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
		includeFilters.add(new AnnotationTypeFilter(CustomComponent.class));
		int beanCount = scanner.scan(BASE_PACKAGE, false, null, includeFilters);
		assertEquals(4, beanCount);
		assertTrue(context.containsBean("messageBean"));
		assertFalse(context.containsBean("serviceInvocationCounter"));
		assertFalse(context.containsBean("fooServiceImpl"));
		assertFalse(context.containsBean("stubFooDao"));
		assertFalse(context.containsBean("myNamedComponent"));
		assertFalse(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}
	
	public void testCustomIncludeFilterAndDefaults() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
		includeFilters.add(new AnnotationTypeFilter(CustomComponent.class));
		int beanCount = scanner.scan(BASE_PACKAGE, true, null, includeFilters);
		assertEquals(9, beanCount);
		assertTrue(context.containsBean("messageBean"));
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testCustomAnnotationExcludeFilterAndDefaults() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
		excludeFilters.add(new AnnotationTypeFilter(Aspect.class));
		int beanCount = scanner.scan(BASE_PACKAGE, true, excludeFilters, null);
		assertEquals(7, beanCount);
		assertFalse(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testCustomAssignableTypeExcludeFilterAndDefaults() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
		excludeFilters.add(new AssignableTypeFilter(FooService.class));
		int beanCount = scanner.scan(BASE_PACKAGE, true, excludeFilters, null);
		assertEquals(7, beanCount);
		assertFalse(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testCustomAssignableTypeExcludeFilterAndDefaultsWithoutPostProcessors() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
		excludeFilters.add(new AssignableTypeFilter(FooService.class));
		int beanCount = scanner.scan(BASE_PACKAGE, true, excludeFilters, null);
		assertEquals(4, beanCount);
		assertFalse(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertFalse(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertFalse(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertFalse(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testMultipleCustomExcludeFiltersAndDefaults() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
		excludeFilters.add(new AssignableTypeFilter(FooService.class));
		excludeFilters.add(new AnnotationTypeFilter(Aspect.class));
		int beanCount = scanner.scan(BASE_PACKAGE, true, excludeFilters, null);
		assertEquals(6, beanCount);
		assertFalse(context.containsBean("fooServiceImpl"));
		assertFalse(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	public void testCustomBeanNameGenerator() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setBeanNameGenerator(new TestBeanNameGenerator());
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(8, beanCount);
		assertFalse(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("fooService"));
		assertTrue(context.containsBean("serviceInvocationCounter"));
		assertTrue(context.containsBean("stubFooDao"));
		assertTrue(context.containsBean("myNamedComponent"));
		assertTrue(context.containsBean("myNamedDao"));
		assertTrue(context.containsBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		assertTrue(context.containsBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
	}
	
	public void testMultipleBasePackagesWithDefaultsOnly() {
		GenericApplicationContext singlePackageContext = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner singlePackageScanner = new ClassPathBeanDefinitionScanner(singlePackageContext);
		GenericApplicationContext multiPackageContext = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner multiPackageScanner = new ClassPathBeanDefinitionScanner(multiPackageContext);
		int singlePackageBeanCount = singlePackageScanner.scan(BASE_PACKAGE);
		assertEquals(8, singlePackageBeanCount);
		int multiPackageBeanCount = multiPackageScanner.scan(
				new String[] { BASE_PACKAGE, "org.springframework.dao.annotation" });
		assertTrue(multiPackageBeanCount > singlePackageBeanCount);
	}
	
	public void testMultipleScanCalls() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(8, beanCount);
		assertEquals(beanCount, context.getBeanDefinitionCount());
		int addedBeanCount = scanner.scan("org.springframework.aop.aspectj.annotation");
		assertEquals(beanCount + addedBeanCount, context.getBeanDefinitionCount());
	}
	
	public void testBeanAutowiredWithAnnotationConfigEnabled() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setBeanNameGenerator(new TestBeanNameGenerator());
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(8, beanCount);
		context.refresh();
		FooService fooService = (FooService) context.getBean("fooService");
		assertTrue(fooService.isInitCalled());
		assertEquals("bar", fooService.foo(123));
	}

	public void testBeanNotAutowiredWithAnnotationConfigDisabled() {
		GenericApplicationContext context = new GenericApplicationContext();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		scanner.setIncludeAnnotationConfig(false);
		scanner.setBeanNameGenerator(new TestBeanNameGenerator());
		int beanCount = scanner.scan(BASE_PACKAGE);
		assertEquals(5, beanCount);
		context.refresh();
		FooService fooService = (FooService) context.getBean("fooService");
		assertFalse(fooService.isInitCalled());
		try {
			fooService.foo(123);
			fail("NullPointerException expected; fooDao should not have been set");
		}
		catch (NullPointerException e) {
			// expected
		}
	}
	
	private static class TestBeanNameGenerator extends AnnotationBeanNameGenerator {

		@Override
		public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
			String beanName = super.generateBeanName(definition, registry);
			return beanName.replace("Impl", "");
		}
	}
	
}
