/*
 *  Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.springframework.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A {@link Comparator}for {@link Boolean}objects that can sort either true or
 * false first.
 */
public final class BooleanComparator implements Comparator, Serializable {
    private static final BooleanComparator TRUE_FIRST = new BooleanComparator(
            true);

    private static final BooleanComparator FALSE_FIRST = new BooleanComparator(
            false);

    private boolean trueFirst = false;

    /**
     * Returns a BooleanComparator instance that sorts <code>true</code>
     * values before <code>false</code> values.
     * 
     * @return the true first singleton BooleanComparator
     */
    public static BooleanComparator trueFirst() {
        return TRUE_FIRST;
    }

    /**
     * Returns a BooleanComparator instance that sorts <code>false</code>
     * values before <code>true</code> values.
     * 
     * @return the false first singleton BooleanComparator
     */
    public static BooleanComparator falseFirst() {
        return FALSE_FIRST;
    }

    /**
     * Returns a BooleanComparator instance that sorts
     * <code><i>trueFirst</i></code> values.
     * 
     * @param trueFirst
     *            when <code>true</code>, sort <code>true</code>, when
     *            <code>false</code>, sort <code>false</code>
     * @return a singleton BooleanComparator instance
     */
    public static BooleanComparator getBooleanComparator(boolean trueFirst) {
        return trueFirst ? TRUE_FIRST : FALSE_FIRST;
    }

    private BooleanComparator(boolean trueFirst) {
        this.trueFirst = trueFirst;
    }

    public boolean isTrueFirst() {
        return trueFirst;
    }

    public int compare(Object obj1, Object obj2) {
        return compare((Boolean)obj1, (Boolean)obj2);
    }

    /**
     * Compares two non- <code>null</code> <code>Boolean</code> objects
     * according to the value of {@link #trueFirst}.
     * 
     * @param b1
     *            the first boolean to compare
     * @param b2
     *            the second boolean to compare
     * @return negative if obj1 is less, positive if greater, zero if equal
     * @throws NullPointerException
     *             when either argument <code>null</code>
     */
    public int compare(Boolean b1, Boolean b2) {
        boolean v1 = b1.booleanValue();
        boolean v2 = b2.booleanValue();
        return (v1 ^ v2) ? ((v1 ^ trueFirst) ? 1 : -1) : 0;
    }

    public int hashCode() {
        int hash = "BooleanComparator".hashCode();
        return trueFirst ? -1 * hash : hash;
    }

    public boolean equals(Object object) {
        return (this == object)
                || ((object instanceof BooleanComparator) && (this.trueFirst == ((BooleanComparator)object).trueFirst));
    }

}

