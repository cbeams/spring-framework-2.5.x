/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValidatingFormModel.java,v 1.2 2004-06-12 22:11:56 kdonald Exp $
 * $Revision: 1.2 $
 * $Date: 2004-06-12 22:11:56 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
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
            final ValueModel formValueModel) {
        final BeanPropertyExpression exp = rulesSource.getRules(
                getDomainObjectClass(), domainObjectProperty);
        if (exp != null) {
            formValueModel.addValueListener(new ValueListener() {
                public void valueChanged() {
                    BeanValidationResultsCollector collector = new BeanValidationResultsCollector(
                            ValidatingFormModel.this);
                    PropertyResults results = (PropertyResults)collector
                            .collectPropertyResults(exp);
                    if (results == null) {
                        constraintSatisfied(exp, formValueModel);
                    }
                    else {
                        constraintViolated(exp, formValueModel, results);
                    }
                }
            });
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
        validationErrors.remove(exp);
        fireConstraintSatisfied(exp);
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