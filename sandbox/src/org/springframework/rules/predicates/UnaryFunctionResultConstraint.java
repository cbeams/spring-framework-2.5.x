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
package org.springframework.rules.predicates;

import org.springframework.rules.UnaryFunction;
import org.springframework.rules.UnaryPredicate;
import org.springframework.util.Assert;

/**
 * Tests the result returned from evaluating a unary function.
 * 
 * @author Keith Donald
 */
public class UnaryFunctionResultConstraint implements UnaryPredicate {
    private UnaryPredicate constraint;
    private UnaryFunction function;

    /**
     * Creates a UnaryFunctionResultTester that tests the result returned from
     * evaulating the specified unary function.
     * 
     * @param constraint
     *            The predicate that will test the function return value.
     * @param function
     *            The function to test.
     */
    public UnaryFunctionResultConstraint(
        UnaryFunction function,
        UnaryPredicate constraint) {
        Assert.notNull(constraint);
        Assert.notNull(function);
        this.constraint = constraint;
        this.function = function;
    }

    /**
     * Tests the result returned by evaluating the specified argument against
     * the configured unary function.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        Object returnValue = function.evaluate(argument);
        return this.constraint.test(returnValue);
    }

    public UnaryFunction getFunction() {
        return function;
    }
    
    public UnaryPredicate getPredicate() {
        return constraint;
    }
    
    public String toString() {
        return "[" + function.toString() + " " + constraint.toString() + "]";
    }
}