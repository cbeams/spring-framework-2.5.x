/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * Spring's default <code>toString()</code> styler. Underneath the hood, uses
 * the reflective visitor pattern to nicely encapsulate styling algorithms for
 * each type of styled object.
 * 
 * This class is used by ToStringBuilder to style toString() output in
 * a consistent manner.
 * 
 * @author Keith Donald
 */
public class SpringToStringStyler implements ToStringStyler {

    public static class StylerVisitor implements Visitor {
        private ReflectiveVisitorSupport visitorSupport =
            new ReflectiveVisitorSupport();
        private StringBuffer buffer;

        public StylerVisitor(StringBuffer buffer) {
            setBuffer(buffer);
        }

        public void setBuffer(StringBuffer buffer) {
            this.buffer = buffer;
        }

        public void visitString(String value) {
            buffer.append('\'').append(value).append('\'');
        }

        public void visitNumber(Number value) {
            buffer.append(value);
        }

        public void visitClass(Class clazz) {
            buffer.append(ClassUtils.getShortName(clazz));
        }
        
        public void visitMethod(Method method) {
            buffer.append(method.getName() + "@" +
                    ClassUtils.getShortName(method.getDeclaringClass()));
        }

        public void visitMap(Map value) {
            buffer.append("<map = { ");
            for (Iterator i = value.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry)i.next();
                styleValue(entry);
                if (i.hasNext()) {
                    buffer.append(',').append(' ');
                }
            }
            if (value.isEmpty()) {
                buffer.append("<none>");
            }
            buffer.append(" }>");
        }

        public void visitMapEntry(Map.Entry value) {
            styleValue(value.getKey());
            buffer.append(" -> ");
            styleValue(value.getValue());
        }

        public void visitSet(Set value) {
            buffer.append("<set = { ");
            for (Iterator i = value.iterator(); i.hasNext();) {
                styleValue(i.next());
                if (i.hasNext()) {
                    buffer.append(',').append(' ');
                }
            }
            if (value.isEmpty()) {
                buffer.append("<none>");
            }
            buffer.append(" }>");
        }

        public void visitList(List value) {
            buffer.append("<list = { ");
            for (Iterator i = value.iterator(); i.hasNext();) {
                styleValue(i.next());
                if (i.hasNext()) {
                    buffer.append(',').append(' ');
                }
            }
            if (value.isEmpty()) {
                buffer.append("<none>");
            }
            buffer.append(" }>");
        }

        public void visitObject(Object value) {
            if (value.getClass().isArray()) {
                styleArray(getObjectArray(value));
            } else {
                buffer.append(String.valueOf(value));
            }
        }

        private void styleArray(Object[] array) {
            buffer.append("<array = { ");
            for (int i = 0; i < array.length - 1; i++) {
                styleValue(array[i]);
                buffer.append(',').append(' ');
            }
            if (array.length > 0) {
                styleValue(array[array.length - 1]);
            } else {
                buffer.append("<none>");
            }
            buffer.append(" }>");
        }

        private Object[] getObjectArray(Object value) {
            if (value.getClass().getComponentType().isPrimitive()) {
                return ArrayUtils.toObjectArrayFromPrimitive(value);
            } else {
                return (Object[])value;
            }
        }

        public void visitNull() {
            buffer.append("<null>");
        }

        public void styleValue(Object value) {
            visitorSupport.invokeVisit(this, value);
        }

        public String toString() {
            return buffer.toString();
        }

    }

    private StylerVisitor stylerVisitor;

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
        if (stylerVisitor == null) {
            stylerVisitor = new StylerVisitor(buffer);
        } else {
            stylerVisitor.setBuffer(buffer);
        }
        stylerVisitor.styleValue(value);
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
