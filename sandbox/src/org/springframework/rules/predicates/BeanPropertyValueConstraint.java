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

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.functions.GetProperty;
import org.springframework.util.Assert;

/**
 * A unary predicate that returns the result of a <code>boolean</code>
 * expression that tests a variable bean property value against a predicate
 * (constraint). For example: <code>pet.age is required</code>
 * 
 * @author Keith Donald
 */
public class BeanPropertyValueConstraint
    extends AbstractBeanPropertyConstraint
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
    public BeanPropertyValueConstraint(
        String propertyName,
        UnaryPredicate valueConstraint) {
        super(propertyName);
        Assert.notNull(valueConstraint);
        this.valueConstraint = valueConstraint;
    }

    /**
     * Tests the value of the configured propertyName for this bean against the
     * configured predicate constraint.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        return valueConstraint.test(
            GetProperty.instance().evaluate(bean, getPropertyName()));
    }

}
