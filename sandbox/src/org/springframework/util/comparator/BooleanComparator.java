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

import org.springframework.util.ToStringCreator;

/**
 * A {@link Comparator}for {@link Boolean}objects that can sort either true or
 * false first.
 */
public final class BooleanComparator implements Comparator, Serializable {
    private static final BooleanComparator TRUE_LOW = new BooleanComparator(
            true);

    private static final BooleanComparator TRUE_HIGH = new BooleanComparator(
            false);

    private boolean trueLow;

    /**
     * Returns the default <code>BooleanComparator</code> instance, sorting
     * <code>true</code> values first.
     * 
     * @return The boolean comparator
     */
    public static Comparator instance() {
        return instance(true);
    }

    /**
     * Returns a BooleanComparator instance that sorts
     * <code><i>trueLow</i></code> values.
     * 
     * @param trueLow
     *            when <code>true</code>, true < false, when
     *            <code>false</code>, true > false</code>
     * @return a singleton BooleanComparator instance
     */
    public static Comparator instance(boolean trueLow) {
        return trueLow ? TRUE_LOW : TRUE_HIGH;
    }

    private BooleanComparator(boolean trueLow) {
        this.trueLow = trueLow;
    }

    public int compare(Object obj1, Object obj2) {
        return compare((Boolean)obj1, (Boolean)obj2);
    }

    /**
     * Compares two non- <code>null</code> <code>Boolean</code> objects
     * according to the value of {@link #trueLow}.
     * 
     * @param b1
     *            the first boolean to compare
     * @param b2
     *            the second boolean to compare
     * @return negative if obj1 is less, positive if greater, zero if equal
     * @throws NullPointerException
     *             when either argument <code>null</code>
     */
    private int compare(Boolean b1, Boolean b2) {
        boolean v1 = b1.booleanValue();
        boolean v2 = b2.booleanValue();
        return (v1 ^ v2) ? ((v1 ^ trueLow) ? 1 : -1) : 0;
    }

    public int hashCode() {
        int hash = "BooleanComparator".hashCode();
        return trueLow ? -1 * hash : hash;
    }

    public boolean equals(Object object) {
        return (this == object)
                || ((object instanceof BooleanComparator) && (this.trueLow == ((BooleanComparator)object).trueLow));
    }

    public String toString() {
        return new ToStringCreator(this).append("trueFirst", trueLow)
                .toString();
    }
}

