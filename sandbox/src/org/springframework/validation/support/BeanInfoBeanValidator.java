/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.validation.BeanValidatorSource;
import org.springframework.validation.PropertyValidator;
import org.springframework.validation.ValidationResultsCollector;

/**
 * Encapsulates the algorithm for validated a bean, where property validators
 * are stored within the bean's <code>BeanInfo</code> metadata objects.
 * 
 * @author Keith Donald
 */
public class BeanInfoBeanValidator {
    private static final Log logger = LogFactory
            .getLog(BeanInfoBeanValidator.class);

    private BeanValidatorSource beanValidatorSource;

    /**
     * Sets the source to load validators from in the event no validators can
     * be found for a <code>bean</code> passed to one of the <code>validate*</code>
     * methods. If this is null, it is expected that all validators have
     * already configured and attached to the appropriate BeanInfo instances.
     * 
     * @param source
     *            The validator source, to load validators on demand.
     */
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

    public void validatePropertyValue(Object bean, String propertyName,
            Object value, ValidationResultsCollector results) {
        PropertyValidator validator = getPropertyValidator(bean, propertyName);
        if (validator == null) {
            logger.warn("No property validator found for property '"
                    + propertyName + "'");
        }
        validator.validatePropertyValue(bean, value, results);
    }

    public void validatePropertyValues(Object bean, String[] properties,
            Object[] values, ValidationResultsCollector results) {
        for (int i = 0; i < properties.length; i++) {
            validatePropertyValue(bean, properties[i], values[i], results);
        }
    }

    /**
     * Checks if the specified bean type has any attached property validators.
     * 
     * @param beanType
     *            The bean type.
     * @return true if the bean is validateable, false otherwise.
     */
    public boolean hasValidators(Class beanType) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
            return validated(beanInfo);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private PropertyValidator getPropertyValidator(Object bean,
            String propertyName) {
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        PropertyDescriptor descriptor = wrapper
                .getPropertyDescriptor(propertyName);
        return (PropertyValidator)descriptor
                .getValue(BeanValidatorConstants.VALIDATOR_PROPERTY);
    }

    private void processBeanInfoValidators(Class beanClass, Object bean,
            ValidationResultsCollector results) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        if (!validated(beanInfo)) {
            return;
        }
        results.beanValidationStarted(bean);
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            if (BeanUtils.isSimpleProperty(property.getPropertyType())) {
                PropertyValidator validator = (PropertyValidator)property
                        .getValue(BeanValidatorConstants.VALIDATOR_PROPERTY);
                if (validator != null) {
                    validator.validateCurrentPropertyValue(bean, results);
                }
            } else {
                BeanWrapper wrapper = new BeanWrapperImpl(bean);
                processBeanInfoValidators(property.getPropertyType(), wrapper
                        .getPropertyValue(property.getName()), results);
            }
        }
        results.beanValidationCompleted(bean);
    }

    private boolean validated(BeanInfo beanInfo) {
        Boolean isValidated = (Boolean)beanInfo.getBeanDescriptor().getValue(
                BeanValidatorConstants.VALIDATED_PROPERTY);
        if (isValidated == null) {
            if (beanValidatorSource != null) {
                beanValidatorSource.loadValidators(beanInfo.getBeanDescriptor()
                        .getBeanClass());
                isValidated = (Boolean)beanInfo.getBeanDescriptor().getValue(
                        BeanValidatorConstants.VALIDATED_PROPERTY);
                Assert
                        .notNull(isValidated,
                                "The validator source did not update the beanInfo's isValidated property.");
            } else {
                return false;
            }
        }
        return isValidated.booleanValue();
    }

}
