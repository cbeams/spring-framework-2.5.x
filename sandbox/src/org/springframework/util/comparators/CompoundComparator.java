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
package org.springframework.util.comparators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;

/**
 * A comparator that chains a sequence of one or more more Comparators.
 * <p>
 * <p>
 * A compound comparator calls each Comparator in sequence until a single
 * Comparator returns a non-zero result, or the comparators are exhausted and
 * zero is returned.
 * <p>
 * <p>
 * This facilitates in-memory sorting similar to multi-column sorting in SQL.
 * The order of any single Comparator in the list can also be reversed.
 * 
 * @author Keith Donald
 * @see adapted from jakarta-commons-lang's ComparatorChain
 */
public class CompoundComparator implements Comparator, Serializable {
    private List comparators;
    private BitSet sortOrder;
    private boolean locked;

    /**
     * Construct a CompoundComparator with initially no Comparators. Clients
     * must add at least one Comparator before calling the
     * compare(Object,Object) method, or an IllegalStateException is thrown.
     */
    public CompoundComparator() {
        this(new ArrayList(), new BitSet());
    }

    /**
     * Construct a CompoundComparator from the Comparators in the List. All
     * Comparators will default to the forward sort order.
     * 
     * @param list
     *            the list of comparators
     */
    public CompoundComparator(List list) {
        this(list, new BitSet(list.size()));
    }

    /**
     * Construct a CompoundComparator from the Comparators in the given List.
     * The sort order of each column will be drawn from the given BitSet. If a
     * bit at a comparator index is <i>false </i>, the forward sort order is
     * used; else a reverse sort order is used.
     * 
     * @param list
     *            the list of Comparators.
     * @param sortOrder
     *            The sort order for each Comparator.
     */
    public CompoundComparator(List list, BitSet sortOrder) {
        Assert.notNull(list);
        Assert.notNull(sortOrder);
        comparators = new ArrayList(list);
        this.sortOrder = sortOrder;
    }

    /**
     * Add a Comparator to the end of the chain using the forward sort order
     * 
     * @param comparator
     *            Comparator with the forward sort order
     */
    public void addComparator(Comparator comparator) {
        addComparator(comparator, false);
    }

    /**
     * Add a Comparator to the end of the chain using the given sort order
     * 
     * @param comparator
     *            Comparator to add to the end of the chain
     * @param reverse
     *            false = forward sort order; true = reverse sort order
     */
    public void addComparator(Comparator comparator, boolean reverse) {
        Assert.notNull(comparator);
        Assert.isTrue(!locked);
        comparators.add(comparator);
        if (reverse == true) {
            sortOrder.set(comparators.size() - 1);
        }
    }

    /**
     * Replace the Comparator at the given index, maintaining the existing sort
     * order.
     * 
     * @param index
     *            index of the Comparator to replace
     * @param comparator
     *            Comparator to place at the given index
     */
    public void setComparator(int index, Comparator comparator) {
        setComparator(index, comparator, false);
    }

    /**
     * Replace the Comparator at the given index in the ComparatorChain, using
     * the given sort order
     * 
     * @param index
     *            index of the Comparator to replace
     * @param comparator
     *            Comparator to set
     * @param reverse
     *            false = forward sort order; true = reverse sort order
     */
    public void setComparator(int index, Comparator comparator, boolean reverse) {
        Assert.notNull(comparator);
        Assert.isTrue(!locked);
        comparators.set(index, comparator);
        if (reverse == true) {
            sortOrder.set(index);
        } else {
            sortOrder.clear(index);
        }
    }

    /**
     * Change the sort order at the given index in the ComparatorChain to a
     * forward sort.
     * 
     * @param index
     *            Index of the ComparatorChain
     */
    public void setForwardSort(int index) {
        Assert.isTrue(!locked);
        Assert.isInRange(index, 0, comparators.size());
        sortOrder.clear(index);
    }

    /**
     * Change the sort order at the given index in the ComparatorChain to a
     * reverse sort.
     * 
     * @param index
     *            Index of the ComparatorChain
     */
    public void setReverseSort(int index) {
        Assert.isTrue(!locked);
        Assert.isInRange(index, 0, comparators.size());
        sortOrder.set(index);
    }

    /**
     * Returns the number of aggregated comparators.
     * 
     * @return The comparator count
     */
    public int size() {
        return comparators.size();
    }

    public int compare(Object o1, Object o2) {
        if (locked == false) {
            Assert.isTrue(comparators.size() > 0);
            locked = true;
        }
        // iterate over all comparators in the chain
        Iterator it = comparators.iterator();
        for (int i = 0; it.hasNext(); i++) {
            Comparator comparator = (Comparator)it.next();
            int result = comparator.compare(o1, o2);
            if (result != 0) {
                // invert the order if it is a reverse sort
                if (sortOrder.get(i) == true) {
                    if (Integer.MIN_VALUE == result) {
                        result = Integer.MAX_VALUE;
                    } else {
                        result *= -1;
                    }
                }
                return result;
            }
        }
        return 0;
    }

    public int hashCode() {
        int hashCode = 0;
        hashCode ^= comparators.hashCode();
        hashCode ^= sortOrder.hashCode();
        return hashCode;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NullSafeComparator) {
            CompoundComparator c = (CompoundComparator)o;
            return sortOrder.equals(c.sortOrder)
                    && comparators.equals(c.comparators);
        }
        return false;
    }

    public String toString() {
        return new ToStringBuilder(this).append("comparators", comparators)
                .append("sortOrder", sortOrder).append("isLocked", locked)
                .toString();
    }

}

