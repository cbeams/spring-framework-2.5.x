/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.value.support;

public class ThreadLocalValueHolder extends AbstractValueModel {

    private static ThreadLocal threadLocal = new ThreadLocal();

    public Object getValue() {
        return threadLocal.get();
    }

    public void setValue(Object value) {
        Object oldValue = getValue();
        if (hasChanged(oldValue, value)) {
            threadLocal.set(value);
        }
        fireValueChanged(oldValue, value);
    }

    public void clear() {
        threadLocal.set(null);
    }

}