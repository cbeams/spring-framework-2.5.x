/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/TypeConverter.java,v 1.2 2004-06-16 21:32:56 kdonald Exp $
 * $Revision: 1.2 $
 * $Date: 2004-06-16 21:32:56 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import java.beans.PropertyEditor;
import java.io.File;

import org.springframework.rules.UnaryFunction;

/**
 * @author Keith Donald
 */
public class TypeConverter implements ValueModel {

    private ValueModel wrappedModel;

    private UnaryFunction convertTo;

    private UnaryFunction convertFrom;

    public TypeConverter(ValueModel wrappedModel, UnaryFunction convertTo,
            UnaryFunction convertFrom) {
        this.wrappedModel = wrappedModel;
        this.convertTo = convertTo;
        this.convertFrom = convertFrom;
    }

    public TypeConverter(ValueModel wrappedModel,
            final PropertyEditor propertyEditor) {
        this.wrappedModel = wrappedModel;
        this.convertTo = new UnaryFunction() {
            public Object evaluate(Object o) {
                propertyEditor.setValue((File)o);
                return propertyEditor.getAsText();
            }
        };
        this.convertFrom = new UnaryFunction() {
            public Object evaluate(Object o) {
                propertyEditor.setAsText((String)o);
                return propertyEditor.getValue();
            }
        };
    }

    /**
     * @see org.springframework.rules.values.ValueModel#get()
     */
    public Object get() {
        return convertTo.evaluate(wrappedModel.get());
    }

    /**
     * @see org.springframework.rules.values.ValueModel#set(java.lang.Object)
     */
    public void set(Object value) {
        wrappedModel.set(convertFrom.evaluate(value));
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