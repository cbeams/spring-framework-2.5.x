/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validates property value as 'required' if and only if some other property is
 * also provided.
 * 
 * @author Seth Ladd
 * @author Keith Donald
 */
public class RequiredIfPresent extends AbstractPropertyValidationRule {
    private String otherPropertyName;

    private Required required = new Required();

    public RequiredIfPresent(String otherPropertyName) {
        this.otherPropertyName = otherPropertyName;
    }

    public boolean validate(Object context, Object value) {
        BeanWrapper wrapper = new BeanWrapperImpl(context);
        Object otherValue = wrapper.getPropertyValue(otherPropertyName);
        if (required.validate(context, otherValue)) {
            return required.validate(context, value);
        } else {
            return true;
        }
    }

    public Object[] getErrorArguments() {
        return new Object[] {
             buildPropertyMessageSourceResolvable(otherPropertyName)};
    }

}
