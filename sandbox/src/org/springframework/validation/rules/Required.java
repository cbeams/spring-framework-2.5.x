/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import org.springframework.util.StringUtils;

/**
 * Validates a required property.
 * 
 * @author Keith Donald
 */
public class Required extends
        AbstractPropertyValidationRule {

    public boolean validate(Object o, Object value) {
        return validateRequired(value);
    }

    private boolean validateRequired(Object value) {
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

}
