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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class DefaultFormModel extends AbstractPropertyChangePublisher implements
        MutableFormModel {
    public static final String HAS_ERRORS_PROPERTY = "hasErrors";

    protected final Log logger = LogFactory.getLog(getClass());

    private MutablePropertyAccessStrategy domainObjectAccessStrategy;

    private ValueModel commitTrigger;

    private NestingFormModel parent;

    private Map formValueModels = new HashMap();

    private boolean bufferChanges = true;

    private Set commitListeners;

    private boolean enabled = true;

    public DefaultFormModel(Object domainObject) {
        this(new BeanPropertyAccessStrategy(domainObject));
    }

    public DefaultFormModel(ValueModel domainObjectHolder) {
        this(new BeanPropertyAccessStrategy(domainObjectHolder));
    }

    public DefaultFormModel(
            MutablePropertyAccessStrategy domainObjectAccessStrategy) {
        this(domainObjectAccessStrategy, true);
    }

    public DefaultFormModel(
            MutablePropertyAccessStrategy domainObjectAccessStrategy,
            boolean bufferChanges) {
        this.domainObjectAccessStrategy = domainObjectAccessStrategy;
        this.commitTrigger = new ValueHolder(null);
        // @TODO this seems kind of hacky - does it make sense to always commit
        // the form object value model if it is buffered? or is that the
        // responsiblity
        // of a higher-level object?
        if (getFormObjectHolder() instanceof BufferedValueModel) {
            ((BufferedValueModel)getFormObjectHolder())
                    .setCommitTrigger(commitTrigger);
        }
        setBufferChangesDefault(bufferChanges);
    }

    public void setFormProperties(String[] formPropertyPaths) {
        formValueModels.clear();
        for (int i = 0; i < formPropertyPaths.length; i++) {
            add(formPropertyPaths[i]);
        }
    }

    public void setParent(NestingFormModel parent) {
        this.parent = parent;
    }

    public boolean getBufferChangesDefault() {
        return bufferChanges;
    }

    public void setBufferChangesDefault(boolean bufferChanges) {
        this.bufferChanges = bufferChanges;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (hasChanged(this.enabled, enabled)) {
            this.enabled = enabled;
            handleEnabledChange();
            firePropertyChange("enabled", !this.enabled, enabled);
        }
    }

    protected void handleEnabledChange() {
        if (this.enabled) {
            validate();
        }
        else {
            clearErrors();
        }
    }

    protected Iterator valueModelIterator() {
        return this.formValueModels.values().iterator();
    }

    protected void validate() {

    }

    protected void clearErrors() {

    }

    public void addCommitListener(CommitListener listener) {
        getOrCreateCommitListeners().add(listener);
    }

    public void removeCommitListener(CommitListener listener) {
        getOrCreateCommitListeners().remove(listener);
    }

    private Set getOrCreateCommitListeners() {
        if (this.commitListeners == null) {
            this.commitListeners = new HashSet(6);
        }
        return commitListeners;
    }

    public void addValidationListener(ValidationListener listener) {
        throw new UnsupportedOperationException();
    }

    public void removeValidationListener(ValidationListener listener) {
        throw new UnsupportedOperationException();
    }

    public void addValueListener(String formPropertyPath,
            ValueListener valueListener) {
        ValueModel valueModel = getValueModel(formPropertyPath);
        assertValueModelNotNull(valueModel, formPropertyPath);
        valueModel.addValueListener(valueListener);
    }

    public void removeValueListener(String formPropertyPath,
            ValueListener valueListener) {
        ValueModel valueModel = getValueModel(formPropertyPath);
        assertValueModelNotNull(valueModel, formPropertyPath);
        valueModel.removeValueListener(valueListener);
    }

    private void assertValueModelNotNull(ValueModel valueModel,
            String formProperty) {
        Assert
                .isTrue(
                        valueModel != null,
                        "The property '"
                                + formProperty
                                + "' has not been added to this form model (or to any parents.)");
    }

    public ValueModel add(String formPropertyPath) {
        return add(formPropertyPath, this.bufferChanges);
    }

    public ValueModel add(String formPropertyPath, boolean bufferChanges) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding new form value model for property '"
                    + formPropertyPath + "'");
        }
        ValueModel formValueModel = new PropertyAdapter(
                domainObjectAccessStrategy, formPropertyPath);
        if (bufferChanges) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating form value buffer for property '"
                        + formPropertyPath + "'");
            }
            formValueModel = new BufferedValueModel(formValueModel,
                    commitTrigger);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("No buffer created; value model updates will commit directly to the domain layer");
            }
        }
        return add(formPropertyPath, formValueModel);
    }

    public ValueModel add(String formPropertyPath, ValueModel formValueModel) {
        if (formValueModel instanceof BufferedValueModel) {
            ((BufferedValueModel)formValueModel)
                    .setCommitTrigger(commitTrigger);
        }
        formValueModel = preProcessNewFormValueModel(formPropertyPath,
                formValueModel);
        formValueModels.put(formPropertyPath, formValueModel);
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Registering '" + formPropertyPath
                            + "' form property, property value model="
                            + formValueModel);
        }
        postProcessNewFormValueModel(formPropertyPath, formValueModel);
        return formValueModel;
    }

    protected ValueModel preProcessNewFormValueModel(
            String formPropertyPath, ValueModel formValueModel) {
        return formValueModel;
    }

    protected void postProcessNewFormValueModel(String formPropertyPath,
            ValueModel formValueModel) {

    }

    public String getDisplayValue(String formPropertyPath) {
        ValueModel valueModel = getDisplayValueModel(formPropertyPath);
        assertValueModelNotNull(valueModel, formPropertyPath);
        Object o = valueModel.get();
        if (o == null) { return ""; }
        return String.valueOf(o);
    }

    public ValueModel getDisplayValueModel(String formPropertyPath) {
        return getValueModel(formPropertyPath, true);
    }

    public Object getValue(String formPropertyPath) {
        ValueModel valueModel = getValueModel(formPropertyPath);
        assertValueModelNotNull(valueModel, formPropertyPath);
        return valueModel.get();
    }

    public ValueModel getValueModel(String formPropertyPath) {
        ValueModel valueModel = getDisplayValueModel(formPropertyPath);
        return recursiveGetWrappedModel(valueModel);
    }

    private ValueModel recursiveGetWrappedModel(ValueModel valueModel) {
        if (valueModel instanceof ValueModelWrapper) { return recursiveGetWrappedModel(((ValueModelWrapper)valueModel)
                .getWrappedModel()); }
        return valueModel;
    }

    public ValueModel getValueModel(String formPropertyPath,
            boolean queryParent) {
        ValueModel valueModel = (ValueModel)formValueModels
                .get(formPropertyPath);
        if (valueModel == null) {
            if (parent != null && queryParent) {
                valueModel = parent.findValueModelFor(this,
                        formPropertyPath);
            }
        }
        return valueModel;
    }

    public boolean getHasErrors() {
        return false;
    }

    public Map getErrors() {
        return Collections.EMPTY_MAP;
    }

    public boolean isDirty() {
        if (getFormObject() instanceof FormObject) {
            return ((FormObject)getFormObject()).isDirty();
        }
        else if (bufferChanges) {
            Iterator it = formValueModels.values().iterator();
            while (it.hasNext()) {
                ValueModel model = (ValueModel)it.next();
                if (model instanceof BufferedValueModel) {
                    BufferedValueModel bufferable = (BufferedValueModel)model;
                    if (bufferable.isDirty()) { return true; }
                }
            }
        }
        return false;
    }

    public MutablePropertyAccessStrategy getPropertyAccessStrategy() {
        return domainObjectAccessStrategy;
    }

    public PropertyMetadataAccessStrategy getMetadataAccessStrategy() {
        return domainObjectAccessStrategy.getMetadataAccessStrategy();
    }

    public Object getFormObject() {
        return domainObjectAccessStrategy.getDomainObject();
    }

    public ValueModel getFormObjectHolder() {
        return domainObjectAccessStrategy.getDomainObjectHolder();
    }

    protected Class getFormObjectClass() {
        return domainObjectAccessStrategy.getDomainObject().getClass();
    }

    protected MutablePropertyAccessStrategy getAccessStrategy() {
        return domainObjectAccessStrategy;
    }

    public void commit() {
        if (logger.isDebugEnabled()) {
            logger.debug("Commit requested for this form model " + this);
        }
        if (getFormObject() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Form object is null; nothing to commit.");
            }
            return;
        }
        if (!enabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("Form is not enabled; commiting null value.");
            }
            getFormObjectHolder().set(null);
            if (getFormObjectHolder() instanceof BufferedValueModel) {
                ((BufferedValueModel)getFormObjectHolder()).commit();
            }
            return;
        }
        if (bufferChanges) {
            if (getHasErrors()) { throw new IllegalStateException(
                    "Form has errors; submit not allowed."); }
            if (preEditCommit()) {
                commitTrigger.set(Boolean.TRUE);
                commitTrigger.set(null);
                postEditCommit();
            }
        }
    }

    protected boolean preEditCommit() {
        if (commitListeners == null) { return true; }
        for (Iterator i = commitListeners.iterator(); i.hasNext();) {
            CommitListener l = (CommitListener)i.next();
            if (!l.preEditCommitted(getFormObject())) { return false; }
        }
        return true;
    }

    protected void postEditCommit() {
        if (commitListeners == null) { return; }
        for (Iterator i = commitListeners.iterator(); i.hasNext();) {
            CommitListener l = (CommitListener)i.next();
            l.postEditCommitted(getFormObject());
        }
    }

    public void revert() {
        if (bufferChanges) {
            commitTrigger.set(Boolean.FALSE);
            commitTrigger.set(null);
        }
    }
}