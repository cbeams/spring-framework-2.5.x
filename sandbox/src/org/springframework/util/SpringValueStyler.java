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
 * Visitor that encapsulates value string styling algorithms.
 * 
 * @author Keith Donald
 */
public class SpringValueStyler implements Visitor {
    private ReflectiveVisitorSupport visitorSupport =
        new ReflectiveVisitorSupport();

    /**
     * Styles the string form of this value using the reflective visitor
     * pattern. The reflective help removes the need to define a vistable class
     * for each type of styled valued.
     * 
     * @param value
     *            The value to be styled.
     * @return The styled string.
     */
    public String styleValue(Object value) {
        return (String)visitorSupport.invokeVisit(this, value);
    }

    public String visitString(String value) {
        return ('\'' + value + '\'');
    }

    public String visitNumber(Number value) {
        return String.valueOf(value);
    }

    public String visitClass(Class clazz) {
        return ClassUtils.getShortName(clazz);
    }

    public String visitMethod(Method method) {
        return method.getName()
            + "@"
            + ClassUtils.getShortName(method.getDeclaringClass());
    }

    public String visitMap(Map value) {
        StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
        buffer.append("<map = { ");
        for (Iterator i = value.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            buffer.append(styleValue(entry));
            if (i.hasNext()) {
                buffer.append(',').append(' ');
            }
        }
        if (value.isEmpty()) {
            buffer.append("<none>");
        }
        buffer.append(" }>");
        return buffer.toString();
    }

    public String visitMapEntry(Map.Entry value) {
        return value.getKey() + " -> " + value.getValue();
    }

    public String visitSet(Set value) {
        StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
        buffer.append("<set = { ");
        for (Iterator i = value.iterator(); i.hasNext();) {
            buffer.append(styleValue(i.next()));
            if (i.hasNext()) {
                buffer.append(',').append(' ');
            }
        }
        if (value.isEmpty()) {
            buffer.append("<none>");
        }
        buffer.append(" }>");
        return buffer.toString();
    }

    public String visitList(List value) {
        StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
        buffer.append("<list = { ");
        for (Iterator i = value.iterator(); i.hasNext();) {
            buffer.append(styleValue(i.next()));
            if (i.hasNext()) {
                buffer.append(',').append(' ');
            }
        }
        if (value.isEmpty()) {
            buffer.append("<none>");
        }
        buffer.append(" }>");
        return buffer.toString();
    }

    public String visitObject(Object value) {
        if (value.getClass().isArray()) {
            return styleArray(getObjectArray(value));
        } else {
            return String.valueOf(value);
        }
    }

    public String visitNull() {
        return "[null]";
    }

    private String styleArray(Object[] array) {
        StringBuffer buffer = new StringBuffer(array.length * 8 + 16);
        buffer.append("<array = { ");
        for (int i = 0; i < array.length - 1; i++) {
            buffer.append(styleValue(array[i]));
            buffer.append(',').append(' ');
        }
        if (array.length > 0) {
            buffer.append(styleValue(array[array.length - 1]));
        } else {
            buffer.append("<none>");
        }
        buffer.append(" }>");
        return buffer.toString();
    }

    private Object[] getObjectArray(Object value) {
        if (value.getClass().getComponentType().isPrimitive()) {
            return ArrayUtils.toObjectArrayFromPrimitive(value);
        } else {
            return (Object[])value;
        }
    }

}