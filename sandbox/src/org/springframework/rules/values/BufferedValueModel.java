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
 * A value model that wraps another value model; delaying or buffering changes
 * until a commit is triggered.
 * 
 * @author Keith Donald
 */
public class BufferedValueModel extends AbstractValueModel implements
        ValueModel {

    private static final Object NO_VALUE = new Object();

    private Object bufferedValue = NO_VALUE;

    private ValueModel wrappedModel;

    private ValueModel commitTrigger;

    public BufferedValueModel(ValueModel wrappedModel, ValueModel commitTrigger) {
        this.wrappedModel = wrappedModel;
        this.wrappedModel.addValueListener(new ValueListener() {
            public void valueChanged() {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("[Wrapped model value has changed; updating buffered value to ;"
                                    + BufferedValueModel.this.wrappedModel
                                            .get() + "']");
                }
                if (isChangeBuffered()) {
                    logger.warn("[Losing buffered edit " + get() + "]");
                }
                set(NO_VALUE);
            }
        });
        this.commitTrigger = commitTrigger;
        this.commitTrigger.addValueListener(new ValueListener() {
            public void valueChanged() {
                Boolean commit = (Boolean)((ValueModel)BufferedValueModel.this.commitTrigger)
                        .get();
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("[Commit trigger fired; trigger request value is '"
                                    + commit + "']");
                }
                if (commit == null) { return; }
                if (commit.booleanValue()) {
                    commit();
                }
                else {
                    revert();
                }
            }
        });
    }

    private void commit() {
        if (isChangeBuffered()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Committing buffered value '" + get()
                        + "' to wrapped value model " + wrappedModel + "]");
            }
            wrappedModel.set(bufferedValue);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[No buffered edit to commit; nothing to do...]");
            }
        }
    }

    public boolean isChangeBuffered() {
        return bufferedValue != NO_VALUE;
    }

    private void revert() {
        if (logger.isDebugEnabled()) {
            logger.debug("[Reverting buffered value '" + get() + " to value "
                    + wrappedModel.get() + "]");
        }
        set(NO_VALUE);
    }

    /**
     * @see org.springframework.rules.values.ValueModel#get()
     */
    public Object get() {
        if (bufferedValue != NO_VALUE) {
            return bufferedValue;
        }
        else {
            return wrappedModel.get();
        }
    }

    /**
     * @see org.springframework.rules.values.ValueModel#set(java.lang.Object)
     */
    public void set(Object value) {
        if (ObjectUtils.nullSafeEquals(this.bufferedValue, value)) { return; }
        if (logger.isDebugEnabled()) {
            if (value == NO_VALUE) {
                logger.debug("[Setting buffered value to NO_VALUE]");
            }
            else {
                logger.debug("[Setting buffered value to '" + value + "']");
            }
        }
        this.bufferedValue = value;
        fireValueChanged();
    }

}