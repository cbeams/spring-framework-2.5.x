/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.springframework.binding.InvalidFormatException;
import org.springframework.binding.TypeConversionException;

/**
 * @author Keith Donald
 */
public class DateToStringTypeConverter extends AbstractToStringTypeConverter {

    private DateFormat dateFormat;

    /**
     * @param convertFromClass
     */
    public DateToStringTypeConverter(DateFormat dateFormat) {
        super(Date.class);
    }

    /**
     * @param convertFromClass
     * @param allowEmpty
     */
    public DateToStringTypeConverter(DateFormat dateFormat, boolean allowEmpty) {
        super(Date.class, allowEmpty);
    }

    // convert from date to string
    protected Object doConvert(Object date) throws TypeConversionException {
        return dateFormat.format((Date)date);
    }

    // convert back from string to date
    protected Object doConvertBack(Object dateString) throws TypeConversionException {
        try {
            return dateFormat.parse((String)dateString);
        }
        catch (ParseException ex) {
            throw new InvalidFormatException(dateString, getConvertFromClass(), getConvertToClass(), ex,
                    "Could not parse date: " + ex.getMessage());
        }
    }

}