/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Generic, immutable composite key class. Useful for storing a structure of
 * unique objects as a unified key in a Map.
 * 
 * Backed by a LinkedHashSet, a JDK 1.4 class that guarantees predictable
 * insertion based iteration order. Objects participating in the key should
 * implement equals and hashCode.
 * 
 * @author Keith Donald
 */
public class CompositeKey {
    private Set parts;

    /**
     * Creates a CompositeKey from the set of individual keys.
     * 
     * @param parts
     *            The set of key parts.
     * @throws IllegalArgumentException
     *             if the set is null or has less than 2 elements.
     */
    public CompositeKey(Set parts) {
        Assert.notNull(parts);
        Assert.isTrue(
            parts.size() > 1,
            "A composite key by definition has more than one key.");
        this.parts = new LinkedHashSet(parts);
    }

    /**
     * Creates a CompositeKey with two key parts.
     * 
     * @param part1
     * @param part2
     */
    public CompositeKey(Object part1, Object part2) {
        this.parts = new LinkedHashSet(2);
        parts.add(part1);
        parts.add(part2);
    }

    /**
     * Creates a CompositeKey with three key parts.
     * 
     * @param part1
     * @param part2
     * @param part3
     */
    public CompositeKey(Object part1, Object part2, Object part3) {
        this.parts = new HashSet(3);
        parts.add(part1);
        parts.add(part2);
        parts.add(part3);
    }

    /**
     * Returns the individual parts that makeup this key.
     * 
     * @return An iterator over this composite key's parts.
     */
    public Iterator parts() {
        return parts.iterator();
    }

    public boolean equals(Object o) {
        if (!(o instanceof CompositeKey)) {
            return false;
        }
        CompositeKey other = (CompositeKey)o;
        return parts.equals(other.parts);
    }

    public int hashCode() {
        return parts.hashCode();
    }

    public String toString() {
        return parts.toString();
    }
}
