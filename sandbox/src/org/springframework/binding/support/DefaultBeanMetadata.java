/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.binding.BeanMetadata;
import org.springframework.binding.PropertyMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class DefaultBeanMetadata implements BeanMetadata {

    private final Log logger = LogFactory.getLog(getClass());

    public String getCaption() {
        return null;
    }
    public String getDescription() {
        return null;
    }
    
    public Collection getPropertyMetadataCollection() {
        return null;
    }
    
    public PropertyMetadata getPropertyMetadata(String propertyPath) {
        return null;
    }
    
    public Class getType() {
        return null;
    }

    public Object newInstance() {
        return null;
    }
    
    /* Map with cached nested DynaClasses */
    private Map nestedDynaProperties;

    private String name = getClass().getName();

    private boolean restricted;

    private String nestedPropertyName;

    private String nestedPath = "";

    private Map dynaProperties;

    public DefaultBeanMetadata() {

    }

    private DefaultBeanMetadata(String nestedPropertyName, String nestedPropertyPath) {
        this.nestedPropertyName = nestedPropertyName;
        this.nestedPath = nestedPropertyPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(String propertyPath) {
        assertNotRestricted();
        add(propertyPath, AbstractBeanAccessor.class, true, true);
    }

    public void add(String propertyPath, Class type, boolean readable, boolean writeable) {
        assertNotRestricted();
        DefaultBeanMetadata nested = getDynaClassForPropertyPath(propertyPath);
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nested, propertyPath));
        nested.add(tokens, type, true, true);
    }

    protected void add(PropertyTokenHolder tokens, Class type, boolean reable, boolean writeable) {
        //dynaProperties.put(tokens.actualName, new SmartDynaProperty(tokens.actualName, type));
    }

    private String getFinalPath(DefaultBeanMetadata clazz, String nestedPath) {
        if (clazz == this) {
            return nestedPath;
        }
        return nestedPath.substring(getNestedPropertySeparatorIndex(nestedPath, true) + 1);
    }

    protected DefaultBeanMetadata getDynaClassForPropertyPath(String propertyPath) {
        int pos = getNestedPropertySeparatorIndex(propertyPath, false);
        // handle nested properties recursively
        if (pos > -1) {
            String nextPropertyName = propertyPath.substring(0, pos);
            String remainingPropertyPath = propertyPath.substring(pos + 1);
            DefaultBeanMetadata next = getNestedDynaClass(nextPropertyName);
            return next.getDynaClassForPropertyPath(remainingPropertyPath);
        }
        else {
            return this;
        }
    }

    /**
     * Determine the first respectively last nested property separator in the
     * given property path, ignoring dots in keys (like "map[my.key]").
     * @param propertyPath the property path to check
     * @param last whether to return the last separator rather than the first
     * @return the index of the nested property separator, or -1 if none
     */
    private int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
        boolean inKey = false;
        int i = (last ? propertyPath.length() - 1 : 0);
        while ((last && i >= 0) || i < propertyPath.length()) {
            switch (propertyPath.charAt(i)) {
            case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
            case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
                inKey = !inKey;
                break;
            case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
                if (!inKey) {
                    return i;
                }
            }
            if (last)
                i--;
            else
                i++;
        }
        return -1;
    }

    /**
     * Retrieve a BeanWrapper for the given nested property. Create a new one if
     * not found in the cache.
     * <p>
     * Note: Caching nested BeanWrappers is necessary now, to keep registered
     * custom editors for nested properties.
     * @param nestedProperty property to create the BeanWrapper for
     * @return the BeanWrapper instance, either cached or newly created
     */
    private DefaultBeanMetadata getNestedDynaClass(String nestedProperty) throws BeansException {
        if (this.nestedDynaProperties == null) {
            this.nestedDynaProperties = new HashMap();
        }
        // get value of bean property
        PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);

        // lookup cached sub-Dynaclass, create new one if not found
        DefaultBeanMetadata nested = (DefaultBeanMetadata)this.nestedDynaProperties.get(tokens.actualName);
        if (nested == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new nested DynaClass for property '" + tokens.actualName + "'");
            }
            nested = new DefaultBeanMetadata(tokens.actualName, this.nestedPath + tokens.actualName
                    + PropertyAccessor.NESTED_PROPERTY_SEPARATOR);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using cached nested DynaClass for property '" + tokens.actualName + "'");
            }
        }
        return nested;
    }

    private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
        PropertyTokenHolder tokens = new PropertyTokenHolder();
        String actualName = null;
        List keys = new ArrayList(2);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = propertyName.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = propertyName.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart
                        + PropertyAccessor.PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    if (actualName == null) {
                        actualName = propertyName.substring(0, keyStart);
                    }
                    String key = propertyName.substring(keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length(),
                            keyEnd);
                    if (key.startsWith("'") && key.endsWith("'")) {
                        key = key.substring(1, key.length() - 1);
                    }
                    else if (key.startsWith("\"") && key.endsWith("\"")) {
                        key = key.substring(1, key.length() - 1);
                    }
                    keys.add(key);
                    searchIndex = keyEnd + PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        tokens.actualName = (actualName != null ? actualName : propertyName);
        tokens.canonicalName = tokens.actualName;
        if (!keys.isEmpty()) {
            tokens.canonicalName += PropertyAccessor.PROPERTY_KEY_PREFIX
                    + StringUtils.collectionToDelimitedString(keys, PropertyAccessor.PROPERTY_KEY_SUFFIX
                            + PropertyAccessor.PROPERTY_KEY_PREFIX) + PropertyAccessor.PROPERTY_KEY_SUFFIX;
            tokens.keys = (String[])keys.toArray(new String[keys.size()]);
        }
        return tokens;
    }

    private static class PropertyTokenHolder {

        // with keys e.g address[0]
        private String canonicalName;

        // without keys e.g address
        private String actualName;

        private String[] keys;
    }

    public void add(String name, Class type) {
        assertNotRestricted();
        add(name, type, true, true);
    }

    private void assertNotRestricted() {
        Assert.state(!restricted, "Mutable dynaclass is now restricted (immutable): no further changes allowed.");
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void remove(String name) {
        throw new UnsupportedOperationException();
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public String getName() {
        return name;
    }

    public DynaProperty getDynaProperty(String propertyPath) {
        DefaultBeanMetadata nested = getDynaClassForPropertyPath(propertyPath);
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nested, propertyPath));
        return nested.getDynaPropertyInternal(tokens);
    }

    protected DynaProperty getDynaPropertyInternal(PropertyTokenHolder tokens) {
        return (DynaProperty)dynaProperties.get(tokens.actualName);
    }

    public DynaProperty[] getDynaProperties() {
        return (DynaProperty[])dynaProperties.values().toArray(new DynaProperty[0]);
    }

}