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

import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.springframework.core.typefilter.AnnotationTypeFilter;
import org.springframework.core.typefilter.AssignableTypeFilter;
import org.springframework.core.typefilter.RegexPatternTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * @author Mark Fisher
 */
public class ClassPathScanningCandidateComponentProviderTests extends TestCase {

	private static final String TEST_BASE_PACKAGE = 
			ClassPathScanningCandidateComponentProviderTests.class.getPackage().getName();

	private static final boolean aspectjAnnotationsAvailable =
			ClassUtils.isPresent("org.aspectj.lang.annotation.Aspect", ClassPathScanningCandidateComponentProviderTests.class.getClassLoader());
	
	
	public void testWithDefaults() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, true);
		Set<Class> candidates = provider.findCandidateComponents();
		if (aspectjAnnotationsAvailable) {
			assertEquals(4, candidates.size());
			assertTrue(candidates.contains(ServiceInvocationCounter.class));
		}
		else {
			assertEquals(3, candidates.size());
		}
		assertTrue(candidates.contains(NamedComponent.class));
		assertTrue(candidates.contains(FooServiceImpl.class));
		assertTrue(candidates.contains(StubFooDao.class));
	}

	public void testWithBogusBasePackage() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider("bogus", true);
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(0, candidates.size());
	}
	
	public void testWithPackageExcludeFilter() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, true);
		provider.addExcludeFilter(new RegexPatternTypeFilter(Pattern.compile(TEST_BASE_PACKAGE + ".*")));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(0, candidates.size());
	}
	
	public void testWithNoFilters() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(0, candidates.size());
	}
	
	public void testWithComponentAnnotationOnly() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(2, candidates.size());
		assertTrue(candidates.contains(NamedComponent.class));
		assertTrue(candidates.contains(FooServiceImpl.class));
	}
	
	@SuppressWarnings("unchecked")
	public void testWithAspectAnnotationOnly() throws Exception {
		if (!aspectjAnnotationsAvailable) {
			assertTrue("AspectJ not on classpath: skipping @Aspect scanning test", true);
			return;
		}
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		provider.addIncludeFilter(new AnnotationTypeFilter(
				ClassUtils.forName("org.aspectj.lang.annotation.Aspect"), false));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(1, candidates.size());
		assertTrue(candidates.contains(ServiceInvocationCounter.class));		
	}

	public void testWithInterfaceType() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		provider.addIncludeFilter(new AssignableTypeFilter(FooDao.class));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(1, candidates.size());
		assertTrue(candidates.contains(StubFooDao.class));			
	}
	
	public void testWithClassType() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		provider.addIncludeFilter(new AssignableTypeFilter(MessageBean.class));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(1, candidates.size());
		assertTrue(candidates.contains(MessageBean.class));			
	}
	
	public void testWithMultipleMatchingFilters() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
		provider.addIncludeFilter(new AssignableTypeFilter(FooServiceImpl.class));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(2, candidates.size());
		assertTrue(candidates.contains(NamedComponent.class));
		assertTrue(candidates.contains(FooServiceImpl.class));
	}
	
	public void testExcludeTakesPrecedence() {
		ClassPathScanningCandidateComponentProvider provider =
				new ClassPathScanningCandidateComponentProvider(TEST_BASE_PACKAGE, false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
		provider.addIncludeFilter(new AssignableTypeFilter(FooServiceImpl.class));
		provider.addExcludeFilter(new AssignableTypeFilter(FooService.class));
		Set<Class> candidates = provider.findCandidateComponents();
		assertEquals(1, candidates.size());
		assertTrue(candidates.contains(NamedComponent.class));
		assertFalse(candidates.contains(FooServiceImpl.class));
	}
	
}
