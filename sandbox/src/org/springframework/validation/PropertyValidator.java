/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

import org.springframework.context.MessageSourceResolvable;

/**
 * A declarative validator for a single bean property. Property validators
 * encapsulate one or more <code>PropertyValidationRules</code> that
 * constrain a property's allowed values.
 * 
 * @author Keith Donald
 */
public interface PropertyValidator {
    
    /**
     * Get the typing hints associated with this property. The nestedPathPrefix
     * is an optional object qualifier prefix used for populating message
     * arguments.
     * 
     * @param nestedPathPrefix
     *            the optional path prefix, for example "pet"
     * @return The resolvable typing hints for this property.
     */
    public MessageSourceResolvable[] getTypingHints(String nestedPathPrefix);

    /**
     * Returns the name of the validated property.
     * 
     * @return The property name.
     */
    public String getPropertyName();

    /**
     * Sets the name of the validated property.
     * 
     * @param propertyName
     */
    public void setPropertyName(String propertyName);

    /**
     * Sets whether this validator should attempt to process subsequent
     * PropertyValidationRules if a validation error occurs.
     * 
     * @param continueOnError
     *            true = process all rules, false = stop on any error.
     */
    public void setContinueOnError(boolean continueOnError);
    
    /**
     * Validates the proposed property value change.
     * 
     * @param bean the validated bean.
     * @param value the proposed property value.
     * @param results the validation results collector.
     */
    public void validatePropertyValue(
        Object bean,
        Object value,
        ValidationResultsCollector results);
    
    /**
     * Validates the existing value of this property.
     * 
     * @param bean the validated bean.
     * @param results the validation results collector.
     */
    public void validateCurrentPropertyValue(
        Object bean,
        ValidationResultsCollector results);
}