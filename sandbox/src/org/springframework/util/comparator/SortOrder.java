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

/**
 * A Sort Order enum type - may be ASCENDING (forward) or DESCENDING (reverse).
 * 
 * @author Keith Donald
 */
public class SortOrder implements Serializable {

    public static final SortOrder ASCENDING = new SortOrder(true);

    public static final SortOrder DESCENDING = new SortOrder(false);

    private boolean ascending;

    private SortOrder(boolean ascending) {
        this.ascending = ascending;
    }

    public boolean equals(Object o) {
        if (!(o instanceof SortOrder)) { return false; }
        SortOrder order = (SortOrder)o;
        return ascending == order.ascending;
    }

    public int hashCode() {
        int hash = "SortOrder".hashCode();
        return ascending ? -1 * hash : hash;
    }

    public String toString() {
        return (ascending ? "ascending" : "descending");
    }

    public static SortOrder flip(SortOrder order) {
        return (order == ASCENDING ? DESCENDING : ASCENDING);
    }

}