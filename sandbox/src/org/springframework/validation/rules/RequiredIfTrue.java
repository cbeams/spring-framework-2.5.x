/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.predicates.Required;
import org.springframework.util.Assert;

/**
 * Validates a property value as 'required' if some other condition is true.
 * 
 * @author Seth Ladd
 * @author Keith Donald
 */
public class RequiredIfTrue implements UnaryPredicate {
    private String propertyName;
    private UnaryPredicate predicate;

    /**
     * Tests that the property is required if the provided predicate is
     * satisified.
     * 
     * @param otherPropertyNames
     *            one or more other properties, delimited by commas.
     * @param operator
     *            the logical operator, either AND or OR.
     */
    public RequiredIfTrue(UnaryPredicate predicate) {
        Assert.notNull(predicate);
        setPredicate(predicate);
    }

    protected RequiredIfTrue() {

    }

    protected void setPredicate(UnaryPredicate predicate) {
        Assert.notNull(predicate);
        this.predicate = predicate;
    }

    /**
     * The property name which may be required if other properties are present.
     * 
     * @param propertyName
     *            The property name
     */
    public void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public boolean test(Object bean) {
        Assert.notNull(propertyName);
        if (predicate.test(bean)) {
            return Required.instance().test(
                    new BeanWrapperImpl(bean).getPropertyValue(propertyName));
        } else {
            return true;
        }
    }

}