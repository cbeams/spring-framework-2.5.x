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

import java.io.Serializable;

import org.springframework.functor.PredicateFactory;
import org.springframework.functor.UnaryPredicate;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;

/**
 * A range whose edges are defined by a minimum Comparable and a maximum
 * Comparable.
 * 
 * @author Keith Donald
 */
public final class Range implements Serializable, UnaryPredicate {

    private UnaryPredicate rangeConstraint;

    /**
     * Creates a range with the specified min and max edges.
     * 
     * @param min
     *            the low edge of the range
     * @param max
     *            the high edge of the range
     */
    public Range(Comparable min, Comparable max) {
        Assert.isTrue(min != null && max != null);
        Assert.isTrue(min.getClass() == max.getClass());
        Assert.isTrue(LessThanEqualTo.instance().test(min, max), "Minimum "
                + min + " must be less than maximum " + max);
        UnaryPredicate minimum = PredicateFactory.bind(GreaterThanEqualTo
                .instance(), min);
        UnaryPredicate maximum = PredicateFactory.bind(LessThanEqualTo
                .instance(), max);
        this.rangeConstraint = new UnaryAnd(minimum, maximum);
    }

    /**
     * Test if the specified comparable argument falls within the range.
     * 
     * @see org.springframework.functor.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        return this.rangeConstraint.test(argument);
    }

    public String toString() {
        return new ToStringBuilder(this).append("rangeConstraint",
                rangeConstraint).toString();
    }

}