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
 * Spring's default <code>toString()</code> styler. Underneath the hood, uses
 * the reflective visitor pattern to nicely encapsulate styling algorithms for
 * each type of styled object.
 * 
 * This class is used by ToStringBuilder to style toString() output in a
 * consistent manner.
 * 
 * @author Keith Donald
 */
public class DefaultToStringStyler implements ToStringStyler {

    private ObjectStyler stylerVisitor = new DefaultObjectStyler();

    public void styleStart(StringBuffer buffer, Object o) {
        if (!o.getClass().isArray()) {
            buffer.append('[').append(ClassUtils.getShortName(o.getClass()));
            styleIdentityHashCode(buffer, o);
        } else {
            buffer.append('[');
            styleIdentityHashCode(buffer, o);
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
        buffer.append(ObjectUtils.getIdentityHexString(object));
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
        buffer.append(stylerVisitor.style(value));
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
