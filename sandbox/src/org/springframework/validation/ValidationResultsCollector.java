/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

/**
 * Collector interface used to track validation progress and report any error
 * results.  The order of the callbacks is as follows:
 * <code>
 * validationService.validate(bean, results);
 * results.beanValidationStarted(bean);
 * results.propertyValidationStarted(property);
 * results.propertyValidationEnded(property)
 * results.validationErrorOccured(property, rule);
 * ... repeat for all properties ...
 * results.beanValidationEnded(bean);
 * </code>
 * @author Keith Donald
 */
public interface ValidationResultsCollector {

    /**
     * Callback indicating the validation process has started for this bean.
     * 
     * @param bean
     *            the validated bean.
     */
    public void beanValidationStarted(Object bean);
    
    /**
     * Callback indicating the validation process has completed for this bean.
     * 
     * @param bean
     *            the validated bean.
     */
    public void beanValidationCompleted(Object bean);

    /**
     * Callback indicating the validation process has started for a property.
     * 
     * @param validator the property validator (@TODO too much?)
     * @param bean the validated bean (@TODO not needed?)
     * @param value the property value to be validated.
     */
    public void propertyValidationStarted(
        PropertyValidator validator,
        Object bean,
        Object value);
    
    /**
     * Callback indicating the validation process has started for a property.
     * 
     * @param validator the property validator (@TODO too much?)
     * @param bean the validated bean (@TODO not needed?)
     * @param value the property value to be validated.
     */
    public void propertyValidationCompleted(
        PropertyValidator validator,
        Object bean,
        Object value);
    
    /**
     * Callback indicating a validation error occured while validating the
     * specified property validation rule.
     * 
     * @param validator the property validator
     * @param rule the validation rule that detected the failure
     * @param bean the validated bean
     * @param value the value that failed validation
     */
    public void validationErrorOccured(
        PropertyValidator validator,
        PropertyValidationRule rule,
        Object bean,
        Object value);
    
    /**
     * Tell the collector to reset() as a new validation session is about
     * to commence.
     */
    public void reset();
}
