/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.functor.predicates;

import java.io.Serializable;

import org.springframework.functor.PredicateFactory;
import org.springframework.functor.UnaryAnd;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.functions.Maximum;
import org.springframework.functor.functions.Minimum;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;

/**
 * A number range.
 * 
 * @author Keith Donald
 * @see adapted from jakarta commons-lang's NumberRange
 */
public final class NumberRange implements Serializable, UnaryPredicate {

    private UnaryPredicate rangeConstraint;

    /**
     * Constructs a new, inclusive <code>NumberRange</code> with the provided
     * minimum and maximum numbers.
     * <p>
     * <p>
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
        Number maximum = (Number)Maximum.instance().evaluate(num1, num2);
        Number minimum = (Number)Minimum.instance().evaluate(num1, num2);
        UnaryPredicate min =
            PredicateFactory.bind(
                GreaterThanEqualTo.instance(),
                minimum);
        UnaryPredicate max =
            PredicateFactory.bind(LessThanEqualTo.instance(), maximum);
        this.rangeConstraint = new UnaryAnd(min, max);
    }

    public boolean evaluate(Object value) {
        return this.rangeConstraint.evaluate(value);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("rangeConstraint", rangeConstraint)
            .toString();
    }

}