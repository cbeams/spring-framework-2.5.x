package org.springframework.validation;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.rules.MaxLength;
import org.springframework.validation.rules.Required;
import org.springframework.validation.support.AttributesValidatorSource;
import org.springframework.validation.support.BeanValidatorBuilder;
import org.springframework.validation.support.DefaultBeanValidationService;
import org.springframework.validation.support.ValidationResultsCollectorAdapter;

/**
 * @author keith
 */
public class BeanValidatorTestSuite extends TestCase {
    AttributesValidatorSource validatorSource;
    BeanValidationService validationService;

    public void testBasicValidationWorkflow() {
        ValidationResultsCollectorAdapter results = new ValidationResultsCollectorAdapter();
        validationService.validate(getPet(), results);
    }

    public void testErrorsValidationWorkflow() {
        Pet pet = getPet();
        BindException b = new BindException(pet, "pet");
        validationService.validate(pet, b);
    }

    public Pet getPet() {
        Pet pet = new Pet();
        pet.setName(new Name());
        pet.setAge(0);
        return pet;
    }
    
    public void testValidatorBuilder() {
        BeanValidationService service = new DefaultBeanValidationService();
        BeanValidatorBuilder builder = new BeanValidatorBuilder(Pet.class);

        builder.setPropertyValidator("name.lastName", new Required());
        Set toyRules = new HashSet();
        toyRules.add(new Required());
        toyRules.add(new MaxLength(255));

        builder.setPropertyValidator("favoriteToy", toyRules);
        ValidationResultsCollectorAdapter results = new ValidationResultsCollectorAdapter();
        service.validate(getPet(), results);
    }

    public void testValidatorBuilderContext() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "org/springframework/validation/application-context.xml");
        BeanValidatorBuilder builder = (BeanValidatorBuilder)context
                .getBean("beanValidatorBuilder");
        BeanValidationService service = new DefaultBeanValidationService();
        ValidationResultsCollectorAdapter results = new ValidationResultsCollectorAdapter();
        service.validate(getPet(), results);
    }

    protected void setUp() {
        validatorSource = new AttributesValidatorSource();
        validationService = new DefaultBeanValidationService(validatorSource);
    }
}
