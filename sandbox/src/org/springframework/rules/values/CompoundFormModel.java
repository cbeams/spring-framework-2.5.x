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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.rules.Algorithms;
import org.springframework.rules.RulesSource;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.UnaryProcedure;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class CompoundFormModel implements FormModel {

    private MutableAspectAccessStrategy domainObjectAccessStrategy;

    private boolean bufferChanges;

    private Map formModels = new LinkedHashMap();

    private RulesSource rulesSource;

    public CompoundFormModel(Object domainObject) {
        this(new BeanPropertyAccessStrategy(domainObject));
    }

    public CompoundFormModel(ValueModel domainObjectHolder) {
        this(new BeanPropertyAccessStrategy(domainObjectHolder));
    }

    public CompoundFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy) {
        this(domainObjectAccessStrategy, true);
    }

    public CompoundFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy,
            boolean bufferChanges) {
        this.domainObjectAccessStrategy = domainObjectAccessStrategy;
        this.bufferChanges = bufferChanges;
    }

    public void setRulesSource(RulesSource rulesSource) {
        this.rulesSource = rulesSource;
    }

    public MutableFormModel createChild(String childFormModelName) {
        Assert.isTrue(getChildFormModel(childFormModelName) == null,
                "Child model by name " + childFormModelName + "already exists");
        ValidatingFormModel childModel = new ValidatingFormModel(
                domainObjectAccessStrategy, bufferChanges);
        childModel.setRulesSource(rulesSource);
        formModels.put(childFormModelName, childModel);
        return childModel;
    }

    /**
     * @see org.springframework.rules.values.FormModel#addValidationListener(org.springframework.rules.values.ValidationListener)
     */
    public void addValidationListener(final ValidationListener listener) {
        Algorithms.instance().forEach(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel).addValidationListener(listener);
                    }
                });
    }

    public Object getValue(String domainObjectProperty) {
        return getValueModel(domainObjectProperty).get();
    }

    public ValueModel getValueModel(String domainObjectProperty) {
        Iterator it = formModels.values().iterator();
        while (it.hasNext()) {
            FormModel formModel = (FormModel)it.next();
            ValueModel valueModel = formModel.getValueModel(domainObjectProperty);
            if (valueModel != null) {
                return valueModel;
            }
        }
        return null;
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

    private FormModel getChildFormModel(String childModelName) {
        return (FormModel)formModels.get(childModelName);
    }

    /**
     * @see org.springframework.rules.values.FormModel#removeValidationListener(org.springframework.rules.values.ValidationListener)
     */
    public void removeValidationListener(final ValidationListener listener) {
        Algorithms.instance().forEach(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel)
                                .removeValidationListener(listener);
                    }
                });
    }

    public Object getFormObject() {
        return domainObjectAccessStrategy.getDomainObject();
    }
    
    public ValueModel getFormObjectHolder() {
        return domainObjectAccessStrategy.getDomainObjectHolder();
    }

    /**
     * @see org.springframework.rules.values.FormModel#hasErrors()
     */
    public boolean hasErrors() {
        return Algorithms.instance().areAnyTrue(formModels.values(),
                new UnaryPredicate() {
                    public boolean test(Object formModel) {
                        return ((FormModel)formModel).hasErrors();
                    }
                });
    }

    public boolean hasErrors(String childModelName) {
        FormModel model = getChildFormModel(childModelName);
        Assert.notNull(model, "No child model by name " + childModelName
                + "exists.");
        return model.hasErrors();
    }

    /**
     * @see org.springframework.rules.values.FormModel#commit()
     */
    public void commit() {
        Algorithms.instance().forEach(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel).commit();
                    }
                });
    }

    /**
     * @see org.springframework.rules.values.FormModel#revert()
     */
    public void revert() {
        Algorithms.instance().forEach(formModels.values(),
                new UnaryProcedure() {
                    public void run(Object formModel) {
                        ((FormModel)formModel).revert();
                    }
                });
    }

}