/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.predicates.beans;

import org.springframework.util.Assert;

/**
 * Convenience superclass for bean property expressions.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanPropertyExpression implements
        BeanPropertyExpression {
    private String propertyName;

    public AbstractBeanPropertyExpression(String propertyName) {
        setPropertyName(propertyName);
    }

    /**
     * @see org.springframework.rules.predicates.beans.BeanPropertyExpression#getPropertyName()
     */
    public String getPropertyName() {
        return propertyName;
    }

    protected void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public String toString() {
        return getPropertyName();
    }

}