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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.rules.RulesProvider;
import org.springframework.rules.RulesSource;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.reporting.BeanValidationResultsCollector;
import org.springframework.rules.reporting.PropertyResults;
import org.springframework.rules.reporting.TypeResolvable;

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

    public Map getErrors() {
        return Collections.unmodifiableMap(validationErrors);
    }

    public int getFormPropertiesWithErrorsCount() {
        return validationErrors.size();
    }

    public int getTotalErrorCount() {
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

    protected ValueModel preProcessNewFormValueModel(
            String domainObjectProperty, ValueModel formValueModel) {
        if (!(formValueModel instanceof TypeConverter)) {
            if (getFormObject() instanceof PropertyEditorProvider) {
                PropertyEditorProvider provider = (PropertyEditorProvider)getFormObject();
                PropertyEditor editor = provider
                        .getPropertyEditor(domainObjectProperty);
                formValueModel = installTypeConverter(formValueModel,
                        domainObjectProperty, editor);
            }
            else {
                PropertyEditor editor = getAspectAccessStrategy()
                        .findCustomEditor(
                                getMetaAspectAccessor().getAspectClass(
                                        domainObjectProperty),
                                domainObjectProperty);
                formValueModel = installTypeConverter(formValueModel,
                        domainObjectProperty, editor);
            }
        }
        return new ValidatingFormValueModel(domainObjectProperty,
                formValueModel, getValidationRule(domainObjectProperty));
    }

    private ValueModel installTypeConverter(ValueModel formValueModel,
            String domainObjectProperty, PropertyEditor editor) {
        if (editor != null) {
            TypeConverter converter = new TypeConverter(formValueModel, editor);
            if (logger.isDebugEnabled()) {
                logger.debug("Installed type converter '" + converter
                        + "' with editor '" + editor + "' for property '"
                        + domainObjectProperty + "'");
            }
            return converter;
        }
        else {
            return formValueModel;
        }
    }

    protected void postProcessNewFormValueModel(String domainObjectProperty,
            ValueModel valueModel) {
        // trigger validation to catch initial form errors
        if (valueModel instanceof ValidatingFormValueModel) {
            ((ValidatingFormValueModel)valueModel).validate();
        }
    }

    protected BeanPropertyExpression getValidationRule(
            String domainObjectProperty) {
        BeanPropertyExpression constraint;
        //@TODO if form object changes, rules aren't updated...introduces
        // subtle bugs...
        // ... for rules dependent on instance...
        if (getFormObject() instanceof RulesProvider) {
            constraint = ((RulesProvider)getFormObject())
                    .getRules(domainObjectProperty);
        }
        else {
            if (rulesSource == null) {
                logger
                        .info("No rules source has been configured; "
                                + "please set a valid reference to enable rules-based validation.");
            }
            constraint = rulesSource.getRules(getFormObjectClass(),
                    domainObjectProperty);
        }
        return constraint;
    }

    private class ValidatingFormValueModel extends ValueModelWrapper {
        private BeanPropertyExpression setterConstraint;

        private BeanPropertyExpression validationRule;

        private boolean valueIsSetting;

        public ValidatingFormValueModel(String domainObjectProperty,
                ValueModel model, BeanPropertyExpression validationRule) {
            super(model);
            this.setterConstraint = new ValueSetterConstraint(
                    getWrappedModel(), domainObjectProperty);
            this.validationRule = validationRule;
            addValueListener(new ValueListener() {
                public void valueChanged() {
                    // validatee any changes that didn't occur as the result
                    // of an explicit set(value) call...maybe value was updated
                    // underneath via another mechanism... this is kinda
                    // tricky...
                    if (!valueIsSetting) {
                        validate();
                    }
                }
            });
        }

        public void set(Object value) {
            try {
                valueIsSetting = true;
                if (!setterConstraint.test(value)) {
                    PropertyResults results = new PropertyResults(
                            setterConstraint.getPropertyName(), value,
                            setterConstraint);
                    constraintViolated(setterConstraint, this, results);
                }
                else {
                    constraintSatisfied(setterConstraint, this);
                    // we validate after a set attempt
                    validate();
                }
            }
            finally {
                valueIsSetting = false;
            }
        }

        public void validate() {
            if (validationRule == null) { return; }
            if (logger.isDebugEnabled()) {
                logger.debug("[Validating domain object property '"
                        + validationRule.getPropertyName() + "']");
            }
            BeanValidationResultsCollector collector = new BeanValidationResultsCollector(
                    ValidatingFormModel.this);
            PropertyResults results = (PropertyResults)collector
                    .collectPropertyResults(validationRule);
            if (results == null) {
                constraintSatisfied(validationRule, this);
            }
            else {
                constraintViolated(validationRule, this, results);
            }
        }

    }

    private class ValueSetterConstraint implements BeanPropertyExpression,
            TypeResolvable {
        private ValueModel valueModel;

        private String property;

        private String type = "typeMismatch";

        public ValueSetterConstraint(ValueModel valueModel, String property) {
            this.valueModel = valueModel;
            this.property = property;
        }

        public String getType() {
            return type;
        }

        public String getPropertyName() {
            return property;
        }

        public boolean test(Object value) {
            //@TODO this error handling needs work - message source resolvable?
            try {
                if (logger.isDebugEnabled()) {
                    Class valueClass = (value != null ? value.getClass() : null);
                    logger.debug("Setting value to convert/validate '" + value
                            + "', class=" + valueClass);
                }
                valueModel.set(value);
                return true;
            }
            catch (NullPointerException e) {
                logger.warn("Null pointer exception occured setting value", e);
                type = "required";
                return false;
            }
            catch (IllegalArgumentException e) {
                logger.warn("Illegal argument exception occured setting value");
                type = "typeMismatch";
                return false;
            }
            catch (Exception e) {
                logger.warn("Exception occured setting value", e);
                type = "unknown";
                return false;
            }
        }
    }

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