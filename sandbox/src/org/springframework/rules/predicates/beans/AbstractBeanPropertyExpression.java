/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.predicates.beans;

import org.springframework.rules.values.BeanPropertyAccessStrategy;
import org.springframework.rules.values.PropertyAccessStrategy;
import org.springframework.util.Assert;

/**
 * Convenience superclass for bean property expressions.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanPropertyExpression implements
        BeanPropertyExpression {
    private String propertyName;

    protected AbstractBeanPropertyExpression() {
    }

    protected AbstractBeanPropertyExpression(String propertyName) {
        setPropertyName(propertyName);
    }

    public String getPropertyName() {
        return propertyName;
    }

    protected void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public boolean test(Object o) {
        if (o instanceof PropertyAccessStrategy) {
            return test((PropertyAccessStrategy)o);
        }
        else {
            return test(new BeanPropertyAccessStrategy(o));
        }
    }

    protected abstract boolean test(
            PropertyAccessStrategy domainObjectAccessStrategy);

    public String toString() {
        return getPropertyName();
    }

}