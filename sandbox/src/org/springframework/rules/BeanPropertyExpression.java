/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

/**
 * A predicate that constrains a bean property in some way.
 * 
 * @author Keith Donald
 */
public interface BeanPropertyExpression extends UnaryPredicate {
    
    /**
     * Returns the constrained property name.
     * 
     * @return The property name
     */
    public String getPropertyName();
}