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
package org.springframework.binding.value.support;

import org.springframework.rules.Closure;
import org.springframework.util.DefaultObjectStyler;

/**
 * @author Keith Donald
 */
public class RefreshableValueHolder extends ValueHolder {
    private Closure refreshFunction;

    private boolean alwaysRefresh;

    public RefreshableValueHolder(Closure refreshFunction) {
        this(refreshFunction, false);
    }

    public RefreshableValueHolder(Closure refreshFunction,
            boolean alwaysRefresh) {
        super((alwaysRefresh ? refreshFunction.call(null) : null));
        this.refreshFunction = refreshFunction;
    }

    public Object getValue() {
        if (alwaysRefresh) {
            refresh();
        }
        return super.getValue();
    }

    public void refresh() {
        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing held value '"
                    + DefaultObjectStyler.call(getValue()) + "'");
        }
        setValue(refreshFunction.call(null));
    }
}