/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util;

/**
 * Helper class for building pretty-printing toString methods (according to
 * Spring style conventions of course.)
 * 
 * @author Keith Donald, adapted from jakarta-commons-lang ToStringBuilder.
 */
public class ToStringBuilder {
    private static final ToStringStyler DEFAULT_STYLER = new DefaultToStringStyler();
    private StringBuffer buffer = new StringBuffer(512);
    private ToStringStyler styler;
    private Object object;
    private boolean field;
    
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
        printFieldSeparator();
        styler.styleField(buffer, fieldName, value);
        return this;
    }

    private void printFieldSeparator() {
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
