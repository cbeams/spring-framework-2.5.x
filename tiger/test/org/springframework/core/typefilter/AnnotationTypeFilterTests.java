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
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 */
public class AnnotationTypeFilterTests extends TestCase {
	
	public void testDirectAnnotationMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.AnnotationTypeFilterTests$SomeComponent";
		AnnotationTypeFilter filter = new AnnotationTypeFilter(InheritedAnnotation.class);
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertTrue(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}

	public void testInheritedAnnotationFromInterfaceDoesNotMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.AnnotationTypeFilterTests$SomeSubClassOfSomeComponentInterface";
		ClassReader classReader = new ClassReader(classUnderTest);
		AnnotationTypeFilter filter = new AnnotationTypeFilter(InheritedAnnotation.class);
		
		// Must fail as annotation on interfaces should not be considered a match
		assertFalse(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
	
	public void testInheritedAnnotationFromBaseClassDoesMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.AnnotationTypeFilterTests$SomeSubClassOfSomeComponent";
		ClassReader classReader = new ClassReader(classUnderTest);
		AnnotationTypeFilter filter = new AnnotationTypeFilter(InheritedAnnotation.class);

		assertTrue(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}

	public void testNonInheritedAnnotationDoesNotMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.AnnotationTypeFilterTests$SomeSubclassOfSomeClassMarkedWithNonInheritedAnnotation";
		ClassReader classReader = new ClassReader(classUnderTest);

		AnnotationTypeFilter filter = new AnnotationTypeFilter(NonInheritedAnnotation.class);
		
		// Must fail as annotation isn't inherited
		assertFalse(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
	
	public void testNonAnnotatedClassDoesntMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.AnnotationTypeFilterTests$SomeNonCandidateClass";
		AnnotationTypeFilter filter = new AnnotationTypeFilter(Component.class);
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertFalse(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}


	// We must use a standalone set of types to ensure that no one else is loading them
	// and interfering with ClassloadingAssertions.assertClassNotLoaded()

	@Inherited
	private static @interface InheritedAnnotation {
	}

	@InheritedAnnotation
	private static class SomeComponent {
	}

	@InheritedAnnotation
	private static interface SomeComponentInterface {
	}

	private static class SomeSubClassOfSomeComponentInterface implements SomeComponentInterface {
	}

	private static class SomeSubClassOfSomeComponent extends SomeComponent {
	}

	private static @interface NonInheritedAnnotation {
	}

	@NonInheritedAnnotation
	private static class SomeClassMarkedWithNonInheritedAnnotation {
	}

	private static class SomeSubclassOfSomeClassMarkedWithNonInheritedAnnotation extends SomeClassMarkedWithNonInheritedAnnotation {
	}
	//----

	private static class SomeNonCandidateClass {
	}

}
