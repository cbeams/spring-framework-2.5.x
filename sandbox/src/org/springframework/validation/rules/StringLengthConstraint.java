/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import org.springframework.functor.BinaryOperator;
import org.springframework.functor.BinaryPredicate;
import org.springframework.functor.UnaryComposePredicate;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.functions.StringLengthFunction;
import org.springframework.functor.predicates.BindSecond;
import org.springframework.functor.predicates.NumberRange;

public class StringLengthConstraint implements UnaryPredicate {
    private UnaryPredicate predicate;

    public StringLengthConstraint(BinaryOperator operator, int length) {
        BinaryPredicate comparator = operator.getPredicate();
        UnaryPredicate binder = new BindSecond(comparator, new Integer(length));
        this.predicate = new UnaryComposePredicate(binder, StringLengthFunction
                .instance());
    }

    public StringLengthConstraint(int min, int max) {
        UnaryPredicate range = new NumberRange(new Integer(min), new Integer(
                max));
        this.predicate = new UnaryComposePredicate(range, StringLengthFunction
                .instance());
    }

    public boolean evaluate(Object value) {
        return this.predicate.evaluate(value);
    }

}