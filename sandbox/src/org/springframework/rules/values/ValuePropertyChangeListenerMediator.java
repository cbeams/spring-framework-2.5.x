/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValuePropertyChangeListenerMediator.java,v 1.1 2004-06-12 07:27:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
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

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Property change event received from domain object; notifiying value listener");
        }
        this.listener.valueChanged();
    }

}