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

import org.springframework.functor.BinaryPredicate;
import org.springframework.functor.PredicateFactory;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.functions.GetProperty;
import org.springframework.util.Assert;

/**
 * A unary predicate that tests an expression that tests a variable bean
 * property value with a constant parameter value. For example: <code>pet.age > 5</code>
 * 
 * @author Keith Donald
 */
public class ParameterizedBeanPropertyExpression implements UnaryPredicate {
    private String propertyName;
    private BinaryPredicate parameterizedExpression;

    /**
     * Creates a BeanPropertyExpressionTester.
     * 
     * @param propertyName
     *            The first property participating in the expression.
     * @param parameter
     *            The constant parameter value participating in the expression.
     * @param expression
     *            The expression predicate (tester).
     */
    public ParameterizedBeanPropertyExpression(
        String propertyName,
        Object parameter,
        BinaryPredicate expression) {
        Assert.notNull(expression);
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
        ParameterizedBinaryPredicate valueTester =
            new ParameterizedBinaryPredicate(expression, parameter);
        this.parameterizedExpression =
            PredicateFactory.attachResultTester(
                valueTester,
                GetProperty.instance());
    }

    /**
     * Tests the value of the configured propertyName for this bean against the
     * configured parameter value using the configured binary predicate.
     * 
     * @see org.springframework.functor.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        return parameterizedExpression.test(bean, propertyName);
    }

}
