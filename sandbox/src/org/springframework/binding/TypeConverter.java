/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

/**
 * A type converter converts objects of one type to that of another and back
 * again.
 * @author Keith
 */
public interface TypeConverter {

    /**
     * Get the type this converter converts to, using the
     * <code>convert(Object)</code> method.
     * @return The convert to type.
     */
    public Class getConvertToType();

    /**
     * Get the type this converter converts from, using the
     * <code>convertBack(Object</code> method.
     * @return The convert from type.
     */
    public Class getConvertFromType();

    /**
     * Convert the provided object argument to the type specified by the
     * <code>convertToClass</code> property.
     * @param o the object to convert, of type <code>convetFromClass</code>
     * @return The converted object, of type <code>convertToClass</code>
     * @throws TypeConversionException An exception occured during conversion.
     */
    public Object convert(Object o) throws TypeConversionException;

    /**
     * Convert the provided object argument back to the type specified by the
     * <code>convertFromClass</code> property.
     * @param o the object to convert, of type <code>convertToClass</code>
     * @return The converted object, of type <code>convertFromClas</code>
     * @throws TypeConversionException An exception occured during conversion.
     */
    public Object convertBack(Object o) throws TypeConversionException;
}