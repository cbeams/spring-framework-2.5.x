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
package org.springframework.rules.predicates;

import org.springframework.rules.BinaryFunction;
import org.springframework.rules.BinaryPredicate;
import org.springframework.rules.UnaryPredicate;
import org.springframework.util.Assert;

/**
 * Tests the result returned from evaluating a binary function.
 * 
 * @author Keith Donald
 */
public class BinaryFunctionResultConstraint implements BinaryPredicate {
    private UnaryPredicate constraint;
    private BinaryFunction function;

    /**
     * Creates a BinaryFunctionResultTester that tests the result return from
     * evaulating the specified binary function.
     * 
     * @param constraint
     *            The predicate that will test the function return value.
     * @param function
     *            The function to test.
     */
    public BinaryFunctionResultConstraint(UnaryPredicate constraint,
            BinaryFunction function) {
        Assert.notNull(constraint);
        Assert.notNull(function);
        this.constraint = constraint;
        this.function = function;
    }

    /**
     * Tests the result returned by evaluating the specified arguments against
     * the configured binary function.
     * 
     * @see org.springframework.rules.BinaryPredicate#test(java.lang.Object,
     *      java.lang.Object)
     */
    public boolean test(Object argument1, Object argument2) {
        return this.constraint.test(function.evaluate(argument1, argument2));
    }

    public String toString() {
        return constraint.toString() + "(" + function.toString() + ")";
    }
}