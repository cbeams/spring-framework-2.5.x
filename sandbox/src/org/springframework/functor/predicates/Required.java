/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.functor.predicates;

import org.springframework.functor.UnaryPredicate;
import org.springframework.util.StringUtils;

/**
 * Validates a required property.
 * 
 * @author Keith Donald
 */
public class Required implements UnaryPredicate {
    public boolean evaluate(Object value) {
        if (value != null) {
            if (value instanceof String) {
                if (StringUtils.hasText((String)value)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
    
    public static UnaryPredicate instance() {
        return instance;
    }
    
    private static final Required instance = new Required();

}
