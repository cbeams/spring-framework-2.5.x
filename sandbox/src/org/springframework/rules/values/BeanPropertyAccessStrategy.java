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
package org.springframework.rules.values;

import java.beans.PropertyEditor;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.enums.CodedEnum;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;

public class BeanPropertyAccessStrategy implements
        MutablePropertyAccessStrategy {
    private static final Log logger = LogFactory
            .getLog(BeanPropertyAccessStrategy.class);

    private Map listeners;

    private BeanWrapperImpl beanWrapper;

    private ValueModel beanHolder;

    private PropertyMetadataAccessStrategy metaAspectAccessor;

    private Map nestedPropertyAccessors;

    private String nestedPath = "";

    public BeanPropertyAccessStrategy(Object bean) {
        this((bean instanceof ValueModel ? (ValueModel)bean : new ValueHolder(
                bean)));
    }

    public BeanPropertyAccessStrategy(final ValueModel beanHolder) {
        Assert.notNull(beanHolder);
        this.beanWrapper = new BeanWrapperImpl();
        if (beanHolder.get() != null) {
            beanWrapper.setWrappedInstance(beanHolder.get());
        }
        this.beanHolder = beanHolder;
        if (logger.isDebugEnabled()) {
            logger.debug("[Bean accessor attaching to mutable bean holder.]");
        }
        this.beanHolder.addValueListener(new ValueListener() {
            public void valueChanged() {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("[Updating the enclosed bean wrapper's target object]");
                }
                if (beanHolder.get() != null) {
                    beanWrapper.setWrappedInstance(beanHolder.get());
                }
            }
        });
    }

    private BeanPropertyAccessStrategy(Object nestedBeanValue,
            String nestedPropertyName, final BeanPropertyAccessStrategy parent) {
        this(new ValueHolder(nestedBeanValue));
        this.nestedPath = nestedPropertyName;
        parent.getDomainObjectHolder().addValueListener(new ValueListener() {
            public void valueChanged() {
                beanHolder.set(parent.getPropertyValue(getFinalPath(parent,
                        nestedPath)));
            }
        });
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

    public Object getPropertyValue(String propertyName) throws BeansException {
        if (beanHolder.get() == null) { return null; }
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

    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
            throws BeansException {
        beanWrapper.setPropertyValues(pvs, ignoreUnknown);
    }

    public void setPropertyValues(PropertyValues pvs) throws BeansException {
        beanWrapper.setPropertyValues(pvs);
    }

    public Class getWrappedClass() {
        return beanWrapper.getWrappedClass();
    }

    protected static int getNestedPropertySeparatorIndex(String propertyPath,
            boolean last) {
        boolean inKey = false;
        int i = (last ? propertyPath.length() - 1 : 0);
        while ((last && i >= 0) || i < propertyPath.length()) {
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
        Object propertyValue = getPropertyValue(canonicalName);
        if (propertyValue == null) { throw new NullValueInNestedPathException(
                getWrappedClass(), this.nestedPath + canonicalName); }

        // lookup cached sub-BeanWrapper, create new one if not found
        PropertyAccessStrategy nestedAccessor = (PropertyAccessStrategy)this.nestedPropertyAccessors
                .get(canonicalName);
        if (nestedAccessor == null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Creating new nested BeanPropertyAccessor for property '"
                                + canonicalName + "'");
            }
            nestedAccessor = new BeanPropertyAccessStrategy(propertyValue,
                    this.nestedPath + canonicalName
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

    public void addValueListener(ValueListener l, String propertyName) {
        if (beanHolder.get() instanceof PropertyChangePublisher) {
            ValuePropertyChangeListenerMediator listener = new ValuePropertyChangeListenerMediator(
                    this, l, propertyName);
            getListeners().put(createListenerKey(l, propertyName), listener);
        }
    }

    public void removeValueListener(ValueListener l, String propertyName) {
        if (beanHolder.get() instanceof PropertyChangePublisher) {
            Set listenerKey = createListenerKey(l, propertyName);
            ValuePropertyChangeListenerMediator listener = (ValuePropertyChangeListenerMediator)listeners
                    .get(listenerKey);
            if (listener != null) {
                listener.unsubscribe();
                getListeners().remove(listenerKey);
            }
        }
    }

    protected Map getListeners() {
        if (this.listeners == null) {
            this.listeners = new WeakHashMap();
        }
        return listeners;
    }

    private Set createListenerKey(ValueListener l, String aspect) {
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
        return beanHolder.get();
    }

    public ValueModel getDomainObjectHolder() {
        return beanHolder;
    }

    public MutablePropertyAccessStrategy newPropertyAccessStrategy(
            ValueModel domainObjectHolder) {
        return new BeanPropertyAccessStrategy(domainObjectHolder);
    }

    public String toString() {
        return new ToStringBuilder(this).append("beanHolder", beanHolder)
                .toString();
    }

}