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
import org.springframework.rules.PredicateFactory;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.functions.GetProperty;

/**
 * Predicate that tests if the specified bean property is "present" - that is,
 * passes the "Required" test.
 * 
 * @author Keith Donald
 * @see Required
 */
public class PropertyPresent
    extends AbstractBeanPropertyExpression
    implements UnaryPredicate {
    private static final BinaryPredicate propertyPresentTester =
        PredicateFactory.attachResultConstraint(
            Required.instance(),
            GetProperty.instance());

    /**
     * Constructs a property present predicate for the specified property.
     * 
     * @param propertyName
     *            The bean property name.
     */
    public PropertyPresent(String propertyName) {
        super(propertyName);
    }

    /**
     * Test if the value of <code>propertyName</code> is present for the
     * specified bean.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        return propertyPresentTester.test(bean, getPropertyName());
    }

}