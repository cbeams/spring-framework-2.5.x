package org.springframework.functor;

import org.springframework.functor.predicates.GreaterThan;
import org.springframework.functor.predicates.GreaterThanEqualTo;
import org.springframework.functor.predicates.LessThan;
import org.springframework.functor.predicates.LessThanEqualTo;

/**
 * @author Keith Donald
 */
public abstract class BinaryOperator {
    public static final BinaryOperator LESS_THAN = new BinaryOperator("<") {
        public BinaryPredicate getPredicate() {
            return LessThan.instance();
        }
    };

    public static final BinaryOperator LESS_THAN_EQUAL_TO = new BinaryOperator(
            "<=") {
        public BinaryPredicate getPredicate() {
            return LessThanEqualTo.instance();
        }
    };

    public static final BinaryOperator GREATER_THAN = new BinaryOperator(">") {
        public BinaryPredicate getPredicate() {
            return GreaterThan.instance();
        }
    };

    public static final BinaryOperator GREATER_THAN_EQUAL_TO = new BinaryOperator(
            ">=") {
        public BinaryPredicate getPredicate() {
            return GreaterThanEqualTo.instance();
        }
    };

    private String name;

    private BinaryOperator(String name) {
        this.name = name;
    }

    public abstract BinaryPredicate getPredicate();

    public String toString() {
        return name;
    }
}