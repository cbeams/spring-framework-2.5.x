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
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.functions.GetProperty;
import org.springframework.util.Assert;

/**
 * A unary predicate that test an expression consisting of two variable bean
 * property values.  For example, <code>pet.ageAtFirstVisit > pet.currentAge</code>
 * 
 * @author Keith Donald
 */
public class BeanPropertyExpression implements UnaryPredicate {
    private String propertyName;
    private String otherPropertyName;
    private BinaryPredicate beanPropertyExpression;

    /**
     * Creates a BeanPropertyExpression
     * 
     * @param propertyName
     *            The first property participating in the expression.
     * @param otherPropertyName
     *            The second property participating in the expression.
     * @param beanPropertyExpression
     *            The expression predicate (tester).
     */
    public BeanPropertyExpression(
        String propertyName,
        String otherPropertyName,
        BinaryPredicate beanPropertyExpression) {
        Assert.notNull(propertyName);
        Assert.notNull(otherPropertyName);
        Assert.notNull(beanPropertyExpression);
        this.propertyName = propertyName;
        this.otherPropertyName = otherPropertyName;
        this.beanPropertyExpression = beanPropertyExpression;
    }

    /**
     * Tests the values of the two specified properties of this bean using the
     * configured binary predicate.
     * 
     * @see org.springframework.functor.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        return beanPropertyExpression.test(
            GetProperty.instance().evaluate(bean, propertyName),
            GetProperty.instance().evaluate(bean, otherPropertyName));
    }

}
