/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/PropertyChangePublisher.java,v 1.1 2004-06-12 07:27:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import java.beans.PropertyChangeListener;

/**
 * Interface implemented by domain objects that can publish property change
 * events. Clients can use this interface to subscribe to the object for change
 * notifications.
 * 
 * @author Keith Donald
 */
public interface PropertyChangePublisher {
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(PropertyChangeListener listener,
            String propertyName);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener,
            String propertyName);
}