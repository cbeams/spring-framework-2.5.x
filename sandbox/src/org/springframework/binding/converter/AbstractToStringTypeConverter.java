/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.converter;

import org.springframework.binding.TypeConverter;

/**
 * @author Keith Donald
 */
public abstract class AbstractToStringTypeConverter extends AbstractTypeConverter implements TypeConverter {

    protected static final String EMPTY_STRING = "";

    /**
     * @param convertToClass
     * @param convertFromClass
     */
    public AbstractToStringTypeConverter(Class convertFromClass) {
        super(String.class, convertFromClass);
    }

    /**
     * @param convertToClass
     * @param convertFromClass
     * @param allowEmpty
     */
    public AbstractToStringTypeConverter(Class convertFromClass, boolean allowEmpty) {
        super(String.class, convertFromClass, allowEmpty);
    }

    protected Object getConvertToClassEmptyValue() {
        return EMPTY_STRING;
    }

}