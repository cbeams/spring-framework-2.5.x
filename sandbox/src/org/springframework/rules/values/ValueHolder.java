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

    public ValueHolder(Object value) {
        this.value = value;
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