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

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * 
 * @author Ramnivas Laddad
 *
 */
public class AssignableTypeFilterTests extends TestCase {
	public void testDirectMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.TestNonInheritingClass";
		ClassReader classReader = new ClassReader(classUnderTest);

		AssignableTypeFilter matchingFilter = new AssignableTypeFilter(TestNonInheritingClass.class);
		AssignableTypeFilter notMatchingFilter = new AssignableTypeFilter(TestInterface.class);
		assertFalse(notMatchingFilter.match(classReader));
		assertTrue(matchingFilter.match(classReader));
	}
	
	public void testInterfaceMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.TestInterfaceImpl";
		AssignableTypeFilter filter = new AssignableTypeFilter(TestInterface.class);
		
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertTrue(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
	
	public void testSuperClassMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeDaoLikeImpl";
		AssignableTypeFilter filter = new AssignableTypeFilter(SimpleJdbcDaoSupport.class);
		
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertTrue(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
	
	public void testInterfaceThroughSuperClassMatch() throws Exception {
		String classUnderTest = "org.springframework.core.typefilter.SomeDaoLikeImpl";
		AssignableTypeFilter filter = new AssignableTypeFilter(JdbcDaoSupport.class);
		
		ClassReader classReader = new ClassReader(classUnderTest);
		
		assertTrue(filter.match(classReader));
		ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
	}
}

// We must use a standalone set of types to ensure that no one else is loading them
// and interfere with ClassloadingAssertions.assertClassNotLoaded()
class TestNonInheritingClass {
}

interface TestInterface {
}

class TestInterfaceImpl implements TestInterface {
}

interface SomeDaoLikeInterface {
}

class SomeDaoLikeImpl extends SimpleJdbcDaoSupport implements SomeDaoLikeInterface {
}