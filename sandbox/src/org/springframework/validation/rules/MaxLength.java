/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import org.springframework.util.Assert;

public class MaxLength extends AbstractPropertyValidationRule {
    private int length;

    public MaxLength(int length) {
        Assert.isTrue(length > 0);
        this.length = length;
    }

    public boolean validate(Object object, Object value) {
        Assert.isTrue(object instanceof String);
        String str = (String)value;
        return (str.length() <= length ? true : false);
    }

    public Object[] getErrorArguments() {
        return new Object[] { new Integer(length) };
    }

}
