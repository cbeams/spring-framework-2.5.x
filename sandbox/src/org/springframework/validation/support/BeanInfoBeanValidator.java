/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.validation.BeanValidatorSource;
import org.springframework.validation.PropertyValidator;
import org.springframework.validation.ValidationResultsCollector;

/**
 * @author Keith Donald
 */
public class BeanInfoBeanValidator {
    public static final String IS_VALIDATED = "isValidated";
    public static final String VALIDATOR = "validator";

    private BeanValidatorSource beanValidatorSource;

    public void setBeanValidatorSource(BeanValidatorSource source) {
        this.beanValidatorSource = source;
    }

    public void validateBean(Object bean, ValidationResultsCollector results) {
        try {
            processBeanInfoValidators(bean.getClass(), bean, results);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public void validatePropertyValue(
        Object bean,
        String propertyName,
        Object value,
        ValidationResultsCollector results) {
        PropertyValidator validator = getPropertyValidator(bean, propertyName);
        validator.validatePropertyValue(bean, value, results);
    }

    public void validatePropertyValues(
        Object bean,
        String[] properties,
        Object[] values,
        ValidationResultsCollector results) {
        for (int i = 0; i < properties.length; i++) {
            validatePropertyValue(bean, properties[i], values[i], results);
        }
    }

    public boolean hasValidators(Class beanType) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
            return validated(beanInfo);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private PropertyValidator getPropertyValidator(
        Object bean,
        String propertyName) {
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        PropertyDescriptor descriptor =
            wrapper.getPropertyDescriptor(propertyName);
        return (PropertyValidator)descriptor.getValue(VALIDATOR);
    }

    private void processBeanInfoValidators(
        Class beanClass,
        Object bean,
        ValidationResultsCollector results)
        throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        if (!validated(beanInfo)) {
            return;
        }
        results.beanValidationStarted(bean);
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            if (BeanUtils.isSimpleProperty(property.getPropertyType())) {
                PropertyValidator validator =
                    (PropertyValidator)property.getValue(VALIDATOR);
                if (validator != null) {
                    validator.validateCurrentPropertyValue(bean, results);
                }
            } else {
                BeanWrapper wrapper = new BeanWrapperImpl(bean);
                processBeanInfoValidators(
                    property.getPropertyType(),
                    wrapper.getPropertyValue(property.getName()),
                    results);
            }
        }
        results.beanValidationCompleted(bean);
    }

    private boolean validated(BeanInfo beanInfo) {
        Boolean isValidated =
            (Boolean)beanInfo.getBeanDescriptor().getValue(IS_VALIDATED);
        if (isValidated == null) {
            if (beanValidatorSource != null) {
                beanValidatorSource.loadValidators(
                    beanInfo.getBeanDescriptor().getBeanClass());
                isValidated =
                    (Boolean)beanInfo.getBeanDescriptor().getValue(
                        IS_VALIDATED);
                Assert.notNull(isValidated);
            } else {
                return false;
            }
        }
        return isValidated.booleanValue();
    }

}
