/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.converter;

import org.springframework.binding.TypeConversionException;
import org.springframework.binding.TypeConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.closure.Closure;

/**
 * @author Keith Donald
 */
public abstract class AbstractTypeConverter implements TypeConverter, Closure {
    private Class convertToClass;

    private Class convertFromClass;

    private boolean allowEmpty;

    protected AbstractTypeConverter(Class convertToClass, Class convertFromClass) {
        this(convertToClass, convertFromClass, false);
    }

    protected AbstractTypeConverter(Class convertToClass, Class convertFromClass, boolean allowEmpty) {
        Assert.notNull(convertToClass, "The class to convert to is required");
        Assert.notNull(convertFromClass, "The class to convert from is requred");
        this.convertToClass = convertToClass;
        this.convertFromClass = convertFromClass;
        this.allowEmpty = allowEmpty;
    }

    /**
     * @return Returns the convertFromClass.
     */
    public Class getConvertFromType() {
        return convertFromClass;
    }

    /**
     * @return Returns the convertToClass.
     */
    public Class getConvertToType() {
        return convertToClass;
    }

    public Object convert(Object o) throws TypeConversionException {
        if (allowEmpty && isEmpty(o)) {
            return getConvertToClassEmptyValue();
        }
        Assert.notNull(o, "Object to convert from '" + getConvertFromType().getName() + "' to '" + getConvertToType()
                + "' cannot be null");
        Assert.isInstanceOf(getConvertFromType(), o);
        return doConvert(o);
    }

    protected abstract Object doConvert(Object o) throws TypeConversionException;

    public Object convertBack(Object o) throws TypeConversionException {
        if (allowEmpty && isEmpty(o)) {
            return getConvertFromClassEmptyValue();
        }
        Assert.notNull(o, "Object to convert back to '" + getConvertFromType().getName() + "' from '"
                + getConvertToType() + "' cannot be null");
        Assert.isInstanceOf(getConvertToType(), o);
        return doConvertBack(o);
    }

    protected abstract Object doConvertBack(Object o) throws TypeConversionException;

    protected boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        else if (o instanceof String) {
            return StringUtils.hasText((String)o);
        }
        else {
            return false;
        }
    }

    protected Object getConvertToClassEmptyValue() {
        return null;
    }

    protected Object getConvertFromClassEmptyValue() {
        return null;
    }

    public Object call(Object argument) {
        return convert(argument);
    }

    public Closure getConvertBackClosure() {
        return convertBackClosure;
    }

    private Closure convertBackClosure = new Closure() {
        public Object call(Object argument) {
            return convertBack(argument);
        }
    };

}