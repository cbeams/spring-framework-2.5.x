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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.value.BoundValueModel;
import org.springframework.binding.value.ValueChangeListener;
import org.springframework.util.DefaultObjectStyler;

/**
 * Base helper implementation of a value model.
 * 
 * @author Keith Donald
 */
public abstract class AbstractValueModel extends
        AbstractPropertyChangePublisher implements BoundValueModel {
    protected static final Log logger = LogFactory
            .getLog(AbstractValueModel.class);

    private Set listeners;

    public void addValueChangeListener(ValueChangeListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Adding value listener " + l + "]");
        }
        getOrCreateListeners().add(l);
    }

    public void removeValueChangeListener(ValueChangeListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Removing value listener " + l + "]");
        }
        getOrCreateListeners().remove(l);
    }

    private Collection getOrCreateListeners() {
        if (listeners == null) {
            listeners = new HashSet();
        }
        return listeners;
    }

    protected void fireValueChanged() {
        if (listeners != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Firing value changed event; newValue="
                        + DefaultObjectStyler.call(getValue()) + "]");
            }
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((ValueChangeListener)it.next()).valueChanged();
            }
        }
    }

    protected void fireValueChanged(boolean oldValue, boolean newValue) {
        fireValueChanged(new Boolean(oldValue), new Boolean(newValue));
    }

    protected void fireValueChanged(int oldValue, int newValue) {
        fireValueChanged(new Integer(oldValue), new Integer(newValue));
    }

    protected void fireValueChanged(long oldValue, long newValue) {
        fireValueChanged(new Long(oldValue), new Long(newValue));
    }

    protected void fireValueChanged(double oldValue, double newValue) {
        fireValueChanged(new Double(oldValue), new Double(newValue));
    }

    protected void fireValueChanged(float oldValue, float newValue) {
        fireValueChanged(new Float(oldValue), new Float(newValue));
    }

    protected void fireValueChanged(Object oldValue, Object newValue) {
        fireValueChanged();
        firePropertyChange(VALUE_PROPERTY, oldValue, newValue);
    }

}