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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.DefaultObjectStyler;

/**
 * Base helper implementation of a value model.
 * 
 * @author Keith Donald
 */
public abstract class AbstractValueModel implements ValueModel {
    protected Log logger = LogFactory.getLog(ValueModel.class);

    private List listeners = new ArrayList();

    /**
     * @see org.springframework.rules.values.ValueChangeable#addValueListener(org.springframework.rules.values.ValueListener)
     */
    public void addValueListener(ValueListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Adding value listener " + l + "]");
        }
        listeners.add(l);
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#removeValueListener(org.springframework.rules.values.ValueListener)
     */
    public void removeValueListener(ValueListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Removing value listener " + l + "]");
        }
        listeners.remove(l);
    }

    protected void fireValueChanged() {
        if (logger.isDebugEnabled()) {
            logger.debug("[Firing value changed event; newValue="
                    + DefaultObjectStyler.evaluate(get()) + "]");
        }
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ValueListener)it.next()).valueChanged();
        }
    }

}