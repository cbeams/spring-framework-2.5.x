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
    private Log logger = LogFactory.getLog(ValueModel.class);

    private Object domainObject;

    private ValueModel domainObjectHolder;

    private String propertyName;

    private ValueListener listener;

    public ValuePropertyChangeListenerMediator(ValueListener listener,
            String propertyName, ValueModel domainObjectHolder) {
        Assert.notNull(listener);
        Assert.notNull(propertyName);
        Assert.notNull(domainObjectHolder);
        this.listener = listener;
        this.propertyName = propertyName;
        this.domainObjectHolder = domainObjectHolder;
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

    private void subscribe() {
        if (domainObject != null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("[Subscribing to domain object for property changes.]");
            }
            getPublisher().addPropertyChangeListener(this, propertyName);
        }
    }

    public void unsubscribe() {
        if (domainObject != null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("[Unsubscribing to domain object for property changes.]");
            }
            getPublisher().removePropertyChangeListener(this, propertyName);
        }
    }

    private PropertyChangePublisher getPublisher() {
        return (PropertyChangePublisher)domainObject;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Property change event received from domain object; notifiying value listener");
        }
        this.listener.valueChanged();
    }

}