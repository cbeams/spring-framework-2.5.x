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

/**
 * @author Keith Donald
 */
public class DefaultFormModel implements FormModel, MutableFormModel {
    protected static final Log logger = LogFactory
            .getLog(DefaultFormModel.class);

    private MutableAspectAccessStrategy domainObjectAccessStrategy;

    private ValueModel commitTrigger;

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

    public void addValidationListener(ValidationListener listener) {

    }

    public void removeValidationListener(ValidationListener listener) {

    }

    public void setFormProperties(String[] domainObjectProperties) {
        formValueModels.clear();
        for (int i = 0; i < domainObjectProperties.length; i++) {
            add(domainObjectProperties[i]);
        }
    }

    public ValueModel add(String domainObjectProperty) {
        ValueModel formValueModel = new AspectAdapter(
                domainObjectAccessStrategy, domainObjectProperty);
        if (bufferChanges) {
            formValueModel = new BufferedValueModel(formValueModel,
                    commitTrigger);
        }
        formValueModels.put(domainObjectProperty, formValueModel);
        onNewFormValueModel(domainObjectProperty, formValueModel);
        return formValueModel;
    }

    protected void onNewFormValueModel(String domainObjectProperty,
            ValueModel formValueModel) {
    }

    public ValueModel getValueModel(String domainObjectProperty) {
        ValueModel valueModel = (ValueModel)formValueModels
                .get(domainObjectProperty);
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