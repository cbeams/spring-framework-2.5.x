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

import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.io.PrintStream;

/**
 * Simple {@link List} wrapper class that allows for elements to be
 * automatically populated as they are requested. This is particularly
 * useful for data binding to {@link List Lists}, allowing for
 * elements to be created and added to the {@link List} in a "just in time"
 * fashion.
 * <p/>
 * Note: This class is not threadsafe. To create a threadsafe version use the
 * {@link java.util.Collections#synchronizedList} utility methods.
 * <p/>
 * Inspired by <code>LazyList</code> from Commons Collections.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class AutoPopulatingList implements List {

	/**
	 * The {@link List} that all operations are finally delegated to.
	 */
	private final List backingList;

	/**
	 * The {@link ElementFactory} to use to create new {@link List} elements
	 * on demand.
	 */
	private final ElementFactory elementFactory;

	/**
	 * Creates a new <code>AutoPopulatingList</code> that is backed by a standard
	 * {@link ArrayList} and adds new instances of the supplied {@link Class element Class}
	 * to the backing {@link List} on demand.
	 */
	public AutoPopulatingList(Class elementClass) {
		this(new ArrayList(), elementClass);
	}

	/**
	 * Creates a new <code>AutoPopulatingList</code> that is backed by the supplied {@link List}
	 * and adds new instances of the supplied {@link Class element Class} to the backing
	 * {@link List} on demand.
	 */
	public AutoPopulatingList(List backingList, Class elementClass) {
		this(backingList, new ReflectiveElementFactory(elementClass));
	}

	/**
	 * Creates a new <code>AutoPopulatingList</code> that is backed by a standard
	 * {@link ArrayList} and creates new elements on demand using the supplied {@link ElementFactory}.
	 */
	public AutoPopulatingList(ElementFactory elementFactory) {
		this(new ArrayList(), elementFactory);
	}

	/**
	 * Creates a new <code>AutoPopulatingList</code> that is backed by the supplied {@link List}
	 * and creates new elements on demand using the supplied {@link ElementFactory}.
	 */
	public AutoPopulatingList(List backingList, ElementFactory elementFactory) {
		Assert.notNull(backingList, "'backingList' cannot be null.");
		Assert.notNull(elementFactory, "'elementFactory' cannot be null.");
		this.backingList = backingList;
		this.elementFactory = elementFactory;
	}

	public void add(int index, Object element) {
		backingList.add(index, element);
	}

	public boolean add(Object o) {
		return backingList.add(o);
	}

	public boolean addAll(Collection c) {
		return backingList.addAll(c);
	}

	public boolean addAll(int index, Collection c) {
		return backingList.addAll(index, c);
	}

	public void clear() {
		backingList.clear();
	}

	public boolean contains(Object o) {
		return backingList.contains(o);
	}

	public boolean containsAll(Collection c) {
		return backingList.containsAll(c);
	}

	public boolean equals(Object o) {
		return backingList.equals(o);
	}

	/**
	 * Gets the element at the supplied index, creating it if there is no element at
	 * that index.
	 */
	public Object get(int index) {
		int backingListSize = this.backingList.size();

		Object element = null;
		if (index < backingListSize) {
			element = this.backingList.get(index);
			if (element == null) {
				element = this.elementFactory.createElement(index);
				this.backingList.set(index, element);
			}
		}
		else {
			for (int x = backingListSize; x < index; x++) {
				this.backingList.add(null);
			}
			element = this.elementFactory.createElement(index);
			this.backingList.add(element);
		}
		return element;
	}

	public int hashCode() {
		return backingList.hashCode();
	}

	public int indexOf(Object o) {
		return backingList.indexOf(o);
	}

	public boolean isEmpty() {
		return backingList.isEmpty();
	}

	public Iterator iterator() {
		return backingList.iterator();
	}

	public int lastIndexOf(Object o) {
		return backingList.lastIndexOf(o);
	}

	public ListIterator listIterator() {
		return backingList.listIterator();
	}

	public ListIterator listIterator(int index) {
		return backingList.listIterator(index);
	}

	public Object remove(int index) {
		return backingList.remove(index);
	}

	public boolean remove(Object o) {
		return backingList.remove(o);
	}

	public boolean removeAll(Collection c) {
		return backingList.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		return backingList.retainAll(c);
	}

	public Object set(int index, Object element) {
		return backingList.set(index, element);
	}

	public int size() {
		return backingList.size();
	}

	public List subList(int fromIndex, int toIndex) {
		return backingList.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return backingList.toArray();
	}

	public Object[] toArray(Object[] a) {
		return backingList.toArray(a);
	}

	private static class ReflectiveElementFactory implements ElementFactory {

		private final Class elementClass;

		public ReflectiveElementFactory(Class elementClass) {
			Assert.notNull(elementClass, "'elementClass' cannot be null.");
			Assert.state(!elementClass.isInterface(), "'elementClass' cannot be an interface type.");
			Assert.state(!Modifier.isAbstract(elementClass.getModifiers()), "'elementClass' cannot be an abstract class.");
			this.elementClass = elementClass;
		}

		public Object createElement(int index) {
			try {
				Constructor ctor = this.elementClass.getDeclaredConstructor((Class[]) null);

				if (!Modifier.isPublic(ctor.getModifiers()) ||
								!Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) {
					ctor.setAccessible(true);
				}
				return ctor.newInstance((Object[]) null);
			}
			catch (InstantiationException ex) {
				throw new ElementInstantiationException("Unable to instantiate element class '"
								+ this.elementClass.getName() + "'. Is it an abstract class?", ex);
			}
			catch (IllegalAccessException ex) {
				throw new ElementInstantiationException("Cannot access element class '" + this.elementClass.getName() +
								". 'Has the class definition changed? Is the constructor accessible?", ex);
			}
			catch (IllegalArgumentException ex) {
				throw new ElementInstantiationException("Illegal arguments for constructor on element class '"
								+ this.elementClass.getName() + "'", ex);
			}
			catch (InvocationTargetException ex) {
				throw new ElementInstantiationException("Constructor for element class '"
								+ this.elementClass.getName() + "' threw exception", ex.getTargetException());
			}
			catch (NoSuchMethodException ex) {
				throw new ElementInstantiationException("Element class '" + this.elementClass.getName()
								+ "' has no public no-arg constructor.", ex);
			}
		}
	}

	private static class ElementInstantiationException extends RuntimeException {

		private Throwable rootCause;

		public ElementInstantiationException(String msg) {
			super(msg);
		}

		public ElementInstantiationException(String msg, Throwable ex) {
			super(msg);
			this.rootCause = ex;
		}

		/**
		 * Print the composite message and the embedded stack trace to the specified stream.
		 *
		 * @param ps the print stream
		 */
		public void printStackTrace(PrintStream ps) {
			if (getCause() == null) {
				super.printStackTrace(ps);
			}
			else {
				ps.println(this);
				ps.print("Caused by: ");
				getCause().printStackTrace(ps);
			}
		}
	}
}
