/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

/**
 * The main interface to the declarative bean validation subsystem. This
 * interface extends the core Spring Validator interface, adding additional
 * validate() methods for validating proposed property values. In addition, the
 * results collector provides additional callbacks for tracking validation
 * results.
 * 
 * @author Keith Donald
 */
public interface BeanValidationService
    extends org.springframework.validation.Validator {

    /**
     * Validate the specfied bean and collect the validation results.
     * <p>
     * <p>
     * This method is expected to iterate over each property validators for
     * this bean and call validateCurrentPropertyValue(), as well as any nested
     * validated beans and their property validators. Existing property values
     * set on the bean bean are validated.
     * 
     * @param bean
     *            The bean to validate.
     * @param results
     *            The validation results collector.
     */
    public void validate(Object bean, ValidationResultsCollector results);

    /**
     * Validate a bean's proposed property value change.
     * <p>
     * <p>
     * The specfied property may be a direct simple property, or a nested bean
     * property (qualified with <code>'.'</code> separators.)
     * 
     * @param bean
     *            The bean to validate.
     * @param propertyName
     *            The property name.
     * @param value
     *            The proposed property value.
     * @param results
     *            The validation results collector.
     */
    public void validatePropertyValue(
        Object bean,
        String propertyName,
        Object value,
        ValidationResultsCollector results);

    /**
     * Convenience method for validating multiple proposed property changes.
     * 
     * @param bean
     *            The bean to validate.
     * @param properties
     *            The property names
     * @param values
     *            The proposed property values (in matching order.)
     * @param results
     *            The validation results collector.
     */
    public void validatePropertyValues(
        Object bean,
        String[] properties,
        Object[] values,
        ValidationResultsCollector results);
}
