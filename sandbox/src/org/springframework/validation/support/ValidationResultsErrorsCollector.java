/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import org.springframework.validation.PropertyValidationRule;
import org.springframework.validation.PropertyValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * @author Keith Donald
 */
public class ValidationResultsErrorsCollector
    extends ValidationResultsCollectorAdapter {
    private Errors errors;

    public ValidationResultsErrorsCollector() {

    }

    public ValidationResultsErrorsCollector(Errors errors) {
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }

    public void beanValidationStarted(Object bean) {
        super.beanValidationStarted(bean);
        if (errors == null) {
            this.errors = new BindException(bean, getObjectName(bean));
        }
    }

    public void validationErrorOccured(
        PropertyValidator validator,
        PropertyValidationRule rule,
        Object bean,
        Object value) {
        super.validationErrorOccured(validator, rule, bean, value);
        System.out.println(getFullNestedPath());
        rule.invokeRejectValue(errors, getNestedPath());
    }
}
