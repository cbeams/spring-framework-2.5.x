package org.springframework.validation;

import junit.framework.TestCase;

import org.springframework.validation.BeanValidationService;
import org.springframework.validation.BindException;
import org.springframework.validation.support.AttributesValidatorSource;
import org.springframework.validation.support.DefaultBeanValidationService;
import org.springframework.validation.support.ValidationResultsCollectorAdapter;

/**
 * @author keith
 */
public class BeanValidatorTestSuite extends TestCase {
    AttributesValidatorSource validatorSource;
    BeanValidationService validationService;

    public void testBasicValidationWorkflow() {
        Pet pet = new Pet();
        pet.setName(new Name());
        pet.setAge(0);
        ValidationResultsCollectorAdapter results =
            new ValidationResultsCollectorAdapter();
        validationService.validate(pet, results);
    }

    public void testErrorsValidationWorkflow() {
        Pet pet = new Pet();
        pet.setName(new Name());
        pet.setAge(0);
        BindException b = new BindException(pet, "pet");
        validationService.validate(pet, b);
    }

    protected void setUp() {
        validatorSource = new AttributesValidatorSource();
        validationService = new DefaultBeanValidationService(validatorSource);
    }
}
