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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.rules.Algorithms;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.UnaryProcedure;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class CompoundFormModel extends AbstractFormModel implements
        NestingFormModel {

    private Map formModels = new LinkedHashMap(9);

    public CompoundFormModel() {
    }

    public CompoundFormModel(Object domainObject) {
        this(new BeanPropertyAccessStrategy(domainObject));
    }

    public CompoundFormModel(ValueModel domainObjectHolder) {
        this(new BeanPropertyAccessStrategy(domainObjectHolder));
    }

    public CompoundFormModel(
            MutablePropertyAccessStrategy domainObjectAccessStrategy) {
        this(domainObjectAccessStrategy, true);
    }

    public CompoundFormModel(
            MutablePropertyAccessStrategy domainObjectAccessStrategy,
            boolean bufferChanges) {
        super(domainObjectAccessStrategy);
        setBufferChangesDefault(bufferChanges);
    }

    public MutableFormModel createChild(String childFormModelName) {
        ValidatingFormModel childModel = new ValidatingFormModel(
                getPropertyAccessStrategy(), getBufferChangesDefault());
        childModel.setRulesSource(getRulesSource());
        addChildModel(childFormModelName, childModel);
        return childModel;
    }

    public MutableFormModel createChild(String childFormModelName,
            String parentPropertyFormObjectPath) {
        return (MutableFormModel)createChildInternal(new ValidatingFormModel(),
                childFormModelName, parentPropertyFormObjectPath);
    }

    public NestingFormModel createCompoundChild(String childFormModelName,
            String parentPropertyFormObjectPath) {
        return (NestingFormModel)createChildInternal(new CompoundFormModel(),
                childFormModelName, parentPropertyFormObjectPath);
    }

    private FormModel createChildInternal(AbstractFormModel childFormModel,
            String childFormModelName, String parentPropertyFormObjectPath) {
        ValueModel valueHolder = new PropertyAdapter(
                getPropertyAccessStrategy(), parentPropertyFormObjectPath);
        if (getBufferChangesDefault()) {
            valueHolder = new BufferedValueModel(valueHolder);
        }
        boolean enabledDefault = valueHolder.get() != null;
        Class valueClass = getMetadataAccessStrategy().getPropertyType(
                parentPropertyFormObjectPath);
        new ChildFormObjectSetter(valueHolder, valueClass);
        return createChildInternal(childFormModel, childFormModelName,
                valueHolder, enabledDefault);
    }

    private static class ChildFormObjectSetter implements ValueListener {
        private ValueModel formObjectHolder;

        private Class formObjectClass;

        public ChildFormObjectSetter(ValueModel formObjectHolder,
                Class formObjectClass) {
            this.formObjectHolder = formObjectHolder;
            this.formObjectClass = formObjectClass;
            this.formObjectHolder.addValueListener(this);
            setIfNull();
        }

        public void valueChanged() {
            setIfNull();
        }

        public void setIfNull() {
            if (formObjectHolder.get() == null) {
                formObjectHolder.set(BeanUtils
                        .instantiateClass(formObjectClass));
            }
        }
    }

    public MutableFormModel createChild(String childFormModelName,
            ValueModel childFormObjectHolder) {
        return createChild(childFormModelName, childFormObjectHolder, true);
    }

    public NestingFormModel createCompoundChild(String childFormModelName,
            ValueModel childFormObjectHolder) {
        return createCompoundChild(childFormModelName, childFormObjectHolder,
                true);
    }

    public MutableFormModel createChild(String childFormModelName,
            ValueModel childFormObjectHolder, boolean enabled) {
        return (MutableFormModel)createChildInternal(new ValidatingFormModel(),
                childFormModelName, childFormObjectHolder, enabled);
    }

    public NestingFormModel createCompoundChild(String childFormModelName,
            ValueModel childFormObjectHolder, boolean enabled) {
        return (NestingFormModel)createChildInternal(new CompoundFormModel(),
                childFormModelName, childFormObjectHolder, enabled);
    }

    private FormModel createChildInternal(AbstractFormModel childModel,
            String childFormModelName, ValueModel childFormObjectHolder,
            boolean enabled) {
        MutablePropertyAccessStrategy childObjectAccessStrategy = getPropertyAccessStrategy()
                .newNestedAccessor(childFormObjectHolder);
        childModel.setPropertyAccessStrategy(childObjectAccessStrategy);
        childModel.setEnabled(enabled);
        childModel.setBufferChangesDefault(getBufferChangesDefault());
        childModel.setRulesSource(getRulesSource());
        addChildModel(childFormModelName, childModel);
        return childModel;
    }

    public NestableFormModel addChildModel(String childFormModelName,
            NestableFormModel childModel) {
        Assert.isTrue(getChildFormModel(childFormModelName) == null,
                "Child model by name '" + childFormModelName
                        + "' already exists");
        childModel.setParent(this);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding new nested form model '" + childFormModelName
                    + "', value=" + childModel);
        }
        formModels.put(childFormModelName, childModel);
        return childModel;
    }

    public void addValidationListener(final ValidationListener listener) {
        Algorithms.instance().forEachIn(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel).addValidationListener(listener);
                    }
                });
    }

    public void addValidationListener(ValidationListener listener,
            String childModelName) {
        FormModel model = getChildFormModel(childModelName);
        Assert.notNull(model, "No child model by name " + childModelName
                + "exists; unable to add listener");
        model.addValidationListener(listener);
    }

    public void removeValidationListener(ValidationListener listener,
            String childModelName) {
        FormModel model = getChildFormModel(childModelName);
        Assert.notNull(model, "No child model by name " + childModelName
                + "exists; unable to remove listener");
        model.removeValidationListener(listener);
    }

    public FormModel getChildFormModel(String childModelName) {
        return (FormModel)formModels.get(childModelName);
    }

    public void removeValidationListener(final ValidationListener listener) {
        Algorithms.instance().forEachIn(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel)
                                .removeValidationListener(listener);
                    }
                });
    }

    public ValueModel getDisplayValueModel(String formProperty) {
        // todo
        return null;
    }

    public ValueModel getValueModel(String formProperty) {
        return getValueModel(formProperty, true);
    }

    public ValueModel findValueModelFor(FormModel delegatingChild,
            String domainObjectProperty) {
        Iterator it = formModels.values().iterator();
        while (it.hasNext()) {
            NestableFormModel formModel = (NestableFormModel)it.next();
            if (delegatingChild != null && formModel == delegatingChild) {
                continue;
            }
            ValueModel valueModel = formModel.getValueModel(
                    domainObjectProperty, false);
            if (valueModel != null) { return valueModel; }
        }
        if (logger.isInfoEnabled()) {
            logger.info("No value model by name '" + domainObjectProperty
                    + "' found on any nested form models... returning [null]");
        }
        return null;
    }

    public ValueModel getValueModel(String formPropertyPath, boolean queryParent) {
        ValueModel valueModel = findValueModelFor(null, formPropertyPath);
        if (valueModel == null) {
            if (getParent() != null && queryParent) {
                valueModel = getParent().findValueModelFor(this,
                        formPropertyPath);
            }
        }
        return valueModel;
    }

    protected void handleEnabledChange() {
        Algorithms.instance().forEachIn(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel).setEnabled(isEnabled());
                    }
                });
    }

    public boolean getHasErrors() {
        return Algorithms.instance().areAnyTrue(formModels.values(),
                new UnaryPredicate() {
                    public boolean test(Object formModel) {
                        return ((FormModel)formModel).getHasErrors();
                    }
                });
    }

    public Map getErrors() {
        final Map allErrors = new HashMap();
        Algorithms.instance().forEachIn(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        allErrors.putAll(((FormModel)formModel).getErrors());
                    }
                });
        return allErrors;
    }

    public boolean isDirty() {
        return Algorithms.instance().areAnyTrue(formModels.values(),
                new UnaryPredicate() {
                    public boolean test(Object formModel) {
                        return ((FormModel)formModel).isDirty();
                    }
                });
    }

    public boolean hasErrors(String childModelName) {
        FormModel model = getChildFormModel(childModelName);
        Assert.notNull(model, "No child model by name " + childModelName
                + "exists.");
        return model.getHasErrors();
    }

    public void commit() {
        if (preEditCommit()) {
            Algorithms.instance().forEachIn(formModels.values(),
                    new UnaryProcedure() {
                        public void run(Object formModel) {
                            ((FormModel)formModel).commit();
                        }
                    });
            postEditCommit();
        }
    }

    public void revert() {
        Algorithms.instance().forEachIn(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel).revert();
                    }
                });
    }
}