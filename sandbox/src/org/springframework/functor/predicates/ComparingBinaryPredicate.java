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
package org.springframework.functor.predicates;

import java.util.Comparator;

import org.springframework.functor.BinaryPredicate;
import org.springframework.util.comparators.NullSafeComparator;

/**
 * Abstract helper superclass for binary predicates involved in comparison
 * operations.
 * 
 * @author Keith Donald
 */
public abstract class ComparingBinaryPredicate implements BinaryPredicate {
    private Comparator comparator;

    /**
     * Creates a comparing binary predicate which operates on
     * <code>Comparable</code> objects.
     */
    public ComparingBinaryPredicate() {

    }

    /**
     * Creates a comparing binary predicate which compares using the provided
     * <code>Comparator</code>.
     * 
     * @param comparator
     *            the comparator, may be null
     */
    public ComparingBinaryPredicate(Comparator comparator) {
        this.comparator = comparator;
    }

    public boolean test(Object value1, Object value2) {
        Comparator c;
        if (comparator != null) {
            c = new NullSafeComparator(comparator);
        } else {
            c = NullSafeComparator.instance();
        }
        return testCompareResult(c.compare(value1, value2));
    }

    /**
     * Template method for evaluating the compare result.
     * 
     * @param result
     *            The compare result
     * @return true or false
     */
    protected abstract boolean testCompareResult(int result);

}