/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.converter;

import java.beans.PropertyEditor;

import org.springframework.binding.TypeConversionException;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class TypeConverterPropertyEditorAdapter extends AbstractToStringTypeConverter {

    private PropertyEditor propertyEditor;

    public TypeConverterPropertyEditorAdapter(PropertyEditor propertyEditor) {
        super(Object.class);
        Assert.notNull(propertyEditor, "Property editor is required");
        this.propertyEditor = propertyEditor;
    }

    public PropertyEditor getPropertyEditor() {
        return propertyEditor;
    }

    protected Object doConvert(Object o) throws TypeConversionException {
        propertyEditor.setValue(o);
        return propertyEditor.getAsText();
    }

    protected Object doConvertBack(Object o) throws TypeConversionException {
        propertyEditor.setAsText((String)o);
        return propertyEditor.getValue();
    }

}