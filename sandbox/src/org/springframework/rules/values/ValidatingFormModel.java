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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.rules.RulesSource;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.reporting.BeanValidationResultsCollector;
import org.springframework.rules.reporting.PropertyResults;

/**
 * @author Keith Donald
 */
public class ValidatingFormModel extends DefaultFormModel implements
        AspectAccessStrategy {
    private RulesSource rulesSource;

    private Map validationErrors = new HashMap();

    private List validationListeners = new ArrayList();

    /**
     * @param domainObject
     */
    public ValidatingFormModel(Object domainObject) {
        super(domainObject);
    }

    /**
     * @param domainObjectHolder
     */
    public ValidatingFormModel(ValueModel domainObjectHolder) {
        super(domainObjectHolder);
    }

    /**
     * @param domainObjectAccessStrategy
     */
    public ValidatingFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy) {
        super(domainObjectAccessStrategy);
    }

    /**
     * @param domainObjectAccessStrategy
     * @param bufferChanges
     */
    public ValidatingFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy,
            boolean bufferChanges) {
        super(domainObjectAccessStrategy, bufferChanges);
    }

    public void setRulesSource(RulesSource rulesSource) {
        this.rulesSource = rulesSource;
    }

    public Object getValue(String aspect) {
        return getValueModel(aspect).get();
    }

    public Object getDomainObject() {
        return this;
    }

    public boolean hasErrors() {
        return validationErrors.size() > 0;
    }

    public int getFormPropertiesWithErrorsCount() {
        return validationErrors.size();
    }

    public int getTotalErrors() {
        Iterator it = validationErrors.values().iterator();
        int totalErrors = 0;
        while (it.hasNext()) {
            totalErrors += ((PropertyResults)it.next()).getViolatedCount();
        }
        return totalErrors;
    }

    public void addValidationListener(ValidationListener validationListener) {
        if (validationListener != null) {
            validationListeners.add(validationListener);
        }
    }

    public void removeValidationListener(ValidationListener validationListener) {
        if (validationListener != null) {
            validationListeners.remove(validationListener);
        }
    }

    protected void onNewFormValueModel(String domainObjectProperty,
            ValueModel formValueModel) {
        BeanPropertyExpression constraint = rulesSource.getRules(
                getDomainObjectClass(), domainObjectProperty);
        if (constraint != null) {
            FormValueModelValidator validator = new FormValueModelValidator(
                    constraint, formValueModel);
            formValueModel.addValueListener(validator);
            validator.validate();
        }
    }

    private class FormValueModelValidator implements ValueListener {
        private BeanPropertyExpression constraint;

        private ValueModel formValueModel;

        public FormValueModelValidator(BeanPropertyExpression constraint,
                ValueModel formValueModel) {
            this.constraint = constraint;
            this.formValueModel = formValueModel;
        }

        public void valueChanged() {
            validate();
        }

        public void validate() {
            BeanValidationResultsCollector collector = new BeanValidationResultsCollector(
                    ValidatingFormModel.this);
            PropertyResults results = (PropertyResults)collector
                    .collectPropertyResults(constraint);
            if (results == null) {
                constraintSatisfied(constraint, formValueModel);
            }
            else {
                constraintViolated(constraint, formValueModel, results);
            }
        }
    };

    protected void constraintSatisfied(BeanPropertyExpression exp,
            ValueModel formValueModel) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Value constraint '" + exp
                            + "' [satisfied] for value model '"
                            + formValueModel + "']");
        }
        if (validationErrors.containsKey(exp)) {
            validationErrors.remove(exp);
            fireConstraintSatisfied(exp);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Number of errors on form is now "
                    + validationErrors.size());
        }
    }

    private void fireConstraintSatisfied(BeanPropertyExpression constraint) {
        Iterator it = validationListeners.iterator();
        while (it.hasNext()) {
            ((ValidationListener)it.next()).constraintSatisfied(constraint);
        }
    }

    protected void constraintViolated(BeanPropertyExpression exp,
            ValueModel formValueModel, PropertyResults results) {
        if (logger.isDebugEnabled()) {
            logger.debug("Value constraint '" + exp
                    + "' [rejected] for value model '" + formValueModel
                    + "', results='" + results + "']");
        }
        //@TODO should change publisher should only publish on results changes
        // this means results needs business identity...
        validationErrors.put(exp, results);
        fireConstraintViolated(exp, results);
        if (logger.isDebugEnabled()) {
            logger.debug("Number of errors on form is now "
                    + validationErrors.size());
        }
    }

    private void fireConstraintViolated(BeanPropertyExpression constraint,
            PropertyResults results) {
        Iterator it = validationListeners.iterator();
        while (it.hasNext()) {
            ((ValidationListener)it.next()).constraintViolated(constraint,
                    results);
        }
    }

}