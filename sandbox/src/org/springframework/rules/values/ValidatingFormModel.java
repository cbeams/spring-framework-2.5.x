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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.rules.RulesProvider;
import org.springframework.rules.RulesSource;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.reporting.BeanValidationResultsCollector;
import org.springframework.rules.reporting.PropertyResults;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class ValidatingFormModel extends DefaultFormModel implements
        AspectAccessStrategy {
    private RulesSource rulesSource;

    private Map validationErrors = new HashMap();

    private List validationListeners = new ArrayList();

    // @TODO property editor registry
    public ValidatingFormModel(Object domainObject) {
        super(domainObject);
    }

    public ValidatingFormModel(ValueModel domainObjectHolder) {
        super(domainObjectHolder);
    }

    public ValidatingFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy) {
        super(domainObjectAccessStrategy);
    }

    public ValidatingFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy,
            boolean bufferChanges) {
        super(domainObjectAccessStrategy, bufferChanges);
    }

    public void setRulesSource(RulesSource rulesSource) {
        this.rulesSource = rulesSource;
    }

    public MetaAspectAccessStrategy getMetaAspectAccessor() {
        return getAccessStrategy().getMetaAspectAccessor();
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

    protected ValueModel onPreProcessNewFormValueModel(
            String domainObjectProperty, ValueModel formValueModel) {
        if (getFormObject() instanceof PropertyEditorProvider) {
            PropertyEditorProvider provider = (PropertyEditorProvider)getFormObject();
            PropertyEditor editor = provider
                    .getPropertyEditor(domainObjectProperty);
            if (editor != null) {
                TypeConverter converter = new TypeConverter(formValueModel,
                        editor);
                if (logger.isDebugEnabled()) {
                    logger.debug("Installed type converter '" + converter
                            + "' with editor '" + editor + "' for property '"
                            + domainObjectProperty + "'");
                }
                formValueModel = converter;
            }
        }
        return formValueModel;
    }

    protected void onFormValueModelAdded(String domainObjectProperty,
            ValueModel formValueModel) {
        Assert
                .notNull(rulesSource,
                        "No rules source has been configured; please set a valid reference.");
        BeanPropertyExpression constraint;
        //@TODO if form object changes, rules aren't updated...introduces
        // subtle bugs...
        // ... for rules dependent on instance...
        if (getFormObject() instanceof RulesProvider) {
            constraint = ((RulesProvider)getFormObject())
                    .getRules(domainObjectProperty);
        }
        else {
            constraint = rulesSource.getRules(getFormObjectClass(),
                    domainObjectProperty);
        }
        if (constraint != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating form validator for property '"
                        + domainObjectProperty + "'");
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("[Validating domain object property '"
                        + constraint.getPropertyName() + "']");
            }
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
            ((ValidationListener)it.next())
                    .constraintSatisfied(new ValidationEvent(this, constraint));
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
            ((ValidationListener)it.next())
                    .constraintViolated(new ValidationEvent(this, constraint,
                            results));
        }
    }

}