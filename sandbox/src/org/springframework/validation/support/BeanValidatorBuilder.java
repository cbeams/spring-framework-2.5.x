/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.Cache;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringBuilder;

public class BeanValidatorBuilder {
    private Class rootBeanType;
    private BeanInfo beanInfo;
    private Cache beanInfoProperties = new Cache() {
        public Object create(Object key) {
            BeanInfo beanInfo = (BeanInfo)key;
            Map beanProperties = new HashMap();
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < properties.length; i++) {
                PropertyDescriptor property = properties[i];
                beanProperties.put(property.getName(), property);
            }
            return beanProperties;
        }
    };

    public BeanValidatorBuilder(Class rootBeanType) {
        setRootBeanType(rootBeanType);
    }

    public void setRootBeanType(Class rootBeanType) {
        Assert.notNull(rootBeanType);
        this.rootBeanType = rootBeanType;
        this.beanInfo = getBeanInfo(this.rootBeanType);
    }

    private PropertyDescriptor getPropertyDescriptor(BeanInfo beanInfo,
            String propertyName) {
        Map beanProperties = (Map)beanInfoProperties.get(beanInfo);
        return (PropertyDescriptor)beanProperties.get(propertyName);
    }

    public void addPropertyValidator(String propertyName,
            Set propertyValidationRules) {
        Assert.isTrue(StringUtils.hasText(propertyName));
        Assert.hasElements(propertyValidationRules);
        addPropertyValidator(beanInfo, buildPropertyList(propertyName),
                propertyValidationRules);
    }

    private List buildPropertyList(String propertyName) {
        List propertyList = new ArrayList();
        StringTokenizer tokens = new StringTokenizer(propertyName, ".");
        while (tokens.hasMoreTokens()) {
            propertyList.add(propertyName);
        }
        return propertyList;
    }

    private void addPropertyValidator(BeanInfo beanInfo, List propertyList,
            Set propertyValidationRules) {
        if (propertyList.size() > 0) {
            String token = (String)propertyList.get(0);
            propertyList.remove(0);
            PropertyDescriptor property = getPropertyDescriptor(beanInfo, token);
            Assert
                    .notNull(property, "No property found starting with "
                            + token);
            Assert.isTrue(!BeanUtils.isSimpleProperty(property
                    .getPropertyType()), "Qualifying property name token '"
                    + token + "' is a simple property; must be a bean.");
            addPropertyValidator(getBeanInfo(property.getPropertyType()),
                    propertyList, propertyValidationRules);
        } else {
            String propertyName = (String)propertyList.get(0);
            PropertyDescriptor property = getPropertyDescriptor(beanInfo,
                    propertyName);
            Assert.notNull(property, "No property found " + propertyName);
            PropertyValidatorImpl validator = new PropertyValidatorImpl(
                    propertyName, propertyValidationRules);
            property.setValue(BeanValidatorConstants.VALIDATED_PROPERTY,
                    Boolean.TRUE);
            property.setValue(BeanValidatorConstants.VALIDATOR_PROPERTY,
                    validator);
        }
    }

    private BeanInfo getBeanInfo(Class type) {
        try {
            return Introspector.getBeanInfo(type);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
