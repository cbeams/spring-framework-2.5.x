/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValidatingFormModel.java,v 1.1 2004-06-12 16:32:18 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 16:32:18 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.RulesSource;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;

/**
 * @author Keith Donald
 */
public class ValidatingFormModel extends DefaultFormModel implements
        AspectAccessStrategy {
    private RulesSource rulesSource;
    
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

    protected void onNewFormValueModel(String domainObjectProperty,
            final ValueModel formValueModel) {
        final BeanPropertyExpression exp = rulesSource.getRules(
                getDomainObjectClass(), domainObjectProperty);
        if (exp != null) {
            formValueModel.addValueListener(new ValueListener() {
                public void valueChanged() {
                    if (exp.test(ValidatingFormModel.this)) {
                        constraintSatisfied(exp, formValueModel);
                    }
                    else {
                        constraintViolated(exp, formValueModel);
                    }
                }
            });
        }
    }

    protected void constraintSatisfied(BeanPropertyExpression exp,
            ValueModel formValueModel) {
        if (logger.isDebugEnabled()) {
            logger.debug("Value constraint " + exp
                    + " [satisfied] for value model " + formValueModel);
        }
    }

    protected void constraintViolated(BeanPropertyExpression exp,
            ValueModel formValueModel) {
        if (logger.isDebugEnabled()) {
            logger.debug("Value constraint " + exp
                    + " [rejected] for value model " + formValueModel);
        }
    }

}