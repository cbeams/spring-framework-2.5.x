/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

import org.springframework.context.MessageSourceResolvable;

/**
 * A single property validation rule that can be declaratively associated with
 * one or more PropertyValidators. This interface should be implemented by all
 * rules.
 * 
 * @author Keith Donald
 */
public interface PropertyValidationRule {

    /**
     * Validate the specified property value.
     * 
     * @param context
     *            The context in which to evalate the value. This is generally
     *            the bean itself.
     * @param value
     *            The property value.
     * @return <code>true -> validation successful</code>, or <code>false -> validation failed</code>
     */
    public boolean validate(Object context, Object value);

    /**
     * Creates a typing hint for the specified property. This assumes the
     * property is subject to this validation rule. A typing hint provides
     * assistance to the user when editing a field about how the validation
     * rule constrains input.
     * 
     * @param propertyNamePath
     *            The property name path, could be qualified by object name,
     *            for example "pet.name.lastName"
     * @return The resolvable typing hint message.
     */
    public MessageSourceResolvable createTypingHint(String propertyNamePath);

    /**
     * Creates a validation error message for the specified property. This
     * assumes validation failure was caused by this validation rule.
     * 
     * @param propertyNamePath
     *            The property name path, could be qualified by object name,
     *            for example "pet.name.lastName"
     * @return The resolvable error message.
     */
    public MessageSourceResolvable createErrorMessage(String propertyNamePath);

    /**
     * Convenience method for rejecting a property value on an <code>Errors</code>
     * object.
     * 
     * @param errors
     *            The errors object.
     * @param propertyNamePath
     *            The erred property name path.
     */
    public void invokeRejectValue(Errors errors, String propertyNamePath);
}
