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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class DefaultFormModel implements MutableFormModel {
    protected static final Log logger = LogFactory
            .getLog(DefaultFormModel.class);

    private MutableAspectAccessStrategy domainObjectAccessStrategy;

    private ValueModel commitTrigger;

    private NestingFormModel parent;

    private Map formValueModels = new HashMap();

    private boolean bufferChanges = true;

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
        this.bufferChanges = bufferChanges;
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

    public void addValidationListener(ValidationListener listener) {
        throw new UnsupportedOperationException();
    }

    public void removeValidationListener(ValidationListener listener) {
        throw new UnsupportedOperationException();
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
        add(domainObjectProperty, formValueModel);
        return formValueModel;
    }

    protected ValueModel add(String domainObjectProperty,
            ValueModel formValueModel) {
        formValueModel = onPreProcessNewFormValueModel(domainObjectProperty,
                formValueModel);
        formValueModels.put(domainObjectProperty, formValueModel);
        onFormValueModelAdded(domainObjectProperty, formValueModel);
        return formValueModel;
    }

    protected ValueModel onPreProcessNewFormValueModel(
            String domainObjectProperty, ValueModel formValueModel) {
        return formValueModel;
    }

    protected void onFormValueModelAdded(String domainObjectProperty,
            ValueModel formValueModel) {

    }

    public Object getValue(String domainObjectProperty) {
        ValueModel valueModel = getValueModel(domainObjectProperty);
        Assert
                .isTrue(
                        valueModel != null,
                        "The property '"
                                + domainObjectProperty
                                + "' value model has not been added to this form model (or to any parents.)");
        return valueModel.get();
    }

    public ValueModel getValueModel(String domainObjectProperty) {
        return getValueModel(domainObjectProperty, true);
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

    public boolean hasErrors() {
        return false;
    }

    public AspectAccessStrategy getAspectAccessStrategy() {
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
        if (bufferChanges) {
            if (hasErrors()) { throw new IllegalStateException(
                    "Form has errors; submit not allowed"); }
            commitTrigger.set(Boolean.TRUE);
            commitTrigger.set(null);
        }
    }

    public void revert() {
        if (bufferChanges) {
            commitTrigger.set(Boolean.FALSE);
            commitTrigger.set(null);
        }
    }
}