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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * A sort definition encapsulating a comparator to sort by and a order property
 * denoting whether comparison results should be treated in forward (standard
 * ascending) order or flipped for reverse (descending) order.
 * 
 * @author Keith Donald
 */
public class SortDefinition implements Comparator, Serializable {
    private Comparator comparator;

    private SortOrder order;

    /**
     * Static factory method creating a sort definition list from the provided
     * comparator array. All sort definitions sort in forward (ascending) order.
     * 
     * @param comparators
     * @return A sort definition list
     */
    public static List createSortDefinitionList(Comparator[] comparators) {
        List sortDefinitions = new ArrayList(comparators.length);
        for (int i = 0; i < comparators.length; i++) {
            sortDefinitions.add(new SortDefinition(comparators[i]));
        }
        return sortDefinitions;
    }

    public SortDefinition(Comparator comparator) {
        this(comparator, null);
    }

    public SortDefinition(Comparator comparator, SortOrder order) {
        setComparator(comparator);
        setOrder(order);
    }

    public void setComparator(Comparator comparator) {
        Assert.notNull(comparator, "The comparator property is required");
        this.comparator = comparator;
    }

    public void setOrder(SortOrder order) {
        if (order == null) {
            order = SortOrder.ASCENDING;
        }
        this.order = order;
    }

    public boolean equals(Object o) {
        if (!(o instanceof SortDefinition)) { return false; }
        SortDefinition def = (SortDefinition)o;
        return comparator.equals(def.comparator) && order.equals(def.order);
    }

    public int hashCode() {
        return comparator.hashCode() + order.hashCode();
    }

    public int compare(Object o1, Object o2) {
        int result = comparator.compare(o1, o2);
        if (result != 0) {
            // invert the order if it is a reverse sort
            if (order == SortOrder.DESCENDING) {
                if (Integer.MIN_VALUE == result) {
                    result = Integer.MAX_VALUE;
                }
                else {
                    result *= -1;
                }
            }
            return result;
        }
        return 0;
    }

    public void flipOrder() {
        this.order = SortOrder.flip(this.order);
    }

    public String toString() {
        return new ToStringCreator(this).append("comparator", comparator)
                .append("order", order).toString();
    }

}