/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

/**
 * Helper class for building pretty-printing toString methods (according to
 * Spring style conventions of course.)
 * 
 * @author Keith Donald, adapted from jakarta-commons-lang ToStringBuilder.
 */
public class ToStringBuilder {
    private static final ToStringStyler DEFAULT_STYLER = new SpringToStringStyler();
    private boolean field;
    private StringBuffer buffer = new StringBuffer(512);
    private ToStringStyler styler;
    private Object object;

    /**
     * Creates a ToStringBuilder for this object.
     * 
     * @param o
     *            the object to be stringified
     */
    public ToStringBuilder(Object o) {
        this(o, DEFAULT_STYLER);
    }

    /**
     * Creates a ToStringBuilder for this object with the provided style.
     * 
     * @param o
     *            the object to be stringified
     * @param styler
     *            the styler encapsulating pretty-print instructions
     */
    public ToStringBuilder(Object o, ToStringStyler styler) {
        this.object = o;
        this.styler = styler;
        this.styler.styleStart(buffer, object);
    }

    /**
     * Append a byte field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, byte value) {
        return append(fieldName, new Byte(value));
    }

    /**
     * Append a short field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, short value) {
        return append(fieldName, new Short(value));
    }

    /**
     * Append a integer field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, int value) {
        return append(fieldName, new Integer(value));
    }

    /**
     * Append a float field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, float value) {
        return append(fieldName, new Float(value));
    }

    /**
     * Append a double field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, double value) {
        return append(fieldName, new Double(value));
    }

    /**
     * Append a long field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, long value) {
        return append(fieldName, new Long(value));
    }

    /**
     * Append a boolean field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, boolean value) {
        return append(fieldName, new Boolean(value));
    }

    /**
     * Append a field value.
     * 
     * @param fieldName
     *            The name of the field, usually the member variable name.
     * @param value
     *            The field value.
     * @return this, to support call-chaining.
     */
    public ToStringBuilder append(String fieldName, Object value) {
        printSeparator();
        styler.styleField(buffer, fieldName, value);
        return this;
    }

    private void printSeparator() {
        if (field) {
            styler.styleFieldSeparator(buffer);
        } else {
            field = true;
        }
    }
    
    /**
     * Return the string built by this builder.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        styler.styleEnd(buffer, object);
        return buffer.toString();
    }
    

}
