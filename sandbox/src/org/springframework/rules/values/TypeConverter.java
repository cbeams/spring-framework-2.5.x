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

import org.springframework.rules.UnaryFunction;

/**
 * @author Keith Donald
 */
public class TypeConverter implements ValueModel {

    private ValueModel wrappedModel;

    private UnaryFunction convertFrom;

    private UnaryFunction convertTo;

    public TypeConverter(ValueModel wrappedModel, UnaryFunction convertTo,
            UnaryFunction convertFrom) {
        this.wrappedModel = wrappedModel;
        this.convertFrom = convertTo;
        this.convertTo = convertFrom;
    }

    public TypeConverter(ValueModel wrappedModel,
            final PropertyEditor propertyEditor) {
        this.wrappedModel = wrappedModel;
        this.convertFrom = new UnaryFunction() {
            public Object evaluate(Object o) {
                propertyEditor.setValue(o);
                return propertyEditor.getAsText();
            }
        };
        this.convertTo = new UnaryFunction() {
            public Object evaluate(Object o) {
                if (o instanceof String) {
                    propertyEditor.setAsText((String)o);
                    return propertyEditor.getValue();
                } else {
                    return o;
                }
            }
        };
    }

    /**
     * @see org.springframework.rules.values.ValueModel#get()
     */
    public Object get() {
        return convertFrom.evaluate(wrappedModel.get());
    }

    /**
     * @see org.springframework.rules.values.ValueModel#set(java.lang.Object)
     */
    public void set(Object value) {
        wrappedModel.set(convertTo.evaluate(value));
    }

    public Object getWrappedValue() {
        return wrappedModel.get();
    }

    public ValueModel getWrappedValueModel() {
        return wrappedModel;
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#addValueListener(org.springframework.rules.values.ValueListener)
     */
    public void addValueListener(ValueListener l) {
        wrappedModel.addValueListener(l);
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#removeValueListener(org.springframework.rules.values.ValueListener)
     */
    public void removeValueListener(ValueListener l) {
        wrappedModel.removeValueListener(l);
    }

}