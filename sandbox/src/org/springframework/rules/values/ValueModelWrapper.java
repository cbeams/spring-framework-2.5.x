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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Keith Donald
 */
public abstract class ValueModelWrapper implements ValueModel {
    protected final Log logger = LogFactory.getLog(getClass());

    private ValueModel wrappedModel;

    public ValueModelWrapper(ValueModel valueModel) {
        this.wrappedModel = valueModel;
    }

    protected ValueModel getWrappedModel() {
        return wrappedModel;
    }

    public Object get() {
        return wrappedModel.get();
    }

    public Object getWrapped() {
        if (wrappedModel instanceof ValueModelWrapper) {
            return ((ValueModelWrapper)wrappedModel).getWrapped();
        }
        else {
            return wrappedModel.get();
        }
    }

    public void set(Object value) {
        this.wrappedModel.set(value);
    }

    public void addValueListener(ValueListener l) {
        wrappedModel.addValueListener(l);
    }

    public void removeValueListener(ValueListener l) {
        wrappedModel.removeValueListener(l);
    }

}