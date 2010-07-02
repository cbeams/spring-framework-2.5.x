/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Keith Donald
 */
public class ReflectiveVisitorHelperTests extends TestCase {

	private ReflectiveVisitorHelper support;

	public void setUp() {
		support = new ReflectiveVisitorHelper();
	}

	public void testDirectDefaultLookup() {
		ensureVisit(new DirectMockVisitor(), new ArrayList());
	}

	public void testDirectDefaultLookupCache() {
		DirectMockVisitor visitor = new DirectMockVisitor();
		ensureVisit(visitor, new ArrayList());
		visitor.visited = false;
		ensureVisit(visitor, new ArrayList());
	}

	public void testSuperClassDefaultLookup() {
		ensureVisit(new SuperClassMockVisitor(), new ArrayList());
	}

	public void testPackagePrivateDefaultLookup() {
		ensureVisit(new PackagePrivateMockVisitor(), new ArrayList());
	}

	public void testSuperClassSuperInterfaceDefaultLookup() {
		ensureVisit(new SuperClassSuperInterfaceMockVisitor(), new ArrayList());
	}

	public void testInterfaceDefaultLookup() {
		ensureVisit(new InterfaceMockVisitor(), new ArrayList());
	}

	public void testInnerClassArgument() {
		Map.Entry entry = new Map.Entry() {
			public Object getKey() {
				return null;
			}

			public Object getValue() {
				return null;
			}

			public Object setValue(Object value) {
				return null;
			}
		};
		ensureVisit(new DirectMockVisitor(), entry);
	}

	public void testInheritedValueLookup() {
		ensureVisit(new InheritedArgMockVisitor(), new ArrayList());
	}

	public void testNullArgument() {
		ensureVisit(new NullVisitor(), null);
	}

	public void testInvisibleClassVisitor() {
		ensureVisit(new InvisibleClassVisitor(), new ArrayList());
	}

	public void testNullVisitor() {
		try {
			support.invokeVisit(null, new Object());
			fail("null visitor was accepted");
		}
		catch (IllegalArgumentException e) {
			// correct behavior.
		}
		catch (Exception e) {
			fail("null visitor was not handled properly");
		}
	}

	private void ensureVisit(AbstractMockVisitor visitor, Object argument) {
		support.invokeVisit(visitor, argument);
		assertTrue("Visitor was not visited!", visitor.visited);
	}

	public abstract class AbstractMockVisitor {
		boolean visited;
	}

	public class DirectMockVisitor extends AbstractMockVisitor {
		public void visit(ArrayList value) {
			visited = true;
		}

		public void visit(Map.Entry entry) {
			visited = true;
		}
	}

	public class InheritedArgMockVisitor extends AbstractMockVisitor {
		public void visit(Object value) {
			visited = true;
		}
	}

	public class SuperClassMockVisitor extends AbstractMockVisitor {
		public void visit(AbstractList value) {
			visited = true;
		}
	}

	public class SuperClassSuperInterfaceMockVisitor extends AbstractMockVisitor {
		public void visit(Collection value) {
			visited = true;
		}
	}

	public class InterfaceMockVisitor extends AbstractMockVisitor {
		public void visit(List value) {
			visited = true;
		}
	}

	public class PackagePrivateMockVisitor extends AbstractMockVisitor {
		void visit(ArrayList value) {
			visited = true;
		}
	}

	private class InvisibleClassVisitor extends AbstractMockVisitor {
		public void visit(ArrayList list) {
			visited = true;
		}
	}

	public class NullVisitor extends AbstractMockVisitor {
		public void visitNull() {
			visited = true;
		}
	}

}
