/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.Cache;
import org.springframework.util.ClassUtils;

/**
 * Support class for simplifying working with the BeanInfo related APIs. Mainly
 * fills gaps in the existing javabeans API.
 * 
 * @author Keith Donald
 */
public class BeanInfoSupport {
    private static final Log logger = LogFactory.getLog(BeanInfoSupport.class);
    private BeanInfo rootBeanInfo;
    private Class rootBeanType;

    /**
     * Cache of BeanInfo keys to a map of PropertyDescriptors indexed by name.
     */
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

    /**
     * Create a BeanInfoSupport for the specified bean type. This type is
     * referred to as the "root" bean. Any nested bean properties or simple
     * properties may be accessed using standard nested property notation (dot
     * separators.)
     * 
     * @param rootBeanType
     *            The root bean type to introspect.
     */
    public BeanInfoSupport(Class rootBeanType) {
        setRootBeanType(rootBeanType);
    }

    /**
     * Sets the root bean type.
     * 
     * @param beanType
     * @throws IllegalArgumentException
     *             if the class argument is null.
     */
    public void setRootBeanType(Class beanType) {
        Assert.notNull(beanType);
        this.rootBeanType = beanType;
        beanInfoProperties.clear();
        this.rootBeanInfo = getBeanInfo(this.rootBeanType);
        Assert.notNull(
            rootBeanInfo,
            "Unable to retrive root bean type " + beanType + " BeanInfo.");
    }

    /**
     * Returns the BeanInfo of the root bean type.
     * 
     * @return The root BeanInfo.
     */
    public BeanInfo getRootBeanInfo() {
        return rootBeanInfo;
    }

    /**
     * Returns the parent bean info of the specified nested property name path.
     * As an example, asking for property "name" will return the root BeanInfo,
     * while asking for "name.lastName", will return Name's BeanInfo.
     * 
     * @param propertyNamePath
     *            The property name path, separated by dots.
     * @return The parent BeanInfo.
     * @throws IllegalArgumentException
     *             if the property is null or does not exist.
     */
    public BeanInfo getParentBeanInfo(String propertyNamePath) {
        Assert.notNull(propertyNamePath);
        int lastDot = propertyNamePath.lastIndexOf('.');
        if (lastDot == -1) {
            PropertyDescriptor property =
                getPropertyDescriptor(rootBeanInfo, propertyNamePath);
            Assert.notNull(
                property,
                "No such property " + propertyNamePath + " on root bean.");
            return rootBeanInfo;
        }
        return getNestedBeanInfo(propertyNamePath.substring(0, lastDot));
    }

    /**
     * Returns the BeanInfo of a nested bean property, which is also another
     * bean.
     * 
     * @param nestedBeanPropertyNamePath
     * @return The nested bean type's BeanInfo.
     * @throws IllegalArgumentException
     *             if the property path is null or if the path points to a
     *             simple property and not a bean property
     */
    public BeanInfo getNestedBeanInfo(String nestedBeanPropertyNamePath) {
        PropertyDescriptor property =
            getPropertyDescriptor(nestedBeanPropertyNamePath);
        Assert.notNull(
            property,
            "No nested bean property found starting with "
                + nestedBeanPropertyNamePath);
        Assert.isTrue(
            !BeanUtils.isSimpleProperty(property.getPropertyType()),
            "Property name '"
                + ClassUtils.getShortName(
                    rootBeanInfo.getBeanDescriptor().getBeanClass())
                + "."
                + nestedBeanPropertyNamePath
                + "' is a simple property; must be a bean.");
        return getBeanInfo(property.getPropertyType());
    }

    /**
     * Returns the property descriptor at the specified property name path.
     * Retrieving properise of nested beans of the root bean type are fully
     * supported.
     * 
     * @param propertyNamePath
     *            The property name path.
     * @return The property descriptor
     * @throws IllegalArgumentException
     *             if the propertyName is null or does not exist.
     */
    public PropertyDescriptor getPropertyDescriptor(String propertyNamePath) {
        Assert.hasText(propertyNamePath);
        List propertyList = buildNestedPropertyTokens(propertyNamePath);
        return getPropertyDescriptor(rootBeanInfo, propertyList);
    }

    private List buildNestedPropertyTokens(String propertyName) {
        List propertyTokens = new ArrayList();
        StringTokenizer tokens = new StringTokenizer(propertyName, ".");
        while (tokens.hasMoreTokens()) {
            propertyTokens.add(tokens.nextToken());
        }
        return propertyTokens;
    }

    private BeanInfo getBeanInfo(Class type) {
        try {
            return Introspector.getBeanInfo(type);
        } catch (IntrospectionException e) {
            logger.error("Unable to introspect bean type " + type, e);
            return null;
        }
    }

    private PropertyDescriptor getPropertyDescriptor(
        BeanInfo beanInfo,
        List nestedPropertyTokens) {
        if (nestedPropertyTokens.size() > 1) {
            String token = (String)nestedPropertyTokens.get(0);
            nestedPropertyTokens.remove(0);
            PropertyDescriptor property =
                getPropertyDescriptor(beanInfo, token);
            Assert.notNull(
                property,
                "No property found starting with " + token);
            Assert.isTrue(
                !BeanUtils.isSimpleProperty(property.getPropertyType()),
                "Qualifying property name token '"
                    + ClassUtils.getShortName(
                        beanInfo.getBeanDescriptor().getBeanClass())
                    + "."
                    + token
                    + "' is a simple property; must be a bean.");
            return getPropertyDescriptor(
                getBeanInfo(property.getPropertyType()),
                nestedPropertyTokens);
        } else {
            String propertyName = (String)nestedPropertyTokens.get(0);
            PropertyDescriptor property =
                getPropertyDescriptor(beanInfo, propertyName);
            Assert.notNull(property, "No property found " + propertyName);
            return property;
        }
    }

    /**
     * Returns a child property <i>directly</i> associated with this
     * BeanInfo. This is a convenience method for accessing a cache of
     * PropertyDescriptors indexed by name for a given BeanInfo. No nested
     * property support is provided.
     * 
     * @param beanInfo
     *            The beanInfo
     * @param propertyName
     *            The property name of this beanInfo.
     * @return The PropertyDescriptor, or null if it does not exist.
     * @throws IllegalArgumentException,
     *             if either parameters are null.
     */
    public PropertyDescriptor getPropertyDescriptor(
        BeanInfo beanInfo,
        String propertyName) {
        Assert.notNull(beanInfo);
        Assert.notNull(propertyName);
        Map beanProperties = (Map)beanInfoProperties.get(beanInfo);
        return (PropertyDescriptor)beanProperties.get(propertyName);
    }

}
