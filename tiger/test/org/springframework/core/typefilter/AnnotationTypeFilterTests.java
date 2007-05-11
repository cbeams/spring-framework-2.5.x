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

package org.springframework.core.typefilter;

import java.lang.annotation.Inherited;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Ramnivas Laddad
 *
 */
public class AnnotationTypeFilterTests extends TestCase {
	
	public void testDirectAnnotationMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeComponent";
		AnnotationTypeFilter filter = new AnnotationTypeFilter(Component.class, false);
		
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertTrue(filter.match(classReader));
		
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}

	public void testInheritedAnnotationFromInterfaceDoesNotMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeSubClassOfSomeComponentInterface";
		ClassReader classReader = new ClassReader(classUnderTest);

		AnnotationTypeFilter inheritingFilter = new AnnotationTypeFilter(Component.class, true);
		AnnotationTypeFilter nonInheritingFilter = new AnnotationTypeFilter(Component.class, false);
		
		// Must fail in both cases as annotation on interfaces should not be considered a match
		assertFalse(inheritingFilter.match(classReader));
		assertFalse(nonInheritingFilter.match(classReader));
		
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
	
	public void testInheritedAnnotationFromBaseClassDoesMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeSubClassOfSomeComponent";
		ClassReader classReader = new ClassReader(classUnderTest);

		AnnotationTypeFilter inheritingFilter = new AnnotationTypeFilter(Component.class, true);
		AnnotationTypeFilter nonInheritingFilter = new AnnotationTypeFilter(Component.class, false);
		
		assertTrue(inheritingFilter.match(classReader));
		assertFalse(nonInheritingFilter.match(classReader));
		
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}

	public void testNonInheritedAnnotationDoesNotMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeSubclassOfSomeClassMarkedWithNonInheritedAnnotation";
		ClassReader classReader = new ClassReader(classUnderTest);

		AnnotationTypeFilter inheritingFilter = new AnnotationTypeFilter(NonInheritedAnnotation.class, true);
		AnnotationTypeFilter nonInheritingFilter = new AnnotationTypeFilter(NonInheritedAnnotation.class, false);
		
		// Must fail in either case as annotation isn't inherited
		assertFalse(inheritingFilter.match(classReader));
		assertFalse(nonInheritingFilter.match(classReader));
		
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
	
	public void testNonAnnotatedClassDoesntMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeNonCandidateClass";
		AnnotationTypeFilter filter = new AnnotationTypeFilter(Component.class, true);
		
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertFalse(filter.match(classReader));
		
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
}

// We must use a standalone set of types to ensure that no one else is loading them
// and interfering with ClassloadingAssertions.assertClassNotLoaded()

// Note that Component is @Inherited 
@Component
class SomeComponent {
}

@Component
interface SomeComponentInterface {
}

class SomeSubClassOfSomeComponentInterface implements SomeComponentInterface {
}

class SomeSubClassOfSomeComponent extends SomeComponent {
}
//----
@interface NonInheritedAnnotation {
	
}

@NonInheritedAnnotation
class SomeClassMarkedWithNonInheritedAnnotation {
}

class SomeSubclassOfSomeClassMarkedWithNonInheritedAnnotation extends SomeClassMarkedWithNonInheritedAnnotation {
}
//----

class SomeNonCandidateClass {
}