/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

/**
 * 
 * @author Keith
 */
public interface TypeConverter {
    public Class getConvertToClass();

    public Class getConvertFromClass();

    public Object convert(Object o) throws TypeConversionException;

    public Object convertBack(Object o) throws TypeConversionException;
}