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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.enums.CodedEnum;
import org.springframework.util.Assert;

/**
 * Aspect access strategy that accesses bean property values. An "aspect" in
 * this case is a single bean property.
 * 
 * @author Keith Donald
 */
public class BeanPropertyAccessStrategy implements
        MutablePropertyAccessStrategy {
    private static final Log logger = LogFactory
            .getLog(BeanPropertyAccessStrategy.class);

    private Map listeners;

    private ValueModel beanHolder;

    private BeanWrapper beanWrapper;

    private PropertyMetadataAccessStrategy metaAspectAccessor;

    private boolean updatingValue;

    public BeanPropertyAccessStrategy(Object bean) {
        this((bean instanceof ValueModel ? (ValueModel)bean : new ValueHolder(
                bean)));
    }

    public BeanPropertyAccessStrategy(final ValueModel beanHolder) {
        Assert.notNull(beanHolder);
        if (beanHolder.get() != null) {
            this.beanWrapper = new BeanWrapperImpl(beanHolder.get());
        }
        else {
            this.beanWrapper = new BeanWrapperImpl();
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

    public void addValueListener(ValueListener l, String aspect) {
        if (beanHolder.get() instanceof PropertyChangePublisher) {
            ValuePropertyChangeListenerMediator listener = new ValuePropertyChangeListenerMediator(
                    this, l, aspect);
            getListeners().put(createListenerKey(l, aspect), listener);
        }
    }

    public void removeValueListener(ValueListener l, String aspect) {
        if (beanHolder.get() instanceof PropertyChangePublisher) {
            Set listenerKey = createListenerKey(l, aspect);
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
        beanWrapper.registerCustomEditor(propertyType, propertyEditor);
    }

    public void registerCustomEditor(String propertyName,
            PropertyEditor propertyEditor) {
        beanWrapper.registerCustomEditor(getMetadataAccessStrategy()
                .getPropertyType(propertyName), propertyName, propertyEditor);
    }

    public PropertyEditor findCustomEditor(String propertyName) {
        return beanWrapper.findCustomEditor(getMetadataAccessStrategy()
                .getPropertyType(propertyName), propertyName);
    }

    public Object getValue(String aspect) {
        try {
            if (beanHolder.get() == null) { return null; }
            if (logger.isDebugEnabled()) {
                logger.debug("Accessing aspect '" + aspect + "'...");
            }
            return beanWrapper.getPropertyValue(aspect);
        }
        catch (NullValueInNestedPathException e) {
            logger
                    .info("Bean property accessor encountered a null object along property path; returning null");
            return null;
        }
    }

    public void setValue(String aspect, Object value) {
        if (beanHolder.get() == null) {
            logger
                    .warn("Attempt to set property on null reference; doing nothing...");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Setting aspect '" + aspect + "' = " + value);
        }
        this.updatingValue = true;
        try {
            this.beanWrapper.setPropertyValue(aspect, value);
        }
        finally {
            this.updatingValue = false;
        }
    }

    public boolean isValueUpdating() {
        return updatingValue;
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

        public Class getPropertyType(String aspect) {
            return beanWrapper.getPropertyDescriptor(aspect).getPropertyType();
        }

        public boolean isNumber(String aspect) {
            Class propertyType = beanWrapper.getPropertyDescriptor(aspect)
                    .getPropertyType();
            return Number.class.isAssignableFrom(propertyType)
                    || propertyType.isPrimitive();
        }

        public boolean isDate(String aspect) {
            return Date.class.isAssignableFrom(beanWrapper
                    .getPropertyDescriptor(aspect).getPropertyType());
        }

        public boolean isEnumeration(String aspect) {
            return CodedEnum.class.isAssignableFrom(beanWrapper
                    .getPropertyDescriptor(aspect).getPropertyType());
        }

        public boolean isReadable(String aspect) {
            return beanWrapper.isReadableProperty(aspect);
        }

        public boolean isWriteable(String aspect) {
            return beanWrapper.isWritableProperty(aspect);
        }

    }

    public Object getDomainObject() {
        return beanHolder.get();
    }

    public ValueModel getDomainObjectHolder() {
        return beanHolder;
    }

    public MutablePropertyAccessStrategy newNestedAccessor(
            ValueModel childValueHolder) {
        return new BeanPropertyAccessStrategy(childValueHolder);
    }

}