/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.rules.Closure;
import org.springframework.rules.Constraint;
import org.springframework.rules.ProcessTemplate;
import org.springframework.rules.closure.Block;
import org.springframework.rules.factory.Closures;

/**
 * Convenience utility class which provides a number of algorithms involving
 * functor objects such as predicates.
 *
 * @author Keith Donald
 */
public class Algorithms {

	private static final Algorithms INSTANCE = new Algorithms();

	private Closures closures = Closures.instance();

	public static Algorithms instance() {
		return INSTANCE;
	}

	public boolean any(Collection collection, Constraint constraint) {
		return any(collection.iterator(), constraint);
	}

	/**
	 * Returns true if any elements in the given collection meet the specified
	 * predicate condition.
	 *
	 * @param collection
	 * @param constraint
	 * @return true or false
	 */
	public boolean any(Iterator it, Constraint constraint) {
		return findFirst(it, constraint) != null;
	}

	public boolean all(Collection collection, Constraint constraint) {
		return all(collection.iterator(), constraint);
	}

	/**
	 * Returns true if any elements in the given collection meet the specified
	 * predicate condition.
	 *
	 * @param collection
	 * @param constraint
	 * @return true or false
	 */
	public boolean all(Iterator it, Constraint constraint) {
		while (it.hasNext()) {
			if (!constraint.test(it.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find the first element in the collection matching the specified unary
	 * predicate.
	 *
	 * @param collection
	 *            the collection
	 * @param constraint
	 *            the predicate
	 * @return The first object match, or null if no match
	 */
	public Object findFirst(Collection collection, Constraint constraint) {
		return findFirst(collection.iterator(), constraint);
	}

	/**
	 * Find the first element in the collection matching the specified unary
	 * predicate.
	 *
	 * @param collection
	 *            the collection
	 * @param constraint
	 *            the predicate
	 * @return The first object match, or null if no match
	 */
	public Object findFirst(Iterator it, Constraint constraint) {
		while (it.hasNext()) {
			Object element = it.next();
			if (constraint.test(element)) {
				return element;
			}
		}
		return null;
	}

	public Collection findAll(Collection collection, Constraint constraint) {
		return findAll(collection.iterator(), constraint);
	}

	public Collection findAll(Iterator it, final Constraint constraint) {
		final Collection results = new ArrayList();
		ProcessTemplate generator = closures.createFilteredGenerator(
				new IteratorElementGenerator(it), constraint);
		generator.run(new Block() {
			protected void handle(Object element) {
				results.add(element);
			}
		});
		return results;
	}

	public void forEach(Collection collection, Closure closure) {
		forEach(collection.iterator(), closure);
	}

	public void forEach(Iterator it, Closure closure) {
		new IteratorElementGenerator(it).run(closure);
	}

}