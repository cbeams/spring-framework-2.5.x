/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import org.springframework.util.ToStringBuilder;
import org.springframework.validation.BeanValidationService;
import org.springframework.validation.BeanValidatorSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationResultsCollector;

/**
 * @author Keith Donald
 */
public class DefaultBeanValidationService implements BeanValidationService {
    private BeanValidatorSource beanValidatorSource;
    private BeanInfoBeanValidator validator = new BeanInfoBeanValidator();

    public DefaultBeanValidationService() {
        
    }
    
    public DefaultBeanValidationService(BeanValidatorSource source) {
        setBeanValidatorSource(source);
    }

    public void setBeanValidatorSource(BeanValidatorSource source) {
        this.beanValidatorSource = source;
        validator.setBeanValidatorSource(source);
    }

    public boolean supports(Class beanType) {
        return validator.hasValidators(beanType);
    }

    public void validate(Object bean, Errors errors) {
        ValidationResultsCollector results = new ValidationResultsErrorsCollector(
                errors);
        validate(bean, results);
    }

    public void validate(Object bean, ValidationResultsCollector results) {
        results.reset();
        validator.validateBean(bean, results);
    }

    public void validatePropertyValue(Object bean, String propertyName,
            Object value, ValidationResultsCollector results) {
        validator.validatePropertyValue(bean, propertyName, value, results);
    }

    public void validatePropertyValues(Object bean, String[] properties,
            Object[] values, ValidationResultsCollector results) {
        validator.validatePropertyValues(bean, properties, values, results);
    }

    public String toString() {
        return new ToStringBuilder(this).append("beanInfoBeanValidator",
                validator).append("validatorSource", beanValidatorSource)
                .toString();
    }
}
