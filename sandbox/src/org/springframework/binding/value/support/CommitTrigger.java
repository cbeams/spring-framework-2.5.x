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

/**
 * A ValueModel implementation that is intended to be used as trigger channel
 * for instances of BufferedValueModel. API users shall trigger commit and flush
 * events using <code>commit</code> and <code>revert</code>.
 * 
 * The value is of type <code>Boolean</code>.
 * <p>
 * The following example delays the commit of a buffered value:
 * 
 * <pre>
 * 
 *  
 *       ValueModel subject = new ValueHolder();
 *       CommitTrigger trigger = new CommitTrigger();
 *       BufferedValueModel buffer = new BufferedValueModel(subject, trigger);
 *       
 *       buffer.setValue(&quot;value&quot;);
 *       ...
 *       trigger.commit();
 *   
 *  
 * </pre>
 * 
 * @author Keith Donald
 * @author Karsten Lentzsch
 */
public final class CommitTrigger extends AbstractValueModel {

    private static final Boolean COMMIT = Boolean.TRUE;

    private static final Boolean REVERT = Boolean.FALSE;

    private static final Boolean NO_OP = null;

    private Boolean value;

    /**
     * Constructs a <code>Trigger</code> set to neutral.
     */
    public CommitTrigger() {
        value = NO_OP;
    }

    /**
     * Returns a Boolean that indicates the current trigger state.
     * 
     * @return a Boolean that indicates the current trigger state
     */
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if ((value != null) && !(value instanceof Boolean))
            throw new IllegalArgumentException(
                    "Trigger values must be of type Boolean.");
        this.value = (Boolean)value;
        fireValueChanged();
    }

    /**
     * Triggers a commit event in BufferedValueModels that share this Trigger.
     */
    public void commit() {
        if (COMMIT.equals(getValue())) {
            setValue(NO_OP);
        }
        setValue(COMMIT);
    }

    /**
     * Triggers a revert event in BufferedValueModels that share this Trigger.
     */
    public void revert() {
        if (REVERT.equals(getValue())) {
            setValue(NO_OP);
        }
        setValue(REVERT);
    }

    public boolean isNoOp() {
        return value == NO_OP;
    }

    public boolean isCommit() {
        return value == COMMIT;
    }

    public boolean isRevert() {
        return value == REVERT;
    }

}