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
package org.springframework.binding.value.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.springframework.binding.MutablePropertyAccessStrategy;
import org.springframework.binding.value.PropertyChangePublisher;
import org.springframework.binding.value.ValueChangeListener;
import org.springframework.util.ToStringCreator;

/**
 * Adapts access to a domain model property to the value model interface. The
 * property access strategy is pluggable.
 * 
 * @author Keith Donald
 */
public class PropertyAdapter extends AbstractValueModel implements
        PropertyChangeListener {
    private String propertyName;

    private MutablePropertyAccessStrategy propertyAccessStrategy;

    private PropertyChangeListener domainObjectChangeHandler;

    public PropertyAdapter(
            MutablePropertyAccessStrategy propertyAccessStrategy,
            String propertyName) {
        if (propertyAccessStrategy.getDomainObjectHolder() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Property adapter for property '" + propertyName
                        + "' subscribing to domain object holder; value="
                        + propertyAccessStrategy.getDomainObject() + "]");
            }
            this.domainObjectChangeHandler = new DomainObjectChangeHandler();
            propertyAccessStrategy.getDomainObjectHolder()
                    .addPropertyChangeListener(domainObjectChangeHandler);
        }
        this.propertyAccessStrategy = propertyAccessStrategy;
        this.propertyName = propertyName;
        this.propertyAccessStrategy.addPropertyChangeListener(propertyName, this);
    }

    private final class DomainObjectChangeHandler implements
            PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Object oldBean = evt.getOldValue();
            if (oldBean instanceof PropertyChangePublisher) {
                propertyAccessStrategy.removePropertyChangeListener(propertyName, this);
            }
            Object newBean = evt.getNewValue();
            if (newBean instanceof PropertyChangePublisher) {
                propertyAccessStrategy.addPropertyChangeListener(propertyName, this);
            }
            if (logger.isDebugEnabled()) {
                logger
                        .debug("[Notifying any dependents for '"
                                + PropertyAdapter.this.propertyName
                                + "' that the value may have changed; target domain object changed, new object="
                                + PropertyAdapter.this.propertyAccessStrategy
                                        .getDomainObject()
                                + ", new property value="
                                + PropertyAdapter.this.propertyAccessStrategy
                                        .getPropertyValue(propertyName) + "]");
            }
            fireValueChanged();
        }
    }

    public Object getValue() {
        return propertyAccessStrategy.getPropertyValue(propertyName);
    }

    public void setValue(Object value) {
        Object oldValue = getValue();
        if (hasChanged(oldValue, value)) {
            updateValueSilently(value);
            fireValueChanged(oldValue, value);
        }
    }

    private void updateValueSilently(Object value) {
        propertyAccessStrategy.removePropertyChangeListener(propertyName, this);
        propertyAccessStrategy.setPropertyValue(propertyName, value);
        propertyAccessStrategy.addPropertyChangeListener(propertyName, this);
    }
    
    public void addValueChangeListener(ValueChangeListener l) {
        super.addValueChangeListener(l);
    }

    public void removeValueChangeListener(ValueChangeListener l) {
        super.removeValueChangeListener(l);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        fireValueChanged(evt.getOldValue(), evt.getNewValue());
    }

    public String toString() {
        return new ToStringCreator(this).append("propertyName", propertyName)
                .append("propertyAccessStrategy", propertyAccessStrategy)
                .toString();
    }
}