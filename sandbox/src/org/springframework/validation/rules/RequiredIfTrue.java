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
     * Tests that the property is present if the provided predicate is
     * satisified.
     * 
     * @param predicate
     *            the condition
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
     * The property name which is required if the set condition is true.
     * 
     * @param propertyName
     *            The property name
     */
    public void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    /**
     * Tests the RequiredIfTrue condition.
     * 
     * @see org.springframework.functor.UnaryPredicate#test(java.lang.Object)
     */
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