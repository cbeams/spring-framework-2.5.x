/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.predicates;

import org.springframework.rules.UnaryPredicate;
import org.springframework.util.Assert;

/**
 * @author  Keith Donald
 */
public abstract class AbstractBeanPropertyExpression implements UnaryPredicate {
    private String propertyName;
    
    public AbstractBeanPropertyExpression(String propertyName) {
        setPropertyName(propertyName);
    }
    
    protected String getPropertyName() {
        return propertyName;
    }
    
    protected void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

}
