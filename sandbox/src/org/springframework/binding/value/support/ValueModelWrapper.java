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

import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.value.BoundValueModel;
import org.springframework.binding.value.ValueChangeListener;
import org.springframework.binding.value.ValueModel;

/**
 * @author Keith Donald
 */
public abstract class ValueModelWrapper implements BoundValueModel {
    protected final Log logger = LogFactory.getLog(getClass());

    private ValueModel wrappedModel;

    public ValueModelWrapper(ValueModel valueModel) {
        this.wrappedModel = valueModel;
    }

    public Object getValue() {
        return wrappedModel.getValue();
    }

    public void setValue(Object value) {
        this.wrappedModel.setValue(value);
    }

    protected ValueModel getWrappedModel() {
        return wrappedModel;
    }

    public ValueModel getInnerMostValueModel() {
        if (wrappedModel instanceof ValueModelWrapper) {
            return ((ValueModelWrapper)wrappedModel).getInnerMostValueModel();
        }
        else {
            return wrappedModel;
        }
    }

    public Object getInnerMostValue() {
        if (wrappedModel instanceof ValueModelWrapper) {
            return ((ValueModelWrapper)wrappedModel).getInnerMostValue();
        }
        else {
            return wrappedModel.getValue();
        }
    }

    public void addValueChangeListener(ValueChangeListener l) {
        wrappedModel.addValueChangeListener(l);
    }

    public void removeValueChangeListener(ValueChangeListener l) {
        wrappedModel.removeValueChangeListener(l);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getBoundValueModel().addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        getBoundValueModel().addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getBoundValueModel().removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        getBoundValueModel().removePropertyChangeListener(propertyName,
                listener);
    }

    private BoundValueModel getBoundValueModel() {
        try {
            return (BoundValueModel)wrappedModel;
        }
        catch (ClassCastException e) {
            RuntimeException ex = new UnsupportedOperationException(
                    "Value model wrapper does not wrap a bound value model");
            ex.initCause(e);
            throw ex;
        }
    }

}