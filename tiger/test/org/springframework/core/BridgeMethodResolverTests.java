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

package org.springframework.core;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class BridgeMethodResolverTests extends TestCase {

	public void testFindBridgedMethod() throws Exception {
		Method unbridged = MyFoo.class.getDeclaredMethod("someMethod", String.class, Object.class);
		Method bridged = MyFoo.class.getDeclaredMethod("someMethod", Serializable.class, Object.class);
		assertFalse(unbridged.isBridge());
		assertTrue(bridged.isBridge());

		assertEquals("Unbridged method not returned directly", unbridged, BridgeMethodResolver.findBridgedMethod(unbridged));
		assertEquals("Incorrect bridged method returned", unbridged, BridgeMethodResolver.findBridgedMethod(bridged));
	}

	public void testFindBridgedMethodInHierarchy() throws Exception {
		Method unbridged = DateAdder.class.getMethod("add", Date.class);
		Method bridged = DateAdder.class.getMethod("add", Object.class);
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

	public void testWithGenericParameter() throws Exception {
		Method[] methods = StringGenericParameter.class.getMethods();
		Method bridgeMethod = null;
		Method bridgedMethod = null;
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if ("getFor".equals(method.getName()) && !method.getParameterTypes()[0].equals(Integer.class)) {
				if (method.getReturnType().equals(Object.class)) {
					bridgeMethod = method;
				}
				else {
					bridgedMethod = method;
				}
			}
		}
		assertNotNull("bridgedMethod should not be null", bridgedMethod);
		assertNotNull("bridgeMethod should not be null", bridgeMethod);
		assertTrue(bridgeMethod.isBridge());
		assertFalse(bridgedMethod.isBridge());
		assertEquals(bridgedMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
	}

	public void testOnAllMethods() throws Exception {
		Method[] methods = StringList.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			assertNotNull(BridgeMethodResolver.findBridgedMethod(method));
		}
	}

	public void testSPR2583() throws Exception {
		Method bridgedMethod = MessageBroadcasterImpl.class.getMethod("receive", MessageEvent.class);
		assertFalse(bridgedMethod.isBridge());
		Method bridgeMethod = MessageBroadcasterImpl.class.getMethod("receive", Event.class);
		assertTrue(bridgeMethod.isBridge());

		Method otherMethod = MessageBroadcasterImpl.class.getMethod("receive", NewMessageEvent.class);
		assertFalse(otherMethod.isBridge());

		Map typeVariableMap = BridgeMethodResolver.createTypeVariableMap(MessageBroadcasterImpl.class);
		assertFalse("Match identified incorrectly", BridgeMethodResolver.isBridgeMethodFor(bridgeMethod, otherMethod, typeVariableMap));
		assertTrue("Match not found correctly", BridgeMethodResolver.isBridgeMethodFor(bridgeMethod, bridgedMethod, typeVariableMap));

		assertEquals(bridgedMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
	}

	public void testSPR2454() throws Exception {
		Map typeVariableMap = BridgeMethodResolver.createTypeVariableMap(YourHomer.class);
		assertEquals(AbstractBounded.class, typeVariableMap.get("L"));
	}

	public void testSPR2603() throws Exception {
		Method objectBridge = YourHomer.class.getDeclaredMethod("foo", Bounded.class);

		Method abstractBoundedFoo = YourHomer.class.getDeclaredMethod("foo", AbstractBounded.class);

		Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(objectBridge);

		assertEquals("foo(AbstractBounded) not resolved.", abstractBoundedFoo, bridgedMethod);
	}

	public void testSPR2648() throws Exception {
		Method bridgeMethod = GenericSqlMapIntegerDao.class.getDeclaredMethod("saveOrUpdate", Object.class);
		assertTrue(bridgeMethod.isBridge());

		Method bridgedMethod = GenericSqlMapIntegerDao.class.getDeclaredMethod("saveOrUpdate", Integer.class);
		assertFalse(bridgedMethod.isBridge());

		assertEquals(bridgedMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
	}

	public void testSPR2763() throws Exception {
		Method bridgedMethod = AbstractDao.class.getDeclaredMethod("save", Object.class);
		assertFalse(bridgedMethod.isBridge());

		Method bridgeMethod = UserDaoImpl.class.getDeclaredMethod("save", User.class);
		assertTrue(bridgeMethod.isBridge());

		assertEquals(bridgedMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
	}

	public void testSPR3041() throws Exception {
		Method bridgedMethod = BusinessDao.class.getDeclaredMethod("save", Business.class);
		assertNotNull(bridgedMethod);
		assertFalse(bridgedMethod.isBridge());

		Method bridgeMethod = BusinessDao.class.getDeclaredMethod("save", Object.class);
		assertNotNull(bridgeMethod);
		assertTrue(bridgeMethod.isBridge());

		assertEquals(bridgedMethod, BridgeMethodResolver.findBridgedMethod(bridgeMethod));
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


	public interface Adder<T> {

		void add(T item);
	}


	public abstract class AbstractDateAdder implements Adder<Date> {

		public abstract void add(Date date);
	}


	public class DateAdder extends AbstractDateAdder {

		@Override
		public void add(Date date) {
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


	private static interface GenericParameter<T> {

		T getFor(Class<T> cls);
	}


	private static class StringGenericParameter implements GenericParameter<String> {

		public String getFor(Class<String> cls) {
			return "foo";
		}

		public String getFor(Integer integer) {
			return "foo";
		}
	}


	private static class StringList implements List<String> {

		public int size() {
			throw new UnsupportedOperationException();
		}

		public boolean isEmpty() {
			throw new UnsupportedOperationException();
		}

		public boolean contains(Object o) {
			throw new UnsupportedOperationException();
		}

		public Iterator<String> iterator() {
			throw new UnsupportedOperationException();
		}

		public Object[] toArray() {
			throw new UnsupportedOperationException();
		}

		public <T> T[] toArray(T[] a) {
			throw new UnsupportedOperationException();
		}

		public boolean add(String o) {
			throw new UnsupportedOperationException();
		}

		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean containsAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends String> c) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(int index, Collection<? extends String> c) {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			throw new UnsupportedOperationException();
		}

		public String get(int index) {
			throw new UnsupportedOperationException();
		}

		public String set(int index, String element) {
			throw new UnsupportedOperationException();
		}

		public void add(int index, String element) {
			throw new UnsupportedOperationException();
		}

		public String remove(int index) {
			throw new UnsupportedOperationException();
		}

		public int indexOf(Object o) {
			throw new UnsupportedOperationException();
		}

		public int lastIndexOf(Object o) {
			throw new UnsupportedOperationException();
		}

		public ListIterator<String> listIterator() {
			throw new UnsupportedOperationException();
		}

		public ListIterator<String> listIterator(int index) {
			throw new UnsupportedOperationException();
		}

		public List<String> subList(int fromIndex, int toIndex) {
			throw new UnsupportedOperationException();
		}
	}


	public interface Event {

		int getPriority();
	}


	public class GenericEvent implements Event {

		private int priority;

		public int getPriority() {
			return priority;
		}

		/**
		 * Constructor that takes an event priority
		 */
		public GenericEvent(int priority) {
			this.priority = priority;
		}

		/**
		 * Default Constructor
		 */
		public GenericEvent() {
		}
	}


	public interface UserInitiatedEvent {

		/**
		 * returns the user who initiator this event
		 * @return
		 */
		//public Session getInitiatorSession();
	}


	public abstract class BaseUserInitiatedEvent extends GenericEvent implements UserInitiatedEvent {

	}


	public class MessageEvent extends BaseUserInitiatedEvent {

	}


	public interface Channel<E extends Event> {

		void send(E event);

		void subscribe(final Receiver<E> receiver, Class<E> event);

		void unsubscribe(final Receiver<E> receiver, Class<E> event);
	}


	public interface Broadcaster {

	}


	public interface EventBroadcaster extends Broadcaster {

		public void subscribe();

		public void unsubscribe();

		public void setChannel(Channel channel);
	}


	public class GenericBroadcasterImpl implements Broadcaster {

	}


	public abstract class GenericEventBroadcasterImpl<T extends Event> extends GenericBroadcasterImpl
					implements EventBroadcaster, BeanNameAware {

		private Class<T>[] subscribingEvents;

		private Channel<T> channel;

		/**
		 * Abstract method to retrieve instance of subclass
		 *
		 * @return receiver instance
		 */
		public abstract Receiver<T> getInstance();

		public void setChannel(Channel channel) {
			this.channel = channel;
		}

		private String beanName;

		public void setBeanName(String name) {
			this.beanName = name;
		}

		public void subscribe() {

		}

		public void unsubscribe() {

		}

		public GenericEventBroadcasterImpl(Class<? extends T>... events) {

		}
	}


	public interface Receiver<E extends Event> {

		void receive(E event);
	}


	public interface MessageBroadcaster extends Receiver<MessageEvent> {

	}


	public class RemovedMessageEvent extends MessageEvent {

	}


	public class NewMessageEvent extends MessageEvent {

	}


	public class ModifiedMessageEvent extends MessageEvent {

	}


	public class MessageBroadcasterImpl extends GenericEventBroadcasterImpl<MessageEvent>
					implements MessageBroadcaster {

		public MessageBroadcasterImpl() {
			super(NewMessageEvent.class);
		}

		public void receive(MessageEvent event) {
			throw new UnsupportedOperationException("should not be called, use subclassed events");
		}

		public void receive(NewMessageEvent event) {
		}

		@Override
		public Receiver<MessageEvent> getInstance() {
			return null;
		}

		public void receive(RemovedMessageEvent event) {
		}

		public void receive(ModifiedMessageEvent event) {
		}
	}


	//-----------------------------
	// SPR-2454 Test Classes
	//-----------------------------

	public interface SimpleGenericRepository<T> {

		public Class<T> getPersistentClass();


		List<T> findByQuery();


		List<T> findAll();


		T refresh(T entity);


		T saveOrUpdate(T entity);


		void delete(Collection<T> entities);
	}


	public interface RepositoryRegistry {

		<T> SimpleGenericRepository<T> getFor(Class<T> entityType);
	}


	public class SettableRepositoryRegistry<R extends SimpleGenericRepository<?>>
					implements RepositoryRegistry, InitializingBean {

		protected void injectInto(R rep) {
		}

		public void register(R rep) {
		}

		public void register(R... reps) {
		}

		public void setRepos(R... reps) {
		}

		public <T> SimpleGenericRepository<T> getFor(Class<T> entityType) {
			return null;
		}

		public void afterPropertiesSet() throws Exception {
		}
	}


	public interface ConvenientGenericRepository<T, ID extends Serializable> extends SimpleGenericRepository<T> {

		T findById(ID id, boolean lock);

		List<T> findByExample(T exampleInstance);

		void delete(ID id);

		void delete(T entity);
	}


	public class GenericHibernateRepository<T, ID extends Serializable> extends HibernateDaoSupport
					implements ConvenientGenericRepository<T, ID> {

		/**
		 * @param c Mandatory. The domain class this repository is responsible for.
		 */
		// Since it is impossible to determine the actual type of a type
		// parameter (!), we resort to requiring the caller to provide the
		// actual type as parameter, too.
		// Not set in a constructor to enable easy CGLIB-proxying (passing
		// constructor arguments to Spring AOP proxies is quite cumbersome).
		public void setPersistentClass(Class<T> c) {
		}

		public Class<T> getPersistentClass() {
			return null;
		}

		public T findById(ID id, boolean lock) {
			return null;
		}

		public List<T> findAll() {
			return null;
		}

		public List<T> findByExample(T exampleInstance) {
			return null;
		}

		public List<T> findByQuery() {
			return null;
		}

		public T saveOrUpdate(T entity) {
			return null;
		}

		public void delete(T entity) {
		}

		public T refresh(T entity) {
			return null;
		}

		public void delete(ID id) {
		}

		public void delete(Collection<T> entities) {
		}
	}


	public class HibernateRepositoryRegistry extends SettableRepositoryRegistry<GenericHibernateRepository<?, ?>> {

		public void injectInto(GenericHibernateRepository<?, ?> rep) {
		}

		public <T> GenericHibernateRepository<T, ?> getFor(Class<T> entityType) {
			return null;
		}
	}


	//-------------------
	// SPR-2603 classes
	//-------------------

	public interface Homer<E> {

		void foo(E e);
	}


	public class MyHomer<T extends Bounded<T>, L extends T> implements Homer<L> {

		public void foo(L t) {
			throw new UnsupportedOperationException();
		}
	}


	public class YourHomer<T extends AbstractBounded<T>, L extends T> extends MyHomer<T, L> {

		public void foo(L t) {
			throw new UnsupportedOperationException();
		}
	}


	public interface GenericDao<T> {

		public void saveOrUpdate(T t);
	}


	public interface ConvenienceGenericDao<T> extends GenericDao<T> {
	}


	public class GenericSqlMapDao<T extends Serializable> implements ConvenienceGenericDao<T> {

		public void saveOrUpdate(T t) {
			throw new UnsupportedOperationException();
		}
	}


	public class GenericSqlMapIntegerDao<T extends Integer> extends GenericSqlMapDao<T> {

		public void saveOrUpdate(T t) {
		}
	}


	public class Permission {
	}


	public class User {
	}


	public interface UserDao {

		@Transactional
		void save(User user);

		@Transactional
		void save(Permission perm);
	}


	public abstract class AbstractDao<T> {

		public void save(T t) {
		}
	}


	public class UserDaoImpl extends AbstractDao<User> implements UserDao {

		public void save(Permission perm) {
		}
	}


	public class Business<T> {
	}


	public class BusinessGenericDao<T, PK extends Serializable> {

		public void save(T object) {
			// Do nothing;
		}
	}


	public class BusinessDao extends BusinessGenericDao<Business<?>, Long> {

    public void save(Business<?> business) {
    }
	}
}
