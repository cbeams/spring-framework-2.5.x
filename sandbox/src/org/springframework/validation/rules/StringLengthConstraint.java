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
package org.springframework.validation.rules;

import org.springframework.functor.BinaryOperator;
import org.springframework.functor.BinaryPredicate;
import org.springframework.functor.PredicateFactory;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.functions.StringLength;
import org.springframework.functor.predicates.Range;

/**
 * Constraint to validate an object's string length.
 * 
 * @author  Keith Donald
 */
public class StringLengthConstraint implements UnaryPredicate {
    private UnaryPredicate predicate;

    /**
     * Constructs a maxlength constraint of the specified length.
     * 
     * @param length the max string length
     */
    public StringLengthConstraint(int length) {
        this(BinaryOperator.LESS_THAN_EQUAL_TO, length);
    }
    
    /**
     * Constructs a string length constraint with the specified
     * operator and length constraint.
     * 
     * @param operator the operator (one of ==, >, >=, <, <=)
     * @param length the length constraint
     */
    public StringLengthConstraint(BinaryOperator operator, int length) {
        BinaryPredicate comparer = operator.getPredicate();
        UnaryPredicate lengthConstraint = PredicateFactory.bind(
                comparer, new Integer(length));
        this.predicate = PredicateFactory.attachResultTester(
                lengthConstraint, StringLength.instance());
    }

    /**
     * Constructs a string length range constraint.
     * 
     * @param min The minimum edge of the range
     * @param max the maximum edge of the range
     */
    public StringLengthConstraint(int min, int max) {
        UnaryPredicate rangeConstraint = new Range(new Integer(min),
                new Integer(max));
        this.predicate = PredicateFactory.attachResultTester(
                rangeConstraint, StringLength.instance());
    }

    public boolean test(Object value) {
        return this.predicate.test(value);
    }

}