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
package org.springframework.util.comparator;

import java.util.Comparator;

import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * A Comparator that will safely compare nulls to be lower or higher than other
 * objects.
 * 
 * @author Keith Donald
 */
public class NullSafeComparator implements Comparator {

    private static final NullSafeComparator NULLS_LOW = new NullSafeComparator(
            true);

    private static final NullSafeComparator NULLS_HIGH = new NullSafeComparator(
            false);

    /**
     * The comparator to use when comparing two non-null objects.
     */
    private Comparator nonNullComparator;

    /**
     * Specifies whether a <code>null</code> object is compared lower than
     * non-null objects.
     */
    private boolean nullsLow;

    /**
     * Create a NullSafeComparator that sorts <code>null</code> based on the
     * provided boolean. When comparing two non-null objects, the
     * {@link ComparableComparator}is used.
     * 
     * @param nullsLow
     *            whether to treat nulls lower or higher than non-null objects
     */
    private NullSafeComparator(boolean nullsLow) {
        this(ComparableComparator.instance(), nullsLow);
    }

    /**
     * Create a NullSafeComparator that sorts <code>null</code> lower than any
     * non-null object it is compared with. When comparing two non-null objects,
     * the specified {@link Comparator}is used.
     * 
     * @param comparator
     *            the comparator to use when comparing two non-null objects.
     */
    public NullSafeComparator(Comparator comparator) {
        this(comparator, true);
    }

    /**
     * Create a NullSafeComparator that sorts <code>null</code> based on the
     * provided boolean. When comparing two non-null objects, the specified
     * {@link Comparator}used.
     * 
     * @param comparator
     *            the comparator to use when comparing two non-null objects
     * @param nullsLow
     *            whether to treat nulls lower or higher than non-null objects
     */
    public NullSafeComparator(Comparator comparator, boolean nullsLow) {
        Assert.notNull(comparator, "The non-null comparator is required");
        this.nonNullComparator = comparator;
        this.nullsLow = nullsLow;
    }

    public int compare(Object o1, Object o2) {
        if (o1 == o2) { return 0; }
        if (o1 == null) { return (this.nullsLow ? -1 : 1); }
        if (o2 == null) { return (this.nullsLow ? 1 : -1); }
        return this.nonNullComparator.compare(o1, o2);
    }

    public int hashCode() {
        return (nullsLow ? -1 : 1) * nonNullComparator.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof NullSafeComparator) {
            NullSafeComparator other = (NullSafeComparator)obj;
            return ((this.nullsLow == other.nullsLow) && (this.nonNullComparator
                    .equals(other.nonNullComparator)));
        }
        return false;
    }

    /**
     * Factory method that returns a global instance of a NullSafeComparator
     * capable of working with Comparables, treating the null value as 'lower'
     * than non null values.
     * 
     * @return The shared instance.
     */
    public static final Comparator instance() {
        return instance(true);
    }

    public static final Comparator instance(boolean nullsLow) {
        return (nullsLow ? NULLS_LOW : NULLS_HIGH);
    }

    public String toString() {
        return new ToStringCreator(this).append("nullsLow", nullsLow).append(
                "nonNullComparator", nonNullComparator).toString();
    }

}