/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

/**
 * Spring's default <code>toString()</code> styler. Underneath the hood, uses
 * the reflective visitor pattern to nicely encapsulate styling algorithms for
 * each type of styled object.
 * 
 * This class is used by ToStringBuilder to style toString() output in a
 * consistent manner.
 * 
 * @author Keith Donald
 */
public class SpringToStringStyler implements ToStringStyler {

    private SpringValueStyler stylerVisitor = new SpringValueStyler();

    public void styleStart(StringBuffer buffer, Object o) {
        buffer.append('[').append(ClassUtils.getShortName(o.getClass()));
        styleIdentityHashCode(buffer, o);
        if (o.getClass().isArray()) {
            buffer.append(' ');
            styleValue(buffer, o);
        }
    }

    /**
     * Append the {@link System#identityHashCode(java.lang.Object)}.
     * 
     * @param buffer
     *            the <code>StringBuffer</code> to append to.
     * @param object
     *            the <code>Object</code> whose id to output
     */
    private void styleIdentityHashCode(StringBuffer buffer, Object object) {
        buffer.append('@');
        buffer.append(Integer.toHexString(System.identityHashCode(object)));
    }

    public void styleEnd(StringBuffer buffer, Object o) {
        buffer.append(']');
    }

    public void styleField(
        StringBuffer buffer,
        String fieldName,
        Object value) {
        styleFieldStart(buffer, fieldName);
        styleValue(buffer, value);
    }

    public void styleValue(StringBuffer buffer, Object value) {
        buffer.append(stylerVisitor.styleValue(value));
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
