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

import java.util.Collection;
import java.util.Iterator;

import org.springframework.rules.Closure;
import org.springframework.rules.Constraint;
import org.springframework.rules.Generator;
import org.springframework.rules.factory.Closures;

/**
 * Convenience utility class which provides a number of algorithms involving
 * functor objects such as predicates.
 * 
 * @author Keith Donald
 */
public class Algorithms {
    private Closures functions = Closures.instance();

    private static final Algorithms INSTANCE = new Algorithms();

    public Algorithms() {
    }

    public static Algorithms instance() {
        return INSTANCE;
    }

    /**
     * Returns true if any elements in the given collection meet the specified
     * predicate condition.
     * 
     * @param collection
     * @param constraint
     * @return true or false
     */
    public boolean areAnyTrue(Collection collection, Constraint constraint) {
        return findFirst(collection, constraint) != null;
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
        for (Iterator i = collection.iterator(); i.hasNext();) {
            Object o = i.next();
            if (constraint.test(o)) { return o; }
        }
        return null;
    }

    public void forEach(Collection collection, Closure closure) {
        forEach(collection.iterator(), closure);
    }

    public void forEach(Iterator it, Closure closure) {
        new IteratorGeneratorAdapter(it).generate(closure);
    }

    public Generator select(final Generator generator,
            final Constraint constraint) {
        return new Generator() {
            public void generate(Closure procedure) {
                generator.generate(functions.constrain(procedure, constraint));
            }
        };
    }

}