/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;
import org.springframework.util.ToStringBuilder;
import org.springframework.validation.BeanValidatorSource;
import org.springframework.validation.PropertyValidationRule;

/**
 * A source that loads validators from attributes metadata declared in source
 * markup. Currently this implementation uses commons-attributes.
 * 
 * @author Seth Ladd
 * @author Keith Donald
 */
public class AttributesValidatorSource implements BeanValidatorSource {
    private static final Log logger = LogFactory
            .getLog(AttributesValidatorSource.class);
    private Attributes attributeResolver = new CommonsAttributes();

    public AttributesValidatorSource() {

    }

    /**
     * Find all getters that have validation attributes on them. Checks if the
     * attribute is instanceof <code>ValidationRule</code>.
     * 
     * @param methods
     *            all methods on a class.
     * @return collection of ValidationRule attributes.
     */
    private void buildValidators(BeanInfo beanInfo) {
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        boolean validated = false;
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            Method readMethod = property.getReadMethod();
            if (readMethod != null) {
                Collection validationRules = getPropertyValidationRules(readMethod);
                if (validationRules.size() > 0) {
                    PropertyValidatorImpl propValidator = new PropertyValidatorImpl(
                            property.getName());
                    propValidator
                            .addAll((PropertyValidationRule[])validationRules
                                    .toArray(new PropertyValidationRule[0]));
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Found property validator via attributes '"
                                        + propValidator + "'");
                    }
                    property.setValue(
                            BeanValidatorConstants.VALIDATED_PROPERTY,
                            Boolean.TRUE);
                    property.setValue(
                            BeanValidatorConstants.VALIDATOR_PROPERTY,
                            propValidator);
                    if (!validated) {
                        validated = true;
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No validation rules found on method '"
                                + readMethod + "'");
                    }
                }
                setValidated(beanInfo, validated);
                Class propertyType = property.getPropertyType();
                if (!isSimpleProperty(propertyType)) {
                    loadValidators(propertyType);
                }
            }
        }
    }

    private void setValidated(BeanInfo beanInfo, boolean validated) {
        if (validated) {
            beanInfo.getBeanDescriptor().setValue(
                    BeanValidatorConstants.VALIDATED_PROPERTY, Boolean.TRUE);
        } else {
            beanInfo.getBeanDescriptor().setValue(
                    BeanValidatorConstants.VALIDATED_PROPERTY, Boolean.FALSE);
        }
    }

    public void loadValidators(Class beanType) {
        try {
            BeanInfo nestedBeanInfo = Introspector.getBeanInfo(beanType);
            buildValidators(nestedBeanInfo);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSimpleProperty(Class type) {
        return BeanUtils.isSimpleProperty(type);
    }

    /**
     * Return all attributes from a method that are of type <code>ValidationRule</code>.
     * 
     * @param method
     *            the method that might have validation attributes
     * @return the validation attributes
     */
    private Collection getPropertyValidationRules(Method method) {
        Collection attributes = attributeResolver.getAttributes(method);
        Collection rules = new ArrayList();
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Object attribute = i.next();
            if (attribute instanceof PropertyValidationRule) {
                rules.add(attribute);
            }
        }
        return rules;
    }

    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
