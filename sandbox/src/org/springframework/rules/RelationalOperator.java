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
package org.springframework.rules;

import org.springframework.rules.predicates.EqualTo;
import org.springframework.rules.predicates.GreaterThan;
import org.springframework.rules.predicates.GreaterThanEqualTo;
import org.springframework.rules.predicates.LessThan;
import org.springframework.rules.predicates.LessThanEqualTo;

/**
 * Type-safe enum class for supported binary operators.
 * 
 * @author Keith Donald
 */
public abstract class RelationalOperator extends Operator {

    /**
     * The <code>EQUAL_TO (==)</code> operator
     */
    public static final RelationalOperator EQUAL_TO = new RelationalOperator(
            "=") {
        public BinaryPredicate getPredicate() {
            return EqualTo.instance();
        }
    };

    /**
     * The <code>LESS_THAN (<)</code> operator
     */
    public static final RelationalOperator LESS_THAN = new RelationalOperator(
            "<") {
        public BinaryPredicate getPredicate() {
            return LessThan.instance();
        }
    };

    /**
     * The <code>LESS_THAN_EQUAL_TO (<=)</code> operator
     */
    public static final RelationalOperator LESS_THAN_EQUAL_TO = new RelationalOperator(
            "<=") {
        public BinaryPredicate getPredicate() {
            return LessThanEqualTo.instance();
        }
    };

    /**
     * The <code>GREATER_THAN (>)</code> operator
     */
    public static final RelationalOperator GREATER_THAN = new RelationalOperator(
            ">") {
        public BinaryPredicate getPredicate() {
            return GreaterThan.instance();
        }
    };

    /**
     * The <code>GREATER_THAN_EQUAL_TO (>=)</code> operator
     */
    public static final RelationalOperator GREATER_THAN_EQUAL_TO = new RelationalOperator(
            ">=") {
        public BinaryPredicate getPredicate() {
            return GreaterThanEqualTo.instance();
        }
    };

    private RelationalOperator(String code) {
        super(code);
    }

    /**
     * Returns the predicate instance associated with this binary operator.
     * 
     * @return the associated binary predicate
     */
    public abstract BinaryPredicate getPredicate();

}