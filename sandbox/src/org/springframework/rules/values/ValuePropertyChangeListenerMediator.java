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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class ValuePropertyChangeListenerMediator implements
        PropertyChangeListener, ValueListener {
    private static final Log logger = LogFactory
            .getLog(ValuePropertyChangeListenerMediator.class);

    private Object domainObject;

    private ValueModel domainObjectHolder;

    private String propertyName;

    private ValueListener listener;

    private MutablePropertyAccessStrategy accessStrategy;

    public ValuePropertyChangeListenerMediator(
            MutablePropertyAccessStrategy accessStrategy,
            ValueListener listener, String propertyName) {
        Assert.notNull(listener);
        Assert.notNull(propertyName);
        this.accessStrategy = accessStrategy;
        this.listener = listener;
        this.propertyName = propertyName;
        this.domainObjectHolder = accessStrategy.getDomainObjectHolder();
        this.domainObjectHolder.addValueListener(this);
        this.domainObject = domainObjectHolder.get();
        subscribe();
    }

    public void valueChanged() {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("[Observed domain object reference has changed; detaching from old and attaching to new...]");
        }
        unsubscribe();
        this.domainObject = domainObjectHolder.get();
        subscribe();
        listener.valueChanged();
    }

    public void subscribe() {
        if (domainObject == null
                || !(domainObject instanceof PropertyChangePublisher)) { return; }
        subscribe((PropertyChangePublisher)domainObject);
    }

    private void subscribe(PropertyChangePublisher publisher) {
        if (publisher != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Subscribing to domain object '" + domainObject
                        + "' for property changes on property '" + propertyName
                        + "'.]");
            }
            publisher.addPropertyChangeListener(propertyName, this);
        }
    }

    public void unsubscribe() {
        if (domainObject == null
                || !(domainObject instanceof PropertyChangePublisher)) { return; }
        unsubscribe((PropertyChangePublisher)domainObject);
    }

    private void unsubscribe(PropertyChangePublisher publisher) {
        if (publisher != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Unsubscribing from domain object '"
                        + domainObject + "' for property changes on property '"
                        + propertyName + "'.]");
            }
            publisher.removePropertyChangeListener(propertyName, this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Property change event received from domain object for property '"
                            + evt.getPropertyName()
                            + "', new value is="
                            + evt.getNewValue()
                            + "; notifiying value listener.");
        }
        this.listener.valueChanged();
    }

}