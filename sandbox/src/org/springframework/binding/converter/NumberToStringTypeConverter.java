/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.converter;

import java.text.NumberFormat;

import org.springframework.binding.TypeConversionException;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

/**
 * @author Keith Donald
 */
public class NumberToStringTypeConverter extends AbstractToStringTypeConverter {

    private NumberFormat numberFormat;

    public NumberToStringTypeConverter(Class numberClass) {
        super(numberClass);
        assertNumberClass(numberClass);
    }

    public NumberToStringTypeConverter(Class numberClass, NumberFormat numberFormat) {
        super(numberClass);
        assertNumberClass(numberClass);
        this.numberFormat = numberFormat;
    }

    public NumberToStringTypeConverter(Class numberClass, boolean allowEmpty) {
        super(numberClass, allowEmpty);
        assertNumberClass(numberClass);
    }

    public NumberToStringTypeConverter(Class numberClass, NumberFormat numberFormat, boolean allowEmpty) {
        super(numberClass, allowEmpty);
        assertNumberClass(numberClass);
        this.numberFormat = numberFormat;
    }

    private void assertNumberClass(Class numberClass) {
        Assert.isTrue(Number.class.isAssignableFrom(numberClass), "Property class must be a subclass of Number");
    }

    protected Object doConvert(Object number) throws TypeConversionException {
        if (this.numberFormat != null) {
            // use NumberFormat for rendering value
            return this.numberFormat.format((Number)number);
        }
        else {
            // use toString method for rendering value
            return number.toString();
        }
    }

    protected Object doConvertBack(Object text) throws TypeConversionException {
        // use given NumberFormat for parsing text
        if (this.numberFormat != null) {
            return NumberUtils.parseNumber((String)text, getConvertFromType(), this.numberFormat);
        }
        // use default valueOf methods for parsing text
        else {
            return NumberUtils.parseNumber((String)text, getConvertFromType());
        }
    }

}