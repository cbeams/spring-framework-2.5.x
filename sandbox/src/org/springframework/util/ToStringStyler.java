/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

/**
 * A strategy interface for pretty-printing toString() methods. Ecapsulates the
 * print algorithms; some other object such as a builder should provide the
 * workflow.
 * 
 * @author Keith Donald
 */
public interface ToStringStyler {

    /**
     * Style a toString()'ed object before its fields are styled.
     * 
     * @param buffer
     *            the buffer to print to.
     * @param o
     *            The object to style.
     */
    public void styleStart(StringBuffer buffer, Object o);

    /**
     * Style a toString()'ed object after it's fields are styled.
     * 
     * @param buffer
     *            the buffer to print to.
     * @param o
     *            The object to style.
     */
    public void styleEnd(StringBuffer buffer, Object o);

    /**
     * Style a field value as a string.
     * 
     * @param buffer
     *            buffer to print to.
     * @param fieldName
     *            The name of the field.
     * @param value
     *            The field value.
     * @return The pretty-printed string.
     */
    public void styleField(StringBuffer buffer, String fieldName, Object value);

    /**
     * Style the field separator.
     * 
     * @param buffer
     *            buffer to print to.
     */
    public void styleFieldSeparator(StringBuffer buffer);
}
