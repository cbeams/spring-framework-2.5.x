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

import java.io.Serializable;

import org.springframework.functor.UnaryPredicate;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;

/**
 * A number range.
 * 
 * @author  keith
 * @see  adapted from jakarta commons-lang's NumberRange
 */
public final class NumberRange implements Serializable, UnaryPredicate {

    /**
     * The minimum number in this range.
     */
    private final Number minimum;

    /**
     * The maximum number in this range.
     */
    private final Number maximum;

    /**
     * Cached output hashCode (class is immutable).
     */
    private transient int hashCode = 0;

    /**
     * Constructs a new, inclusive <code>NumberRange</code> with the provided minimum
     * and maximum numbers.
     * <p><p> 
     * The arguments may be passed in the order (min, max) or (max, min).
     * 
     * @param num1
     *            first number that defines the range, inclusive
     * @param num2
     *            second number that defines the range, inclusive
     * @throws IllegalArgumentException
     *             if either number is <code>null</code>
     * @throws IllegalArgumentException
     *             if the numbers are of different types
     */
    public NumberRange(Number num1, Number num2) {
        Assert.isTrue(num1 != null && num2 != null);
        Assert.isTrue(num1.getClass() == num2.getClass());
        int compare = ((Comparable)num1).compareTo(num2);
        if (compare == 0) {
            this.minimum = num1;
            this.maximum = num1;
        } else if (compare > 0) {
            this.minimum = num2;
            this.maximum = num1;
        } else {
            this.minimum = num1;
            this.maximum = num2;
        }
    }

    /**
     * Returns the minimum number in this range.
     * 
     * @return the minimum number in this range
     */
    public Number getMinimum() {
        return minimum;
    }

    /**
     * Returns the maximum number in this range.
     * 
     * @return the maximum number in this range
     */
    public Number getMaximum() {
        return maximum;
    }

    /**
     * Tests whether the specified <code>number</code> occurs within this
     * range.
     * 
     * @param number
     *            the number to test, may be <code>null</code>
     * @return <code>true</code> if the specified number occurs within this
     *         range
     * @throws IllegalArgumentException
     *             if the number is of a different type to the range
     */
    public boolean containsNumber(Number number) {
        if (number == null) {
            return false;
        }
        Assert.isTrue(number.getClass() == minimum.getClass());
        int compareMin = ((Comparable)minimum).compareTo(number);
        int compareMax = ((Comparable)maximum).compareTo(number);
        return (compareMin <= 0 && compareMax >= 0);
    }

    public boolean evaluate(Object value) {
        return containsNumber((Number)value);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof NumberRange == false) {
            return false;
        }
        NumberRange range = (NumberRange)obj;
        return minimum.equals(range.minimum) && maximum.equals(range.maximum);
    }

    public int hashCode() {
        if (hashCode == 0) {
            hashCode = 17;
            hashCode = 37 * hashCode + getClass().hashCode();
            hashCode = 37 * hashCode + minimum.hashCode();
            hashCode = 37 * hashCode + maximum.hashCode();
        }
        return hashCode;
    }

    public String toString() {
        return new ToStringBuilder(this).append("minimum", minimum).append(
                "maximum", maximum).toString();
    }

}