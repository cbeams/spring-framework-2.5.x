/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.predicates;

import org.springframework.rules.UnaryPredicate;

/**
 * @author Keith Donald
 */
public interface BeanPropertyExpression extends UnaryPredicate {
    public String getPropertyName();
}
