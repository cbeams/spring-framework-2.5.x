/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.binding.BeanMetadata;
import org.springframework.binding.PropertyAccessor;
import org.springframework.binding.PropertyMetadata;
import org.springframework.binding.value.IndexAdapter;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.GrowableIndexAdapter;
import org.springframework.binding.value.support.LazyCollectionInitializer;
import org.springframework.binding.value.support.MapKeyAdapter;
import org.springframework.binding.value.support.PropertyValueGuard;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class AbstractBeanAccessor implements PropertyAccessor {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Path separator for nested properties. Follows normal Java conventions:
     * getFoo().getBar() would be "foo.bar".
     */
    protected static final String NESTED_PROPERTY_SEPARATOR = ".";

    protected static final char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

    /**
     * Marker that indicates the start of a property key for an indexed or
     * mapped property like "person.addresses[0]".
     */
    protected static final String PROPERTY_KEY_PREFIX = "[";

    protected static final char PROPERTY_KEY_PREFIX_CHAR = '[';

    /**
     * Marker that indicates the end of a property key for an indexed or mapped
     * property like "person.addresses[0]".
     */
    protected static final String PROPERTY_KEY_SUFFIX = "]";

    protected static final char PROPERTY_KEY_SUFFIX_CHAR = ']';

    private BeanMetadata metadata;

    private Map propertyValueModels;

    private String nestedPropertyName;

    private String nestedPath = "";

    public AbstractBeanAccessor() {
        this(new DefaultBeanMetadata());
    }

    public AbstractBeanAccessor(BeanMetadata metadata) {
        this.metadata = metadata;
    }

    protected AbstractBeanAccessor(BeanMetadata metadata, String nestedPath) {
        this.metadata = metadata;
        this.nestedPath = nestedPath;
    }

    protected BeanMetadata getBeanMetadata() {
        return metadata;
    }

    protected BeanMetadata getBeanMetadata(String nestedBeanPropertyName) {
        return metadata.getPropertyMetadata(nestedBeanPropertyName).getBeanMetadata();
    }

    public Object getPropertyValue(String propertyPath) throws PropertyAccessException {
        AbstractBeanAccessor nested = getNestedBeanAccessor(propertyPath);
        return nested.getPropertyValueInternal(parsePropertyNameTokens(trimNestedPropertyPathPrefix(this, propertyPath)));
    }

    public Map getPropertyValues() {
        return null;
    }

    public void setPropertyValue(String propertyPath, Object value) throws PropertyAccessException {
        AbstractBeanAccessor nested = getNestedBeanAccessor(propertyPath);
        String nestedPropertyName = trimNestedPropertyPathPrefix(this, propertyPath);
        PropertyNameTokens propertyNameTokens = parsePropertyNameTokens(nestedPropertyName);
        nested.getPropertyValueModel(propertyNameTokens).setValue(value);
    }

    protected ValueModel getPropertyValueModel(String propertyName) {
        if (propertyValueModels == null) {
            this.propertyValueModels = new HashMap();
        }
        ValueModel propertyValueHolder = (ValueModel)propertyValueModels.get(propertyName);
        if (propertyValueHolder == null) {
            propertyValueHolder = createPropertyValueAccessor(propertyName);
            PropertyMetadata propertyMetadata = getBeanMetadata().getPropertyMetadata(propertyName);
            propertyValueHolder = new PropertyValueGuard(propertyMetadata, propertyValueHolder);
            if (isLazy() && propertyMetadata.isCollection()) {
                propertyValueHolder = new LazyCollectionInitializer(propertyMetadata, propertyValueHolder);
            }
            propertyValueModels.put(propertyName, propertyValueHolder);
        }
        return propertyValueHolder;
    }

    protected ValueModel createPropertyValueAccessor(String propertyName) {
        return new ValueHolder();
    }

    protected boolean isLazy() {
        return true;
    }

    protected ValueModel getPropertyValueModel(PropertyNameTokens propertyNameTokens) {
        ValueModel valueModel = getPropertyValueModel(propertyNameTokens.actualName);
        if (propertyNameTokens.keys != null) {
            for (int i = 0; i < propertyNameTokens.keys.length; i++) {
                String key = propertyNameTokens.keys[i];
                Object value = valueModel.getValue();
                if (value.getClass().isArray() || value instanceof Collection) {
                    IndexAdapter indexAdapter = new GrowableIndexAdapter(isLazy(), valueModel);
                    indexAdapter.setIndex(Integer.parseInt(key));
                    valueModel = indexAdapter;
                }
                else if (value instanceof Map) {
                    valueModel = new MapKeyAdapter(valueModel, key);
                }
                else {
                    throw new IllegalArgumentException("Keys provided but no collection instance found");
                }
            }
        }
        return valueModel;
    }

    // single property only address, adddress[0], address['keith']
    protected Object getPropertyValueInternal(PropertyNameTokens propertyNameTokens) {
        return getPropertyValueModel(propertyNameTokens).getValue();
    }

    protected void setPropertyValueInternal(String propertyName, Object value) {
        getPropertyValueModel(propertyName).setValue(value);
    }

    protected AbstractBeanAccessor getNestedBeanAccessor(String propertyPath) {
        int pos = getNestedPropertyPathSeparatorIndex(propertyPath, false);
        // handle nested properties recursively
        if (pos > -1) {
            String nextPropertyName = propertyPath.substring(0, pos);
            String remainingPropertyPath = propertyPath.substring(pos + 1);
            return getBeanAccessor(nextPropertyName);
        }
        else {
            return this;
        }
    }

    /**
     * Trim the nested.path.prefix.leaving.suffix, e.g resulting in just
     * "suffix".
     * @param bean The bean accessor
     * @param nestedPath The nested.path.prefix.with.suffix for this bean
     * @return last path suffix
     */
    private String trimNestedPropertyPathPrefix(AbstractBeanAccessor bean, String nestedPath) {
        if (bean == this) {
            return nestedPath;
        }
        return nestedPath.substring(getNestedPropertyPathSeparatorIndex(nestedPath, true) + 1);
    }

    /**
     * Determine the first respectively last nested property separator in the
     * given property path, ignoring dots in keys (like "map[my.key]").
     * @param propertyPath the property path to check
     * @param last whether to return the last separator rather than the first
     * @return the index of the nested property separator, or -1 if none
     */
    private int getNestedPropertyPathSeparatorIndex(String propertyPath, boolean last) {
        boolean inKey = false;
        int i = (last ? propertyPath.length() - 1 : 0);
        while ((last && i >= 0) || i < propertyPath.length()) {
            switch (propertyPath.charAt(i)) {
            case PROPERTY_KEY_PREFIX_CHAR:
            case PROPERTY_KEY_SUFFIX_CHAR:
                inKey = !inKey;
                break;
            case NESTED_PROPERTY_SEPARATOR_CHAR:
                if (!inKey) {
                    return i;
                }
            }
            if (last) {
                i--;
            }
            else {
                i++;
            }
        }
        return -1;
    }

    /**
     * Retrieve a BeanWrapper for the given property. Create a new one if not
     * found in the cache.
     * <p>
     * Note: Caching nested BeanWrappers is necessary now, to keep registered
     * custom editors for nested properties.
     * @param beanPropertyName property to create the BeanWrapper for
     * @return the BeanWrapper instance, either cached or newly created
     */
    private AbstractBeanAccessor getBeanAccessor(String beanPropertyName) throws BeansException {
        if (this.propertyValueModels == null) {
            this.propertyValueModels = new HashMap();
        }
        PropertyNameTokens tokens = parsePropertyNameTokens(beanPropertyName);

        // lookup cached sub-bean, create new one if not found
        AbstractBeanAccessor nested = (AbstractBeanAccessor)this.propertyValueModels.get(tokens.actualName);
        if (nested == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new nested accessor for bean property '" + tokens.actualName + "'");
            }
            nested = createNestedBeanAccessor(tokens);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using cached nested accessors for bean property '" + tokens.actualName + "'");
            }
        }
        return nested;
    }

    protected AbstractBeanAccessor createNestedBeanAccessor(PropertyNameTokens tokens) {
        return new AbstractBeanAccessor(getBeanMetadata(tokens.canonicalName), this.nestedPath + tokens.actualName
                + NESTED_PROPERTY_SEPARATOR);
    }

    private PropertyNameTokens parsePropertyNameTokens(String propertyName) {
        PropertyNameTokens tokens = new PropertyNameTokens();
        String actualName = null;
        List keys = new ArrayList(2);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    if (actualName == null) {
                        actualName = propertyName.substring(0, keyStart);
                    }
                    String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
                    if (key.startsWith("'") && key.endsWith("'")) {
                        key = key.substring(1, key.length() - 1);
                    }
                    else if (key.startsWith("\"") && key.endsWith("\"")) {
                        key = key.substring(1, key.length() - 1);
                    }
                    keys.add(key);
                    searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        tokens.actualName = (actualName != null ? actualName : propertyName);
        tokens.canonicalName = tokens.actualName;
        if (!keys.isEmpty()) {
            tokens.canonicalName += PROPERTY_KEY_PREFIX
                    + StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX)
                    + PROPERTY_KEY_SUFFIX;
            tokens.keys = (String[])keys.toArray(new String[keys.size()]);
        }
        return tokens;
    }

    private static class PropertyNameTokens {
        // with keys e.g address[0]
        private String canonicalName;

        // without keys e.g address
        private String actualName;

        // just keys e.g 0, fred, 9, barney
        private String[] keys;
    }

}