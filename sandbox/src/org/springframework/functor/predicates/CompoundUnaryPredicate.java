/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.functor.predicates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.functor.Algorithms;
import org.springframework.functor.UnaryPredicate;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public abstract class CompoundUnaryPredicate implements UnaryPredicate {
    private Set predicates = new HashSet();

    public CompoundUnaryPredicate() {

    }

    public CompoundUnaryPredicate(UnaryPredicate predicate1,
            UnaryPredicate predicate2) {
        Assert.isTrue(predicate1 != null && predicate2 != null);
        predicates.add(predicate1);
        predicates.add(predicate2);
    }

    public CompoundUnaryPredicate(UnaryPredicate[] predicates) {
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public CompoundUnaryPredicate add(UnaryPredicate predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public Iterator iterator() {
        return predicates.iterator();
    }

    public abstract boolean evaluate(Object value);

    public void validateTypeSafety(final Class clazz) {
        UnaryPredicate predicate = new UnaryPredicate() {
            public boolean evaluate(Object o) {
                return o.getClass() != clazz || o.getClass() != this.getClass();
            }
        };
        Object violator = Algorithms.findFirst(predicates, predicate);
        if (violator != null) {
            throw new IllegalArgumentException(violator.getClass()
                    + " class not allowed");
        }
    }

}