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

package org.springframework.core;

import junit.framework.TestCase;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class BridgeMethodResolverTests extends TestCase {

	public void testFindBridgedMethod() throws Exception {
		Method unbridged = MyFoo.class.getDeclaredMethod("someMethod", String.class, Object.class);
		Method bridged = MyFoo.class.getDeclaredMethod("someMethod", Serializable.class, Object.class);
		Method other = MyFoo.class.getDeclaredMethod("someMethod", Integer.class, Object.class);
		assertFalse(unbridged.isBridge());
		assertTrue(bridged.isBridge());

		assertEquals("Unbridged method not returned directly", unbridged, BridgeMethodResolver.findBridgedMethod(unbridged));
		assertEquals("Incorrect bridged method returned", unbridged, BridgeMethodResolver.findBridgedMethod(bridged));
	}

	public void testIsBridgeMethodFor() throws Exception {
		Map typeParameterMap = BridgeMethodResolver.createTypeVariableMap(MyBar.class);
		Method bridged = MyBar.class.getDeclaredMethod("someMethod", String.class, Object.class);
		Method other = MyBar.class.getDeclaredMethod("someMethod", Integer.class, Object.class);
		Method bridge = MyBar.class.getDeclaredMethod("someMethod", Object.class, Object.class);
		assertTrue("Should be bridge method", BridgeMethodResolver.isBridgeMethodFor(bridge, bridged, typeParameterMap));
		assertFalse("Should not be bridge method", BridgeMethodResolver.isBridgeMethodFor(bridge, other, typeParameterMap));
	}

	public void testCreateTypeVariableMap() throws Exception {
		Map<String, Class> typeVariableMap = BridgeMethodResolver.createTypeVariableMap(MyBar.class);
		assertEquals(String.class, typeVariableMap.get("T"));
		typeVariableMap = BridgeMethodResolver.createTypeVariableMap(MyFoo.class);
		assertEquals(String.class, typeVariableMap.get("T"));
		typeVariableMap = BridgeMethodResolver.createTypeVariableMap(ExtendsEnclosing.ExtendsEnclosed.ExtendsReallyDeepNow.class);
		assertEquals(Long.class, typeVariableMap.get("R"));
		assertEquals(Integer.class, typeVariableMap.get("S"));
		assertEquals(String.class, typeVariableMap.get("T"));
	}

	public void testDoubleParameterization() throws Exception {
		Method objectBridge = MyBoo.class.getDeclaredMethod("foo", Object.class);
		Method serializableBridge = MyBoo.class.getDeclaredMethod("foo", Serializable.class);

		Method stringFoo = MyBoo.class.getDeclaredMethod("foo", String.class);
		Method integerFoo = MyBoo.class.getDeclaredMethod("foo", Integer.class);

		assertEquals("foo(String) not resolved.", stringFoo, BridgeMethodResolver.findBridgedMethod(objectBridge));
		assertEquals("foo(Integer) not resolved.", integerFoo, BridgeMethodResolver.findBridgedMethod(serializableBridge));
	}

	public void testFindBridgedMethodFromMultipleBridges() throws Exception {
		Method loadWithObjectReturn = findMethodWithReturnType("load", Object.class, SettingsDaoImpl.class);
		assertNotNull(loadWithObjectReturn);

		Method loadWithSettingsReturn = findMethodWithReturnType("load", Settings.class, SettingsDaoImpl.class);
		assertNotNull(loadWithSettingsReturn);

		assertNotSame(loadWithObjectReturn, loadWithSettingsReturn);

		Method method = SettingsDaoImpl.class.getMethod("load");
		assertNotNull(method);

		assertEquals(method, BridgeMethodResolver.findBridgedMethod(loadWithObjectReturn));
		assertEquals(method, BridgeMethodResolver.findBridgedMethod(loadWithSettingsReturn));
	}

	public void testFindBridgedMethodFromParent() throws Exception {
		Method loadFromParentBridge = SettingsDaoImpl.class.getMethod("loadFromParent");
		assertNotNull(loadFromParentBridge);
		assertTrue(loadFromParentBridge.isBridge());

		Method loadFromParent = AbstractDaoImpl.class.getMethod("loadFromParent");
		assertNotNull(loadFromParent);
		assertFalse(loadFromParent.isBridge());

		assertEquals(loadFromParent, BridgeMethodResolver.findBridgedMethod(loadFromParentBridge));
	}

	public void testWithSingleBoundParameterizedOnInstaniate() throws Exception {
		Method bridgeMethod = DelayQueue.class.getMethod("add", Object.class);
		assertTrue(bridgeMethod.isBridge());
		Method actualMethod = DelayQueue.class.getMethod("add", Delayed.class);
		assertFalse(actualMethod.isBridge());
		assertEquals(actualMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
	}

	public void testWithDoubleBoundParameterizedOnInstantiate() throws Exception {
		Method bridgeMethod = SerializableBounded.class.getMethod("boundedOperation", Object.class);
		assertTrue(bridgeMethod.isBridge());
		Method actualMethod = SerializableBounded.class.getMethod("boundedOperation", HashMap.class);
		assertFalse(actualMethod.isBridge());
		assertEquals(actualMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
	}

	private Method findMethodWithReturnType(String name, Class returnType, Class targetType) {
		Method[] methods = targetType.getMethods();
		for (Method m : methods) {
			if (m.getName().equals(name) && m.getReturnType().equals(returnType)) {
				return m;
			}
		}
		return null;
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

	interface Settings {

	}

	interface ConcreteSettings extends Settings {

	}

	interface Dao<T, S> {

		T load();

		S loadFromParent();
	}

	interface SettingsDao<T extends Settings, S> extends Dao<T, S> {

		T load();
	}

	interface ConcreteSettingsDao extends SettingsDao<ConcreteSettings, String> {

		String loadFromParent();
	}

	abstract class AbstractDaoImpl<T, S> implements Dao<T, S> {

		protected T object;

		protected S otherObject;

		protected AbstractDaoImpl(T object, S otherObject) {
			this.object = object;
			this.otherObject = otherObject;
		}

		@Transactional(readOnly = true)
		public S loadFromParent() {
			return otherObject;
		}
	}

	class SettingsDaoImpl extends AbstractDaoImpl<ConcreteSettings, String> implements ConcreteSettingsDao {


		protected SettingsDaoImpl(ConcreteSettings object) {
			super(object, "From Parent");
		}

		@Transactional(readOnly = true)
		public ConcreteSettings load() {
			return super.object;
		}
	}

	private static class MyDelayed implements Delayed {

		public long getDelay(TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

		public int compareTo(Delayed o) {
			throw new UnsupportedOperationException();
		}
	}

	private static interface Bounded<E> {
		boolean boundedOperation(E e);
	}
	private static class AbstractBounded<E> implements Bounded<E> {
		public boolean boundedOperation(E myE) {
			return true;
		}
	}

	private static class SerializableBounded<E extends HashMap & Delayed> extends AbstractBounded<E> {
		public boolean boundedOperation(E myE) {
			return false;
		}
	}
}
