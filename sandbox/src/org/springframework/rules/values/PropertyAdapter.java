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

import org.springframework.util.ToStringBuilder;

/**
 * Adapts access to a domain model property to the value model interface. The
 * property access strategy is pluggable.
 * 
 * @author Keith Donald
 */
public class PropertyAdapter extends AbstractValueModel {
    private String propertyName;

    private MutablePropertyAccessStrategy propertyAccessStrategy;

    public PropertyAdapter(
            MutablePropertyAccessStrategy propertyAccessStrategy,
            String propertyName) {
        if (propertyAccessStrategy.getDomainObjectHolder() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Property adapter for property '" + propertyName
                        + "' attaching to mutable domain object holder.]");
            }
            propertyAccessStrategy.getDomainObjectHolder().addValueListener(
                    new ValueListener() {
                        public void valueChanged() {
                            if (logger.isDebugEnabled()) {
                                logger
                                        .debug("[Notifying any dependents for '"
                                                + PropertyAdapter.this.propertyName
                                                + "' the '"
                                                + PropertyAdapter.this.propertyName
                                                + "' aspect value may have changed; target domain object changed]");
                            }
                            PropertyAdapter.this.fireValueChanged();
                        }
                    });
        }
        this.propertyAccessStrategy = propertyAccessStrategy;
        this.propertyName = propertyName;
    }

    public void addValueListener(ValueListener l) {
        super.addValueListener(l);
        propertyAccessStrategy.addValueListener(l, propertyName);
    }

    public void removeValueListener(ValueListener l) {
        super.removeValueListener(l);
        propertyAccessStrategy.removeValueListener(l, propertyName);
    }

    public Object get() {
        return propertyAccessStrategy.getPropertyValue(propertyName);
    }

    public void set(Object value) {
        propertyAccessStrategy.setPropertyValue(propertyName, value);
    }

    public String toString() {
        return new ToStringBuilder(this).append("propertyName", propertyName)
                .append("propertyAccessStrategy", propertyAccessStrategy)
                .toString();
    }
}