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

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Utility class that builds pretty-printing toString methods with pluggable
 * styling conventions. By default, ToStringBuilder's adhere to Spring's
 * toString() styling conventions.
 * 
 * @author Keith Donald
 */
public class ToStringCreator {
    private static ToStringStyler DEFAULT_STYLER = new DefaultToStringStyler();

    public static final void setDefaultToStringStyler(
            ToStringStyler defaultStyler) {
        DEFAULT_STYLER = defaultStyler;
    }

    private StringBuffer buffer = new StringBuffer(512);

    private ToStringStyler styler;

    private Object object;

    private boolean styledFirstField;

    /**
     * Creates a ToStringBuilder for this object.
     * 
     * @param o
     *            the object to be stringified
     */
    public ToStringCreator(Object o) {
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
    public ToStringCreator(Object o, ToStringStyler styler) {
        setObject(o);
        setToStringStyler(styler);
        this.styler.styleStart(buffer, object);
    }

    private void setObject(Object object) {
        Assert.notNull(object, "The object to be styled is required");
        this.object = object;
    }

    private void setToStringStyler(ToStringStyler styler) {
        this.styler = (styler != null ? styler : DEFAULT_STYLER);
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
    public ToStringCreator append(String fieldName, byte value) {
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
    public ToStringCreator append(String fieldName, short value) {
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
    public ToStringCreator append(String fieldName, int value) {
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
    public ToStringCreator append(String fieldName, float value) {
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
    public ToStringCreator append(String fieldName, double value) {
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
    public ToStringCreator append(String fieldName, long value) {
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
    public ToStringCreator append(String fieldName, boolean value) {
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
    public ToStringCreator append(String fieldName, Object value) {
        printFieldSeparatorIfNeccessary();
        styler.styleField(buffer, fieldName, value);
        return this;
    }

    /**
     * Append styled property=value pairs for each of the target object's java
     * bean properties.
     * <p>
     * Note: special handling is taken for parent/child relationships between
     * objects-- this method assumes each calls appendProperties(), to prevent
     * possible infinite recursion.
     * 
     * @return this, to support call-chaining.
     */
    public ToStringCreator appendProperties() {
        BeanWrapper wrapper = new BeanWrapperImpl(object);
        PropertyDescriptor[] properties = wrapper.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            if (property.getReadMethod() != null
                    && !"class".equals(property.getName())) {
                Object propertyValue = wrapper.getPropertyValue(property
                        .getName());
                if (propertyValue != null
                        && !BeanUtils
                                .isSimpleProperty(propertyValue.getClass())) {
                    wrapper.setWrappedInstance(propertyValue);
                    if (isBidirectionalRelationship(wrapper
                            .getPropertyDescriptors(), object.getClass())) {
                        if (wrapper.getPropertyDescriptor("name") != null) {
                            propertyValue = wrapper.getPropertyValue("name");
                        }
                        else if (wrapper.getPropertyDescriptor("displayName") != null) {
                            propertyValue = wrapper
                                    .getPropertyValue("displayName");
                        }
                        else {
                            wrapper.setWrappedInstance(object);
                            continue;
                        }
                    }
                    wrapper.setWrappedInstance(object);
                }
                append(property.getDisplayName(), propertyValue);
            }
        }
        return this;
    }

    private boolean isBidirectionalRelationship(
            PropertyDescriptor[] properties, Class propertyType) {
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            if (property.getPropertyType().equals(propertyType)) { return true; }
        }
        return false;
    }

    private void printFieldSeparatorIfNeccessary() {
        if (styledFirstField) {
            styler.styleFieldSeparator(buffer);
        }
        else {
            styledFirstField = true;
        }
    }

    public String toString() {
        styler.styleEnd(buffer, object);
        return buffer.toString();
    }

    /**
     * Convenient static utility method for returning a styled string rendering
     * this the provided object's javabean properties.
     * 
     * Note: be careful about using this method: use could result in infinite
     * recursion, specifically, where a bi-directional relationship exists
     * between two objects, both exposing the other as properties.
     * 
     * @param bean
     * @return The styled string
     */
    public static final String propertiesToString(Object bean) {
        return new ToStringCreator(bean).appendProperties().toString();
    }

    /**
     * A strategy interface for pretty-printing toString() methods. Ecapsulates
     * the print algorithms; some other object such as a builder should provide
     * the workflow.
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
        public void styleField(StringBuffer buffer, String fieldName,
                Object value);

        /**
         * Style the field separator.
         * 
         * @param buffer
         *            buffer to print to.
         */
        public void styleFieldSeparator(StringBuffer buffer);
    }

    /**
     * Spring's default <code>toString()</code> styler. Underneath the hood,
     * uses the reflective visitor pattern to nicely encapsulate styling
     * algorithms for each type of styled object.
     * 
     * This class is used by ToStringBuilder to style toString() output in a
     * consistent manner.
     * 
     * @author Keith Donald
     */
    public static class DefaultToStringStyler implements ToStringStyler {

        private ObjectStyler valueStyler;

        public DefaultToStringStyler() {
            this(DefaultObjectStyler.instance());
        }

        public DefaultToStringStyler(ObjectStyler valueStyler) {
            this.valueStyler = valueStyler;
        }

        public void styleStart(StringBuffer buffer, Object o) {
            if (!o.getClass().isArray()) {
                buffer.append('[')
                        .append(ClassUtils.getShortName(o.getClass()));
                styleIdentityHashCode(buffer, o);
            }
            else {
                buffer.append('[');
                styleIdentityHashCode(buffer, o);
                buffer.append(' ');
                styleValue(buffer, o);
            }
        }

        private void styleIdentityHashCode(StringBuffer buffer, Object object) {
            buffer.append('@');
            buffer.append(ObjectUtils.getIdentityHexString(object));
        }

        public void styleEnd(StringBuffer buffer, Object o) {
            buffer.append(']');
        }

        public void styleField(StringBuffer buffer, String fieldName,
                Object value) {
            styleFieldStart(buffer, fieldName);
            styleValue(buffer, value);
        }

        public void styleValue(StringBuffer buffer, Object value) {
            buffer.append(valueStyler.style(value));
        }

        public void styleFieldStart(StringBuffer buffer, String fieldName) {
            buffer.append(' ').append(fieldName).append(" = ");
        }

        public void styleFieldEnd(StringBuffer buffer, String fieldName) {

        }

        public void styleFieldSeparator(StringBuffer buffer) {
            buffer.append(',');
        }
    }
}