/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * A single property validation rule that can be declaratively associated with
 * one or more PropertyValidators. This interface should be implemented by all
 * rules, be them backed programatically or by a rules engine.
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
     * provided property is subject to this validation rule. A typing hint
     * provides assistance to the user when editing a field about what a single
     * validation rule is for a property.
     * 
     * @param propertyNamePath
     *            The property name path, could be qualified by object name,
     *            for example "pet.name.lastName"
     * @return The resolvable typing hint message.
     */
    public MessageSourceResolvable createTypingHint(String propertyNamePath);

    /**
     * Convenience method for rejecting a property value on a <code>Errors</code>
     * object.  
     * 
     * @param errors The errors object.
     * @param propertyNamePath The erred property name path.
     */
    public void invokeRejectValue(Errors errors, String propertyNamePath);
}
