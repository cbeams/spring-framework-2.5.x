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

    private MutableAspectAccessStrategy domainObjectAccessStrategy;

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
            MutableAspectAccessStrategy domainObjectAccessStrategy) {
        this(domainObjectAccessStrategy, true);
    }

    public DefaultFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy,
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

    public void setFormProperties(String[] domainObjectProperties) {
        formValueModels.clear();
        for (int i = 0; i < domainObjectProperties.length; i++) {
            add(domainObjectProperties[i]);
        }
    }

    public void setParent(NestingFormModel parent) {
        this.parent = parent;
    }

    public boolean getBufferChanges() {
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

    public void addValueListener(String formProperty,
            ValueListener valueListener) {
        ValueModel valueModel = getValueModel(formProperty);
        assertValueModelNotNull(valueModel, formProperty);
        valueModel.addValueListener(valueListener);
    }

    public void removeValueListener(String formProperty,
            ValueListener valueListener) {
        ValueModel valueModel = getValueModel(formProperty);
        assertValueModelNotNull(valueModel, formProperty);
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

    public ValueModel add(String domainObjectProperty) {
        return add(domainObjectProperty, this.bufferChanges);
    }

    public ValueModel add(String domainObjectProperty, boolean bufferChanges) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding new form value model for property '"
                    + domainObjectProperty + "'");
        }
        ValueModel formValueModel = new AspectAdapter(
                domainObjectAccessStrategy, domainObjectProperty);
        if (bufferChanges) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating form value buffer for property '"
                        + domainObjectProperty + "'");
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
        return add(domainObjectProperty, formValueModel);
    }

    public ValueModel add(String domainObjectProperty, ValueModel formValueModel) {
        if (formValueModel instanceof BufferedValueModel) {
            ((BufferedValueModel)formValueModel)
                    .setCommitTrigger(commitTrigger);
        }
        formValueModel = preProcessNewFormValueModel(domainObjectProperty,
                formValueModel);
        formValueModels.put(domainObjectProperty, formValueModel);
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Registering '" + domainObjectProperty
                            + "' form property, property value model="
                            + formValueModel);
        }
        postProcessNewFormValueModel(domainObjectProperty, formValueModel);
        return formValueModel;
    }

    protected ValueModel preProcessNewFormValueModel(
            String domainObjectProperty, ValueModel formValueModel) {
        return formValueModel;
    }

    protected void postProcessNewFormValueModel(String domainObjectProperty,
            ValueModel formValueModel) {

    }

    public String getDisplayValue(String formProperty) {
        ValueModel valueModel = getDisplayValueModel(formProperty);
        assertValueModelNotNull(valueModel, formProperty);
        Object o = valueModel.get();
        if (o == null) { return ""; }
        return String.valueOf(o);
    }

    public ValueModel getDisplayValueModel(String formProperty) {
        return getValueModel(formProperty, true);
    }

    public Object getValue(String formProperty) {
        ValueModel valueModel = getValueModel(formProperty);
        assertValueModelNotNull(valueModel, formProperty);
        return valueModel.get();
    }

    public ValueModel getValueModel(String formProperty) {
        ValueModel valueModel = getDisplayValueModel(formProperty);
        return recursiveGetWrappedModel(valueModel);
    }

    private ValueModel recursiveGetWrappedModel(ValueModel valueModel) {
        if (valueModel instanceof ValueModelWrapper) { return recursiveGetWrappedModel(((ValueModelWrapper)valueModel)
                .getWrappedModel()); }
        return valueModel;
    }

    public ValueModel getValueModel(String domainObjectProperty,
            boolean queryParent) {
        ValueModel valueModel = (ValueModel)formValueModels
                .get(domainObjectProperty);
        if (valueModel == null) {
            if (parent != null && queryParent) {
                valueModel = parent.findValueModelFor(this,
                        domainObjectProperty);
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

    public MutableAspectAccessStrategy getAspectAccessStrategy() {
        return domainObjectAccessStrategy;
    }

    public MetaAspectAccessStrategy getMetaAspectAccessor() {
        return domainObjectAccessStrategy.getMetaAspectAccessor();
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

    protected MutableAspectAccessStrategy getAccessStrategy() {
        return domainObjectAccessStrategy;
    }

    public void commit() {
        if (logger.isDebugEnabled()) {
            logger.debug("Commit requested for this form model: " + this);
        }
        if (getFormObject() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Form object is null; nothing to commit.");
            }
            return;
        }
        if (!enabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("Form object is not enabled; nothing to commit.");
            }
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