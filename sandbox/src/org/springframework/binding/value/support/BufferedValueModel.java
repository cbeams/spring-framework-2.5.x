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

import org.springframework.binding.value.ValueChangeListener;
import org.springframework.binding.value.ValueModel;
import org.springframework.rules.Closure;
import org.springframework.rules.closure.NoArgBlock;
import org.springframework.util.ToStringBuilder;

/**
 * A value model that wraps another value model; delaying or buffering changes
 * until a commit is triggered.
 * 
 * @author Keith Donald
 */
public class BufferedValueModel extends AbstractValueModel implements
        ValueModel {

    protected static final Object NO_VALUE = new Object();

    public static final String IS_DIRTY_PROPERTY = "isDirty";

    private Object bufferedValue = NO_VALUE;

    private ValueModel wrappedModel;

    private CommitTrigger commitTrigger;

    private ValueChangeListener commitTriggerHandler;

    private ValueChangeListener wrappedModelChangeHandler;

    public BufferedValueModel(ValueModel wrappedModel) {
        this(wrappedModel, null);
    }

    public BufferedValueModel(ValueModel wrappedModel,
            CommitTrigger commitTrigger) {
        setWrappedModel(wrappedModel);
        setCommitTrigger(commitTrigger);
    }

    protected final void setWrappedModel(ValueModel wrappedModel) {
        this.wrappedModel = wrappedModel;
        this.wrappedModelChangeHandler = new WrappedModelValueChangeHandler();
        this.wrappedModel.addValueChangeListener(wrappedModelChangeHandler);
    }

    private class WrappedModelValueChangeHandler implements ValueChangeListener {
        public void valueChanged() {
            if (logger.isDebugEnabled()) {
                logger.debug("[Wrapped model value has changed; new value is '"
                        + BufferedValueModel.this.wrappedModel.getValue()
                        + "']");
            }
            onWrappedValueChanged();
        }
    }

    protected void onWrappedValueChanged() {
        if (hasChangeBuffered()) {
            if (logger.isInfoEnabled()) {
                logger.info("[Losing buffered edit " + getValue() + "]");
            }
            setValue(NO_VALUE);
        }
        else {
            fireValueChanged();
        }
    }

    public final void setCommitTrigger(CommitTrigger commitTrigger) {
        if (this.commitTrigger == commitTrigger) { return; }
        if (this.commitTriggerHandler == null) {
            this.commitTriggerHandler = new CommitTriggerHandler();
        }
        if (this.commitTrigger != null) {
            this.commitTrigger.removeValueChangeListener(commitTriggerHandler);
        }
        this.commitTrigger = commitTrigger;
        this.commitTrigger.addValueChangeListener(commitTriggerHandler);
    }

    private class CommitTriggerHandler implements ValueChangeListener {
        public void valueChanged() {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("[Commit trigger fired; trigger request value is '"
                                + commitTrigger.getValue() + "']");
            }
            if (commitTrigger.isNoOp()) { return; }
            if (commitTrigger.isCommit()) {
                commit();
            }
            else {
                revert();
            }
        }
    }

    public boolean isDirty() {
        return hasChangeBuffered();
    }

    public boolean hasChangeBuffered() {
        return bufferedValue != NO_VALUE;
    }

    public Object getValue() {
        if (bufferedValue != NO_VALUE) {
            return bufferedValue;
        }
        else {
            return wrappedModel.getValue();
        }
    }

    public final void setValue(Object value) {
        if (hasChanged(this.bufferedValue, value)) {
            if (logger.isDebugEnabled()) {
                if (value == NO_VALUE) {
                    logger.debug("[Setting buffered value to NO_VALUE]");
                }
                else {
                    logger.debug("[Setting buffered value to '" + value + "']");
                }
            }
            Object oldValue = (this.bufferedValue == NO_VALUE) ? getWrappedValue()
                    : this.bufferedValue;
            this.bufferedValue = value;
            fireValueChanged(oldValue,
                    (this.bufferedValue == NO_VALUE ? getWrappedValue()
                            : this.bufferedValue));
            firePropertyChange(VALUE_PROPERTY, oldValue, this.bufferedValue);
            firePropertyChange(IS_DIRTY_PROPERTY, !isDirty(), isDirty());
        }
    }

    public ValueModel getWrappedModel() {
        return wrappedModel;
    }

    public Object getWrappedValue() {
        return wrappedModel.getValue();
    }

    public final void commit() {
        if (hasChangeBuffered()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Committing buffered value '" + getValue()
                        + "' to wrapped value model " + wrappedModel + "]");
            }
            commitBufferedValueSilently();
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("[No buffered edit to commit; nothing to do...]");
            }
        }
    }

    private void commitBufferedValueSilently() {
        doSilently(new NoArgBlock() {
            protected void handle() {
                doBufferedValueCommit(bufferedValue);
                bufferedValue = NO_VALUE;
            }
        });
    }

    protected void doBufferedValueCommit(Object bufferedValue) {
        wrappedModel.setValue(bufferedValue);
    }

    protected void doSilently(Closure block) {
        updateSilently(wrappedModel, wrappedModelChangeHandler, block);
    }
    
    public final void revert() {
        if (logger.isDebugEnabled()) {
            logger.debug("[Reverting buffered value '" + getValue()
                    + " to value " + wrappedModel.getValue() + "]");
        }
        setValue(NO_VALUE);
    }

    public String toString() {
        return new ToStringBuilder(this).append("bufferedValue", bufferedValue)
                .toString();
    }

}