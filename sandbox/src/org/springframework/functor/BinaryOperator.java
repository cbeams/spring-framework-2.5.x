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