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
package org.springframework.rules.predicates.beans;

import org.springframework.rules.BinaryPredicate;
import org.springframework.rules.functions.GetProperty;
import org.springframework.util.Assert;

/**
 * A unary predicate that returns the result of a <code>boolean</code>
 * expression that tests two variable bean property values. For example,
 * <code>pet.ageAtFirstVisit > pet.currentAge</code>
 * 
 * @author Keith Donald
 */
public class BeanPropertiesExpression extends AbstractBeanPropertyExpression {
    private String otherPropertyName;
    private BinaryPredicate beanPropertyExpression;

    /**
     * Creates a BeanPropertyExpression
     * 
     * @param propertyName
     *            The first property participating in the expression.
     * @param beanPropertyExpression
     *            The expression predicate that will test the two bean property
     *            values.
     * @param otherPropertyName
     *            The second property participating in the expression.
     */
    public BeanPropertiesExpression(String propertyName,
            BinaryPredicate beanPropertyExpression, String otherPropertyName) {
        super(propertyName);
        Assert.notNull(otherPropertyName);
        Assert.notNull(beanPropertyExpression);
        this.otherPropertyName = otherPropertyName;
        this.beanPropertyExpression = beanPropertyExpression;
    }

    public String getOtherPropertyName() {
        return otherPropertyName;
    }

    public BinaryPredicate getPredicate() {
        return beanPropertyExpression;
    }

    /**
     * Tests the values of the two configured propertyNames for this bean using
     * the configured binary predicate.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        GetProperty getProperty = new GetProperty(bean);
        return beanPropertyExpression.test(getProperty
                .evaluate(getPropertyName()), getProperty
                .evaluate(otherPropertyName));
    }

    public String toString() {
        return super.toString() + " " + beanPropertyExpression.toString() + " "
                + otherPropertyName;
    }

}