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

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.values.PropertyAccessStrategy;
import org.springframework.util.Assert;

/**
 * A unary predicate that returns the result of a <code>boolean</code>
 * expression that tests a variable bean property value against a predicate
 * (constraint). For example: <code>pet.age is required</code>
 * 
 * @author Keith Donald
 */
public class BeanPropertyValueConstraint extends AbstractBeanPropertyExpression
        implements UnaryPredicate {
    private UnaryPredicate valueConstraint;

    /**
     * Creates a BeanPropertyValueConstraint.
     * 
     * @param propertyName
     *            The constrained property.
     * @param valueConstraint
     *            The property value constraint (tester).
     */
    public BeanPropertyValueConstraint(String propertyName,
            UnaryPredicate valueConstraint) {
        super(propertyName);
        Assert.notNull(valueConstraint);
        Assert.isTrue(!(valueConstraint instanceof BeanPropertyExpression));
        this.valueConstraint = valueConstraint;
    }

    protected boolean test(PropertyAccessStrategy domainObjectAccessStrategy) {
        return valueConstraint.test(domainObjectAccessStrategy
                .getPropertyValue(getPropertyName()));
    }

    public UnaryPredicate getPredicate() {
        return valueConstraint;
    }

    public String toString() {
        return valueConstraint.toString();
    }
}