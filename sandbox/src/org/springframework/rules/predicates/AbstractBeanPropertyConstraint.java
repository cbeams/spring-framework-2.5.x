/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.predicates;

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public abstract class AbstractBeanPropertyConstraint
    implements BeanPropertyConstraint {
    private String propertyName;

    public AbstractBeanPropertyConstraint(String propertyName) {
        setPropertyName(propertyName);
    }

    public String getPropertyName() {
        return propertyName;
    }

    protected void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

}
