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
import org.springframework.rules.predicates.Required;
import org.springframework.rules.values.PropertyAccessStrategy;
import org.springframework.util.Assert;

/**
 * Validates a property value as 'required' if some other condition is true.
 * 
 * @author Seth Ladd
 * @author Keith Donald
 */
public class RequiredIfTrue extends AbstractBeanPropertyExpression implements
        UnaryPredicate {
    private String propertyName;

    private UnaryPredicate predicate;

    /**
     * Tests that the property is present if the provided predicate is
     * satisified.
     * 
     * @param predicate
     *            the condition
     */
    public RequiredIfTrue(String propertyName, UnaryPredicate predicate) {
        super(propertyName);
        setPredicate(predicate);
    }

    protected RequiredIfTrue(String propertyName) {
        super(propertyName);
    }

    public UnaryPredicate getPredicate() {
        return predicate;
    }

    protected void setPredicate(UnaryPredicate predicate) {
        Assert.notNull(predicate);
        this.predicate = predicate;
    }

    protected boolean test(PropertyAccessStrategy domainObjectAccessStrategy) {
        if (predicate.test(domainObjectAccessStrategy)) {
            return Required.instance().test(
                    domainObjectAccessStrategy.getValue(getPropertyName()));
        }
        else {
            return true;
        }
    }

    public String toString() {
        return "required if (" + predicate + ")";
    }

}