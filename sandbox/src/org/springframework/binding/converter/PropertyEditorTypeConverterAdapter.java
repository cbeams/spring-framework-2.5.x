/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.converter;

import java.beans.PropertyEditorSupport;

import org.springframework.binding.TypeConversionException;
import org.springframework.binding.TypeConverter;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class PropertyEditorTypeConverterAdapter extends PropertyEditorSupport {

    private TypeConverter typeConverter;

    public PropertyEditorTypeConverterAdapter(TypeConverter typeConverter) {
        Assert.notNull(typeConverter, "Type converter is required");
        Assert.isTrue(typeConverter.getConvertToClass().equals(String.class),
                "Adapted type converter must convert to string");
        this.typeConverter = typeConverter;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(typeConverter.convertBack(text));
        }
        catch (TypeConversionException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Could not convert back from string: "
                    + e.getMessage());
            iae.initCause(e);
            throw iae;
        }
    }

    public String getAsText() {
        return (String)typeConverter.convert(getValue());
    }
}