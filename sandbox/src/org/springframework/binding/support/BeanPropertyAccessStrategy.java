/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.binding.MutablePropertyAccessStrategy;
import org.springframework.binding.PropertyAccessStrategy;
import org.springframework.binding.PropertyMetadataAccessStrategy;
import org.springframework.binding.value.BoundValueModel;
import org.springframework.binding.value.PropertyChangePublisher;
import org.springframework.binding.value.ValueChangeListener;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.PropertyAdapter;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.enums.CodedEnum;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ToStringCreator;

public class BeanPropertyAccessStrategy implements
        MutablePropertyAccessStrategy {
    private static final Log logger = LogFactory
            .getLog(BeanPropertyAccessStrategy.class);

    private Map listeners;

    private BeanWrapperImpl beanWrapper;

    private BoundValueModel beanHolder;

    private PropertyMetadataAccessStrategy metaAspectAccessor;

    private Map nestedPropertyAccessors;

    private Map propertyValueModels;

    private String nestedPath = "";

    private DomainObjectChangeHandler domainObjectChangeHandler;

    public BeanPropertyAccessStrategy(Object bean) {
        this((bean instanceof BoundValueModel ? (BoundValueModel)bean
                : createValueHolder(bean)));
    }

    private static BoundValueModel createValueHolder(Object bean) {
        Assert
                .notNull(
                        bean,
                        "The bean value must not be null--otherwise I won't know what class the bean is!");
        if (Class.class.isAssignableFrom(bean.getClass())) {
            return new ValueHolder(BeanUtils.instantiateClass((Class)bean));
        }
        else {
            return new ValueHolder(bean);
        }
    }

    public BeanPropertyAccessStrategy(final BoundValueModel beanHolder) {
        Assert.notNull(beanHolder, "No bean holder specified");
        this.beanWrapper = new BeanWrapperImpl();
        this.beanHolder = beanHolder;
        Assert
                .notNull(
                        beanHolder.getValue(),
                        "The bean holder value must not be null--otherwise I won't know what class the bean is!");
        if (logger.isDebugEnabled()) {
            logger.debug("[Subscribing to mutable bean holder; bean value="
                    + beanHolder.getValue() + "]");
        }
        this.domainObjectChangeHandler = new DomainObjectChangeHandler();
        this.domainObjectChangeHandler.updateBeanWrapper();
        this.beanHolder.addValueChangeListener(domainObjectChangeHandler);
    }

    private class DomainObjectChangeHandler implements ValueChangeListener {
        public void valueChanged() {
            if (logger.isDebugEnabled()) {
                logger.debug("["
                        + ObjectUtils.getIdentityHexString(this)
                        + " - Backing domain object of class "
                        + (beanHolder.getValue() != null ? beanHolder
                                .getValue().getClass().getName() : "null")
                        + " has changed; new value = " + beanHolder.getValue());
            }
            updateBeanWrapper(beanHolder.getValue());
        }

        private void updateBeanWrapper() {
            updateBeanWrapper(beanHolder.getValue());
        }

        private void updateBeanWrapper(Object newValue) {
            if (beanHolder.getValue() != null) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("[Setting the enclosed bean wrapper's target object; new value='"
                                    + newValue + "']");
                }
                beanWrapper.setWrappedInstance(newValue);
            }
            else {
                throw new IllegalArgumentException(
                        "Held bean value cannot be null!");
            }
        }
    }

    private BeanPropertyAccessStrategy(BoundValueModel nestedDomainObjectHolder,
            String nestedPropertyName, final BeanPropertyAccessStrategy parent) {
        this(nestedDomainObjectHolder);
        this.nestedPath = nestedPropertyName;
    }

    public Object getPropertyValue(String propertyName) throws BeansException {
        return beanWrapper.getPropertyValue(propertyName);
    }

    public void setPropertyValue(PropertyValue pv) throws BeansException {
        beanWrapper.setPropertyValue(pv);
    }

    public void setPropertyValue(String propertyName, Object value)
            throws BeansException {
        beanWrapper.setPropertyValue(propertyName, value);
    }

    public void setPropertyValues(Map map) throws BeansException {
        beanWrapper.setPropertyValues(map);
    }

    public void setPropertyValues(PropertyValues pvs) throws BeansException {
        beanWrapper.setPropertyValues(pvs);
    }

    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
            throws BeansException {
        beanWrapper.setPropertyValues(pvs, ignoreUnknown);
    }

    public Class getDomainObjectClass() {
        return beanWrapper.getWrappedClass();
    }

    public BoundValueModel getPropertyValueModel(String propertyPath)
            throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving property value model for path '"
                    + propertyPath + "'");
        }
        BeanPropertyAccessStrategy nestedAccessor = (BeanPropertyAccessStrategy)getPropertyAccessStrategyForPath(propertyPath);
        return nestedAccessor.getOrCreateValueModel(getFinalPath(
                nestedAccessor, propertyPath));
    }

    /**
     * Recursively navigate to return a BeanWrapper for the nested property
     * path.
     * 
     * @param propertyPath
     *            property property path, which may be nested
     * @return a access strategy for the target bean
     */
    public MutablePropertyAccessStrategy getPropertyAccessStrategyForPath(
            String propertyPath) throws BeansException {
        int pos = getNestedPropertySeparatorIndex(propertyPath, false);
        // handle nested properties recursively
        if (pos > -1) {
            String nestedPropertyName = propertyPath.substring(0, pos);
            PropertyAccessStrategy nestedAccessStrategy = getNestedPropertyAccessStrategy(nestedPropertyName);
            String remainingNestedPropertyPath = propertyPath
                    .substring(pos + 1);
            return ((BeanPropertyAccessStrategy)nestedAccessStrategy)
                    .getPropertyAccessStrategyForPath(remainingNestedPropertyPath);
        }
        else {
            return this;
        }
    }

    protected static int getNestedPropertySeparatorIndex(String propertyPath,
            boolean last) {
        boolean inKey = false;
        int i = (last ? propertyPath.length() - 1 : 0);
        while ((last && i >= 0) || (!last && i < propertyPath.length())) {
            switch (propertyPath.charAt(i)) {
            case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
            case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
                inKey = !inKey;
                break;
            case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
                if (!inKey) { return i; }
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
     * Get the last component of the path. Also works if not nested.
     * 
     * @param pas
     *            BeanAccessStrategy to work on
     * @param nestedPath
     *            property path we know is nested
     * @return last component of the path (the property on the target bean)
     */
    private String getFinalPath(PropertyAccessStrategy pas, String nestedPath) {
        if (pas == this) { return nestedPath; }
        return nestedPath.substring(getNestedPropertySeparatorIndex(nestedPath,
                true) + 1);
    }

    /**
     * Retrieve a PropertyAccessStrategy for the given nested property. Create a
     * new one if not found in the cache.
     * 
     * @param nestedPropertyName
     *            property to create the BeanWrapper for
     * @return the access strategy instance, either cached or newly created
     */
    protected PropertyAccessStrategy getNestedPropertyAccessStrategy(
            String nestedPropertyName) throws BeansException {
        if (this.nestedPropertyAccessors == null) {
            this.nestedPropertyAccessors = new HashMap();
        }
        String[] tokens = getPropertyNameTokens(nestedPropertyName);
        String canonicalName = tokens[0];
        PropertyAccessStrategy nestedAccessor = (PropertyAccessStrategy)this.nestedPropertyAccessors
                .get(canonicalName);
        // lookup cached sub-BeanWrapper, create new one if not found
        if (nestedAccessor == null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Creating new nested BeanPropertyAccessor for property '"
                                + canonicalName + "'");
            }
            BoundValueModel propertyValueHolder = getOrCreateValueModel(nestedPropertyName);
            nestedAccessor = new BeanPropertyAccessStrategy(
                    propertyValueHolder, this.nestedPath + canonicalName
                            + PropertyAccessor.NESTED_PROPERTY_SEPARATOR, this);
            this.nestedPropertyAccessors.put(canonicalName, nestedAccessor);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Using cached nested BeanPropertyAccessor for property '"
                                + canonicalName + "'");
            }
        }
        return nestedAccessor;
    }

    protected BoundValueModel getOrCreateValueModel(String propertyName) {
        if (propertyValueModels == null) {
            this.propertyValueModels = new HashMap();
        }
        BoundValueModel propertyValueHolder = (BoundValueModel)propertyValueModels
                .get(propertyName);
        if (propertyValueHolder == null) {
            propertyValueHolder = new PropertyAdapter(this, propertyName);
            propertyValueModels.put(propertyName, propertyValueHolder);
        }
        return propertyValueHolder;
    }

    protected String[] getPropertyNameTokens(String propertyPath) {
        String actualName = propertyPath;
        String key = null;
        int keyStart = propertyPath
                .indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX);
        if (keyStart != -1
                && propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX)) {
            actualName = propertyPath.substring(0, keyStart);
            key = propertyPath.substring(keyStart + 1,
                    propertyPath.length() - 1);
            if (key.startsWith("'") && key.endsWith("'")) {
                key = key.substring(1, key.length() - 1);
            }
            else if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
        }
        String canonicalName = actualName;
        if (key != null) {
            canonicalName += PropertyAccessor.PROPERTY_KEY_PREFIX + key
                    + PropertyAccessor.PROPERTY_KEY_SUFFIX;
        }
        return new String[] { canonicalName, actualName, key };
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (beanHolder.getValue() instanceof PropertyChangePublisher) {
            PropertyChangePublisher p = (PropertyChangePublisher)beanHolder
                    .getValue();
            p.addPropertyChangeListener(l);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (beanHolder.getValue() instanceof PropertyChangePublisher) {
            PropertyChangePublisher p = (PropertyChangePublisher)beanHolder
                    .getValue();
            p.addPropertyChangeListener(l);
        }
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener l) {
        if (beanHolder.getValue() instanceof PropertyChangePublisher) {
            PropertyChangePublisher p = (PropertyChangePublisher)beanHolder
                    .getValue();
            p.addPropertyChangeListener(propertyName, l);
        }
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener l) {
        if (beanHolder.getValue() instanceof PropertyChangePublisher) {
            PropertyChangePublisher p = (PropertyChangePublisher)beanHolder
                    .getValue();
            p.removePropertyChangeListener(propertyName, l);
        }
    }

    protected Map getListeners() {
        if (this.listeners == null) {
            this.listeners = new WeakHashMap();
        }
        return listeners;
    }

    private Set createListenerKey(ValueChangeListener l, String aspect) {
        LinkedHashSet key = new LinkedHashSet(2);
        key.add(l);
        key.add(aspect);
        return key;
    }

    public void registerCustomEditor(Class propertyType,
            PropertyEditor propertyEditor) {
        this.beanWrapper.registerCustomEditor(propertyType, propertyEditor);
    }

    public void registerCustomEditor(String propertyName,
            PropertyEditor propertyEditor) {
        this.beanWrapper.registerCustomEditor(getMetadataAccessStrategy()
                .getPropertyType(propertyName), propertyName, propertyEditor);
    }

    public PropertyEditor findCustomEditor(String propertyName) {
        return this.beanWrapper.findCustomEditor(getMetadataAccessStrategy()
                .getPropertyType(propertyName), propertyName);
    }

    public PropertyMetadataAccessStrategy getMetadataAccessStrategy() {
        if (metaAspectAccessor == null) {
            this.metaAspectAccessor = new BeanPropertyMetaAspectAccessor(
                    beanWrapper);
        }
        return metaAspectAccessor;
    }

    private static class BeanPropertyMetaAspectAccessor implements
            PropertyMetadataAccessStrategy {

        private BeanWrapper beanWrapper;

        public BeanPropertyMetaAspectAccessor(BeanWrapper beanWrapper) {
            this.beanWrapper = beanWrapper;
        }

        public Class getPropertyType(String propertyName) {
            return beanWrapper.getPropertyDescriptor(propertyName)
                    .getPropertyType();
        }

        public boolean isNumber(String propertyName) {
            Class propertyType = beanWrapper
                    .getPropertyDescriptor(propertyName).getPropertyType();
            return Number.class.isAssignableFrom(propertyType)
                    || propertyType.isPrimitive();
        }

        public boolean isDate(String propertyName) {
            return Date.class.isAssignableFrom(beanWrapper
                    .getPropertyDescriptor(propertyName).getPropertyType());
        }

        public boolean isEnumeration(String propertyName) {
            return CodedEnum.class.isAssignableFrom(beanWrapper
                    .getPropertyDescriptor(propertyName).getPropertyType());
        }

        public boolean isReadable(String propertyName) {
            return beanWrapper.isReadableProperty(propertyName);
        }

        public boolean isWriteable(String propertyName) {
            return beanWrapper.isWritableProperty(propertyName);
        }

    }

    public Object getDomainObject() {
        return beanHolder.getValue();
    }

    public BoundValueModel getDomainObjectHolder() {
        return beanHolder;
    }

    public MutablePropertyAccessStrategy newPropertyAccessStrategy(
            ValueModel domainObjectHolder) {
        return new BeanPropertyAccessStrategy(domainObjectHolder);
    }

    public String toString() {
        return new ToStringCreator(this).append("beanHolder", beanHolder)
                .toString();
    }

}