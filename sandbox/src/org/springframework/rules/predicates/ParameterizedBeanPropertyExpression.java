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

import org.springframework.rules.BinaryPredicate;

/**
 * A unary predicate that returns the result of a <code>boolean</code>
 * expression that tests a variable bean property value against a constant
 * parameter value. For example: <code>pet.age > 5</code>
 * 
 * @author Keith Donald
 */
public class ParameterizedBeanPropertyExpression
    implements BeanPropertyExpression {
    private BeanPropertyValueConstraint parameterizedExpression;

    /**
     * Creates a BeanPropertyExpressionTester.
     * 
     * @param propertyName
     *            The property participating in the expression.
     * @param expression
     *            The expression predicate (tester).
     * @param parameter
     *            The constant parameter value participating in the expression.
     */
    public ParameterizedBeanPropertyExpression(
        String propertyName,
        BinaryPredicate expression,
        Object parameter) {
        ParameterizedBinaryPredicate valueConstraint =
            new ParameterizedBinaryPredicate(expression, parameter);
        this.parameterizedExpression =
            new BeanPropertyValueConstraint(propertyName, valueConstraint);
    }

    /**
     * @see org.springframework.rules.predicates.BeanPropertyExpression#getPropertyName()
     */
    public String getPropertyName() {
        return parameterizedExpression.getPropertyName();
    }

    public BinaryPredicate getPredicate() {
        return getParameterizedBinaryPredicate().getPredicate();
    }

    public Object getParameter() {
        return getParameterizedBinaryPredicate().getParameter();
    }

    public ParameterizedBinaryPredicate getParameterizedBinaryPredicate() {
        return (ParameterizedBinaryPredicate)this
            .parameterizedExpression
            .getPredicate();
    }

    /**
     * Tests the value of the configured propertyName for this bean against the
     * configured parameter value using the configured binary predicate.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        return parameterizedExpression.test(bean);
    }

}
