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

import org.springframework.rules.Function;
import org.springframework.util.DefaultObjectStyler;

/**
 * @author Keith Donald
 */
public class RefreshableValueHolder extends ValueHolder {
    private Function refreshFunction;

    private boolean alwaysRefresh;

    public RefreshableValueHolder(Function refreshFunction) {
        this(refreshFunction, false);
    }

    public RefreshableValueHolder(Function refreshFunction,
            boolean alwaysRefresh) {
        super((alwaysRefresh ? refreshFunction.evaluate() : null));
        this.refreshFunction = refreshFunction;
    }

    public Object get() {
        if (alwaysRefresh) {
            refresh();
        }
        return super.get();
    }

    public void refresh() {
        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing held value '"
                    + DefaultObjectStyler.evaluate(get()) + "'");
        }
        set(refreshFunction.evaluate());
    }
}