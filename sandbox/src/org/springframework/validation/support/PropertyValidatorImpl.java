/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;
import org.springframework.validation.PropertyValidationRule;
import org.springframework.validation.PropertyValidator;
import org.springframework.validation.ValidationResultsCollector;

public class PropertyValidatorImpl implements PropertyValidator {
    private String propertyName;
    private Set validationRules;
    private boolean continueOnError;

    public PropertyValidatorImpl(String propertyName) {
        setPropertyName(propertyName);
        this.validationRules = new HashSet(3);
    }

    public PropertyValidatorImpl(String propertyName, Set rules) {
        setPropertyName(propertyName);
        this.validationRules = new HashSet(rules);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    public MessageSourceResolvable[] getTypingHints(String nestedPathPrefix) {
        MessageSourceResolvable[] typingHints = new MessageSourceResolvable[validationRules
                .size()];
        int index = 0;
        for (Iterator i = validationRules.iterator(); i.hasNext();) {
            PropertyValidationRule rule = (PropertyValidationRule)i.next();
            if (nestedPathPrefix != null) {
                typingHints[index++] = rule.createTypingHint(nestedPathPrefix
                        + '.' + propertyName);
            } else {
                typingHints[index++] = rule.createTypingHint(propertyName);
            }
        }
        return typingHints;
    }

    public void add(PropertyValidationRule rule) {
        validationRules.add(rule);
    }

    public void addAll(PropertyValidationRule[] rules) {
        for (int i = 0; i < rules.length; i++) {
            add(rules[i]);
        }
    }

    public void validateCurrentPropertyValue(Object bean,
            ValidationResultsCollector results) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        validatePropertyValue(bean, beanWrapper
                .getPropertyValue(getPropertyName()), results);
    }

    public void validatePropertyValue(Object bean, Object value,
            ValidationResultsCollector results) {
        Assert.notNull(bean);
        Assert.notNull(results);
        results.propertyValidationStarted(this, bean, value);
        for (Iterator i = validationRules.iterator(); i.hasNext();) {
            PropertyValidationRule rule = (PropertyValidationRule)i.next();
            if (!rule.validate(bean, value)) {
                results.validationErrorOccured(this, rule, bean, value);
                if (!continueOnError) {
                    break;
                }
            }
        }
        results.propertyValidationCompleted(this, bean, value);
    }

    public String toString() {
        return new ToStringBuilder(this).append("propertyName", propertyName)
                .append("validationRules", validationRules).append(
                        "continueOnError", continueOnError).toString();
    }
}
