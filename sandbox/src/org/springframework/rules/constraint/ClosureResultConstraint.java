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
package org.springframework.rules.constraint;

import org.springframework.rules.Closure;
import org.springframework.rules.Constraint;
import org.springframework.util.Assert;

/**
 * Tests the result returned from evaluating a closure function.
 * 
 * @author Keith Donald
 */
public class ClosureResultConstraint implements Constraint {
    private Constraint constraint;

    private Closure closure;

    /**
     * Creates a UnaryFunctionResultTester that tests the result returned from
     * evaulating the specified unary function.
     * 
     * @param constraint
     *            The predicate that will test the function return value.
     * @param function
     *            The function to test.
     */
    public ClosureResultConstraint(Closure function, Constraint constraint) {
        Assert.notNull(constraint);
        Assert.notNull(function);
        this.constraint = constraint;
        this.closure = function;
    }

    /**
     * Tests the result returned by evaluating the specified argument against
     * the configured unary function.
     * 
     * @see org.springframework.rules.Constraint#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        Object returnValue = closure.call(argument);
        return this.constraint.test(returnValue);
    }

    public Closure getFunction() {
        return closure;
    }

    public Constraint getPredicate() {
        return constraint;
    }

    public String toString() {
        return "[" + closure.toString() + " " + constraint.toString() + "]";
    }
}