/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.constraints.beans;

import org.springframework.rules.Constraint;

/**
 * A predicate that constrains a bean property in some way.
 * 
 * @author Keith Donald
 */
public interface BeanPropertyConstraint extends Constraint {
    
    /**
     * Returns the constrained property name.
     * 
     * @return The property name
     */
    public String getPropertyName();
}