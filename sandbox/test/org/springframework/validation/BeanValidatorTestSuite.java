package org.springframework.validation;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.rules.MaxLength;
import org.springframework.validation.rules.Required;
import org.springframework.validation.support.BeanValidatorBuilder;
import org.springframework.validation.support.DefaultBeanValidationService;
import org.springframework.validation.support.ValidationResultsCollectorAdapter;

/**
 * @author keith
 */
public class BeanValidatorTestSuite extends TestCase {
    ApplicationContext context;

    public void testAttributesValidatorSource() {
        getAttributesBeanValidatorSource().loadValidators(Pet.class);
    }
    public void testBasicValidationWorkflow() {
        ValidationResultsCollectorAdapter results =
            new ValidationResultsCollectorAdapter();
        getBeanValidationService().validate(getPet(), results);
    }

    public void testErrorsValidationWorkflow() {
        Pet pet = getPet();
        BindException b = new BindException(pet, "pet");
        getBeanValidationService().validate(pet, b);
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
        ValidationResultsCollectorAdapter results =
            new ValidationResultsCollectorAdapter();
        service.validate(getPet(), results);
    }

    public void testValidatorBuilderContext() {
        BeanValidationService service = new DefaultBeanValidationService();
        ValidationResultsCollectorAdapter results =
            new ValidationResultsCollectorAdapter();
        service.validate(getPet(), results);
    }

    public BeanValidatorBuilder getBeanValidatorBuilder() {
        return (BeanValidatorBuilder)context.getBean("beanValidatorBuilder");
    }

    public BeanValidationService getBeanValidationService() {
        return (BeanValidationService)context.getBean("beanValidationService");
    }

    public BeanValidatorSource getAttributesBeanValidatorSource() {
        return (BeanValidatorSource)context.getBean(
            "attributesValidatorSource");
    }

    protected void setUp() {
        context =
            new ClassPathXmlApplicationContext("org/springframework/validation/application-context.xml");
    }
}
