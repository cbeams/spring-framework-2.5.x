/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValueHolder.java,v 1.1 2004-06-12 07:27:08 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:08 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.util.ObjectUtils;

/**
 * A simple value model that contains a single value. Notifies listeners when
 * the contained value changes.
 * 
 * @author Keith Donald
 */
public class ValueHolder extends AbstractValueModel {
    private Object value;

    public ValueHolder(Object defaultValue) {
        this.value = defaultValue;
    }

    public Object get() {
        return value;
    }

    public void set(Object value) {
        if (ObjectUtils.nullSafeEquals(this.value, value)) { return; }
        this.value = value;
        fireValueChanged();
    }
}