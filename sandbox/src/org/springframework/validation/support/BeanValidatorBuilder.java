/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanInfoSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringBuilder;
import org.springframework.validation.PropertyValidationRule;
import org.springframework.validation.PropertyValidator;

/**
 * Provides a convenient, programmatic way of configuring <code>PropertyValidationRules</code>
 * for constrained bean properties.
 * <p>
 * <p>
 * This class encapsulates instantiating a <code>PropertyValidator</code> for
 * each constrained property on a bean or its nested beans. It also associates
 * each <code>PropertyValidator</code> with the appropriate <code>PropertyDescriptor</code>.
 * This will facilitate the <code>BeanInfoBeanValidator</code>, used by the
 * <code>DefaultBeanValidationService</code>, to pick it the new validation
 * rules on subsequent validate attempts.
 * 
 * @author Keith Donald
 * @see org.springframework.validation.PropertyValidator
 * @see org.springframework.validation.PropertyValidationRule
 * @see BeanInfoBeanValidator
 * @see DefaultBeanValidationService
 */
public class BeanValidatorBuilder {
    private BeanInfoSupport beanInfoSupport;

    /**
     * Creates a BeanValidatorBuilder capable of creating validators on
     * properties of the specified bean type. Full support for creating
     * validators on this bean type's nested beans' properties is also
     * provided.
     * 
     * @param rootBeanType
     *            The root bean type to associate validation rules with.
     */
    public BeanValidatorBuilder(Class rootBeanType) {
        this.beanInfoSupport = new BeanInfoSupport(rootBeanType);
    }

    /**
     * Sets the root bean type.
     * 
     * @param rootBeanType
     *            The class or interface for which we which to attach property
     *            validation rules.
     * @throws IllegalArgumentException
     *             if the class argument is null.
     */
    public void setRootBeanType(Class rootBeanType) {
        this.beanInfoSupport.setRootBeanType(rootBeanType);
    }

    /**
     * Configures a set of property validators for the <code>rootBeanType</code>.
     * <p>
     * <p>
     * Structurally, the map should consist of the following:
     * <ul>
     * <li>Each key should be the constrained property name
     * (java.lang.String). Support for nested properties separated by dots is
     * provided.
     * <li>Each value should be a set of one or more PropertyValidationRules
     * that constrain that property.
     * </ul>
     * Note: this method will overwrite any PropertyValidators previous
     * associated with property name keys specified in the map.
     * 
     * @param propertyValidators
     *            A map containing for each validated property name, the
     *            PropertyValidationRules that apply.
     */
    public void setPropertyValidators(Map propertyValidators) {
        for (Iterator i = propertyValidators.entrySet().iterator();
            i.hasNext();
            ) {
            Map.Entry entry = (Map.Entry)i.next();
            String propertyName = (String)entry.getKey();
            Object o = entry.getValue();
            if (o instanceof Set) {
                Set propertyValidationRules = (Set)o;
                setPropertyValidator(propertyName, propertyValidationRules);
            } else {
                setPropertyValidator(propertyName, (PropertyValidationRule)o);
            }
        }
    }

    /**
     * Constrains the specified property name by a single
     * PropertyValidationRule.
     * 
     * @param propertyName
     *            The validated property name.
     * @param propertyValidationRule
     *            The constraining property validation rule.
     */
    public void setPropertyValidator(
        String propertyName,
        PropertyValidationRule rule) {
        Set propertyValidationRules = new HashSet(1);
        propertyValidationRules.add(rule);
        setPropertyValidator(propertyName, propertyValidationRules);
    }

    /**
     * Constrains the specified property name by the provided set of
     * PropertyValidationRules.
     * 
     * @param propertyName
     *            The validated property name.
     * @param propertyValidationRules
     *            The set of constraining property validation rules.
     */
    public void setPropertyValidator(
        String propertyNamePath,
        Set propertyValidationRules) {
        Assert.hasText(propertyNamePath);
        Assert.hasElements(propertyValidationRules);
        BeanInfo beanInfo = beanInfoSupport.getParentBeanInfo(propertyNamePath);
        String propertyName = StringUtils.unqualify(propertyNamePath);
        PropertyDescriptor property =
            beanInfoSupport.getPropertyDescriptor(beanInfo, propertyName);
        Assert.notNull(
            property,
            "No property found '" + propertyNamePath + "'");
        PropertyValidator validator =
            new PropertyValidatorImpl(propertyName, propertyValidationRules);
        property.setValue(
            BeanValidatorConstants.VALIDATED_PROPERTY,
            Boolean.TRUE);
        property.setValue(BeanValidatorConstants.VALIDATOR_PROPERTY, validator);
        beanInfo.getBeanDescriptor().setValue(
            BeanValidatorConstants.VALIDATED_PROPERTY,
            Boolean.TRUE);
    }

    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
