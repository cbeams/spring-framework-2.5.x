/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

/**
 * @author Keith Donald
 */
public class InvalidFormatException extends TypeConversionException {

    /**
     * @param value
     * @param convertToClass
     */
    public InvalidFormatException(Object value, Class convertToClass) {
        super(value, convertToClass);
    }

    /**
     * @param value
     * @param convertToClass
     * @param convertFromClass
     */
    public InvalidFormatException(Object value, Class convertToClass, Class convertFromClass) {
        super(value, convertToClass, convertFromClass);
    }

    /**
     * @param value
     * @param convertToClass
     * @param convertFromClass
     * @param cause
     * @param message
     */
    public InvalidFormatException(Object value, Class convertToClass, Class convertFromClass, Throwable cause,
            String message) {
        super(value, convertToClass, convertFromClass, cause, message);
    }

    /**
     * @param value
     * @param convertToClass
     * @param cause
     */
    public InvalidFormatException(Object value, Class convertToClass, Throwable cause) {
        super(value, convertToClass, cause);
    }

    /**
     * @param value
     * @param convertToClass
     * @param cause
     * @param message
     */
    public InvalidFormatException(Object value, Class convertToClass, Throwable cause, String message) {
        super(value, convertToClass, cause, message);
    }

}