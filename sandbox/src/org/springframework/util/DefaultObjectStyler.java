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

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.collection.PersistentCollection;

import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * Visitor that encapsulates value string styling algorithms according to Spring
 * conventions.
 * 
 * @author Keith Donald
 */
public class DefaultObjectStyler implements Visitor, ObjectStyler {

    private ReflectiveVisitorSupport visitorSupport = new ReflectiveVisitorSupport();

    private static final String EMPTY = "[empty]";

    private static final String NULL = "[null]";

    private static final String COLLECTION = "collection";

    private static final String SET = "set";

    private static final String LIST = "list";

    private static final String MAP = "map";

    private static final String ARRAY = "array";

    private static final ObjectStyler INSTANCE = new DefaultObjectStyler();

    /**
     * Return the default object styler singleton instance, for convenient
     * access.
     * 
     * @return The default object styler
     */
    public static ObjectStyler instance() {
        return INSTANCE;
    }

    /**
     * Static accessor that calls the default styler's style method.
     * 
     * @param o the argument to style
     * @return The styled string.
     */
    public static String call(Object o) {
        return INSTANCE.style(o);
    }

    /**
     * Styles the string form of this object using the reflective visitor
     * pattern. The reflective help removes the need to define a vistable class
     * for each type of styled valued.
     * 
     * @param o The object to be styled.
     * @return The styled string.
     */
    public String style(Object o) {
        return (String)visitorSupport.invokeVisit(this, o);
    }

    String visit(String value) {
        return ('\'' + value + '\'');
    }

    String visit(Number value) {
        return NumberFormat.getInstance().format(value);
    }

    String visit(Class clazz) {
        return ClassUtils.getShortName(clazz);
    }

    String visit(Method method) {
        return method.getName() + "@" + ClassUtils.getShortName(method.getDeclaringClass());
    }

    String visit(Map value) {
        if (value instanceof PersistentCollection) {
            return visit((PersistentCollection)value);
        }
        else {
            return visitInternal(value);
        }
    }

    private String visitInternal(Map value) {
        StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
        buffer.append(MAP + "[");
        for (Iterator i = value.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            buffer.append(style(entry));
            if (i.hasNext()) {
                buffer.append(',').append(' ');
            }
        }
        if (value.isEmpty()) {
            buffer.append(EMPTY);
        }
        buffer.append("]");
        return buffer.toString();
    }

    String visit(Map.Entry value) {
        return style(value.getKey()) + " -> " + style(value.getValue());
    }

    String visit(Collection value) {
        if (value instanceof PersistentCollection) {
            return visit((PersistentCollection)value);
        }
        else {
            return visitInternal(value);
        }
    }

    private String visitInternal(Collection value) {
        StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
        buffer.append(getCollectionTypeString(value) + "[");
        for (Iterator i = value.iterator(); i.hasNext();) {
            buffer.append(style(i.next()));
            if (i.hasNext()) {
                buffer.append(',').append(' ');
            }
        }
        if (value.isEmpty()) {
            buffer.append(EMPTY);
        }
        buffer.append("]");
        return buffer.toString();
    }

    private String getCollectionTypeString(Collection value) {
        if (value instanceof List) {
            return LIST;
        }
        else if (value instanceof Set) {
            return SET;
        }
        else {
            return COLLECTION;
        }
    }

    String visit(PersistentCollection hibernateCollection) {
        if (hibernateCollection.wasInitialized()) {
            if (hibernateCollection instanceof Collection) {
                return visitInternal((Collection)hibernateCollection);
            }
            else if (hibernateCollection instanceof Map) {
                return visitInternal((Map)hibernateCollection);
            }
            else {
                return style("<initialized unknown hibernate collection>");
            }
        }
        else {
            return style("<uninitialized hibernate collection>");
        }
    }

    String visit(Object value) {
        if (value.getClass().isArray()) {
            return styleArray(getObjectArray(value));
        }
        else {
            return String.valueOf(value);
        }
    }

    String visitNull() {
        return NULL;
    }

    private String styleArray(Object[] array) {
        StringBuffer buffer = new StringBuffer(array.length * 8 + 16);
        buffer.append(ARRAY + "<" + StringUtils.delete(ClassUtils.getShortName(array.getClass()), ";") + ">[");
        for (int i = 0; i < array.length - 1; i++) {
            buffer.append(style(array[i]));
            buffer.append(',').append(' ');
        }
        if (array.length > 0) {
            buffer.append(style(array[array.length - 1]));
        }
        else {
            buffer.append(EMPTY);
        }
        buffer.append("]");
        return buffer.toString();
    }

    private Object[] getObjectArray(Object value) {
        if (value.getClass().getComponentType().isPrimitive()) {
            return ObjectUtils.toObjectArray(value);
        }
        else {
            return (Object[])value;
        }
    }

}