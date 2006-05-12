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

package org.springframework.util;

import junit.framework.TestCase;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class BridgeMethodResolverTests extends TestCase {

	private ReflectionBasedBridgeMethodResolver resolver = new ReflectionBasedBridgeMethodResolver();

	public void testFindBridgedMethod() throws Exception {
		Method unbridged = MyFoo.class.getMethod("someMethod", String.class, Object.class);
		Method bridged = MyFoo.class.getMethod("someMethod", Serializable.class, Object.class);
		Method other = MyFoo.class.getMethod("someMethod", Integer.class, Object.class);
		assertFalse(unbridged.isBridge());
		assertTrue(bridged.isBridge());

		assertEquals("Unbridged method not returned directly", unbridged, this.resolver.resolveBridgeMethod(unbridged));
		assertEquals("Incorrect bridged method returned", unbridged, this.resolver.resolveBridgeMethod(bridged));
	}

	public void testIsBridgeMethodFor() throws Exception {
		Map typeParameterMap = this.resolver.createTypeVariableMap(MyBar.class);
		Method bridged = MyBar.class.getMethod("someMethod", String.class, Object.class);
		Method other = MyBar.class.getMethod("someMethod", Integer.class, Object.class);
		Method bridge = MyBar.class.getMethod("someMethod", Object.class, Object.class);
		assertTrue("Should be bridge method", this.resolver.isBridgeMethodFor(bridge, bridged, typeParameterMap));
		assertFalse("Should not be bridge method", this.resolver.isBridgeMethodFor(bridge, other, typeParameterMap));
	}


	public void testCreateTypeVariableMap() throws Exception {
		Map<String, Class> typeVariableMap = resolver.createTypeVariableMap(MyBar.class);
		assertEquals(String.class, typeVariableMap.get("T"));
		typeVariableMap = resolver.createTypeVariableMap(MyFoo.class);
		assertEquals(String.class, typeVariableMap.get("T"));
		typeVariableMap = resolver.createTypeVariableMap(ExtendsEnclosing.ExtendsEnclosed.ExtendsReallyDeepNow.class);
		assertEquals(Long.class, typeVariableMap.get("R"));
		assertEquals(Integer.class, typeVariableMap.get("S"));
		assertEquals(String.class, typeVariableMap.get("T"));
	}

	public void testDoubleParameterization() throws Exception {
		Method objectBridge = MyBoo.class.getDeclaredMethod("foo", Object.class);
		Method serializableBridge = MyBoo.class.getDeclaredMethod("foo", Serializable.class);

		Method stringFoo = MyBoo.class.getDeclaredMethod("foo", String.class);
		Method integerFoo = MyBoo.class.getDeclaredMethod("foo", Integer.class);

		assertEquals("foo(String) not resolved.", stringFoo, this.resolver.resolveBridgeMethod(objectBridge));
		assertEquals("foo(Integer) not resolved.", integerFoo, this.resolver.resolveBridgeMethod(serializableBridge));
	}

	public static interface Foo<T extends Serializable> {

		void someMethod(T theArg, Object otherArg);
	}

	public static class MyFoo implements Foo<String> {

		public void someMethod(Integer theArg, Object otherArg) {
		}

		public void someMethod(String theArg, Object otherArg) {
		}

	}

	public static abstract class Bar<T> {

		void someMethod(Map m, Object otherArg) {

		}

		void someMethod(T theArg, Map m) {

		}

		abstract void someMethod(T theArg, Object otherArg);
	}

	public static abstract class InterBar<T> extends Bar<T> {

	}

	public static class MyBar extends InterBar<String> {

		public void someMethod(String theArg, Object otherArg) {

		}

		public void someMethod(Integer theArg, Object otherArg) {

		}
	}

	public class Enclosing<T> {

		public class Enclosed<S> {

			public class ReallyDeepNow<R> {

				void someMethod(S s, T t, R r) {

				}
			}

		}
	}

	public class ExtendsEnclosing extends Enclosing<String> {

		public class ExtendsEnclosed extends Enclosed<Integer> {

			public class ExtendsReallyDeepNow extends ReallyDeepNow<Long> {

				void someMethod(Integer s, String t, Long r) {
					throw new UnsupportedOperationException();
				}
			}
		}
	}

	public interface Boo<E, T extends Serializable> {
		void foo(E e);
		void foo(T t);
	}

	public class MyBoo implements Boo<String, Integer> {

		public void foo(String e) {
			throw new UnsupportedOperationException();
		}

		public void foo(Integer t) {
			throw new UnsupportedOperationException();
		}
	}
}
