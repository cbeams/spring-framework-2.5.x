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
package org.springframework.rules.constraint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.rules.Constraint;
import org.springframework.rules.support.Algorithms;
import org.springframework.rules.support.ClosureWithoutResult;
import org.springframework.util.Assert;

/**
 * Abstract base class for unary constraints which compose other constraints.
 * 
 * @author Keith Donald
 */
public abstract class CompoundConstraint implements Constraint,
        Serializable {
    private List constraints = new ArrayList();

    /**
     * Constructs a compound predicate with no initial members. It is expected
     * the client will call "add" to add individual constraints.
     */
    public CompoundConstraint() {

    }

    /**
     * Creates a CompoundUnaryPredicate composed of two constraints.
     * 
     * @param predicate1
     *            the first predicate
     * @param predicate2
     *            the second predicate
     */
    public CompoundConstraint(Constraint predicate1, Constraint predicate2) {
        Assert.isTrue(predicate1 != null && predicate2 != null);
        constraints.add(predicate1);
        constraints.add(predicate2);
    }

    /**
     * Creates a CompoundUnaryPredicate composed of the specified constraints.
     * 
     * @param constraints
     *            the aggregated constraints
     */
    public CompoundConstraint(Constraint[] constraints) {
        this.constraints.addAll(Arrays.asList(constraints));
    }

    /**
     * Add the specified predicate to the set of constraints aggregated by this
     * compound predicate.
     * 
     * @param predicate
     *            the predicate to add
     * @return A reference to this, to support chaining.
     */
    public CompoundConstraint add(Constraint predicate) {
        this.constraints.add(predicate);
        return this;
    }

    /**
     * Add the list of constraints to the set of constraints aggregated by this
     * compound predicate.
     * 
     * @param constraints
     *            the list of constraints to add
     * @return A reference to this, to support chaining.
     */
    public CompoundConstraint addAll(List constraints) {
        Algorithms.instance().forEach(constraints,
                new ClosureWithoutResult() {
                    public void doCall(Object o) {
                        add((Constraint)o);
                    }
                });
        return this;
    }

    public void remove(Constraint predicate) {
        constraints.remove(predicate);
    }

    public int indexOf(Constraint child) {
        return constraints.indexOf(child);
    }

    public Constraint get(int index) {
        return (Constraint)constraints.get(index);
    }

    public void copyInto(CompoundConstraint p) {
        p.constraints.clear();
        p.constraints.addAll(constraints);
    }

    public void set(int index, Constraint predicate) {
        constraints.set(index, predicate);
    }

    /**
     * Return an iterator over the aggregated constraints.
     * 
     * @return An iterator
     */
    public Iterator iterator() {
        return constraints.iterator();
    }

    /**
     * Returns the number of constraints aggregated by this compound predicate.
     * 
     * @return The size.
     */
    public int size() {
        return constraints.size();
    }

    public abstract boolean test(Object argument);

    /**
     * Utility method that validates that each predicate aggregated by this
     * compound predicate is of the specified class OR is another compound
     * predicate.
     * 
     * @param clazz
     *            the class to validate
     */
    public void validateTypeSafety(final Class clazz) {
        Constraint predicate = new Constraint() {
            public boolean test(Object o) {
                return clazz.getClass().isAssignableFrom(o.getClass());
            }
        };
        Object violator = Algorithms.instance().findFirst(constraints,
                predicate);
        if (violator != null) { throw new IllegalArgumentException(violator
                .getClass()
                + " class not allowed"); }
    }

}