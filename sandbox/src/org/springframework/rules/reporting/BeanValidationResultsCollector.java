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
package org.springframework.rules.reporting;

import org.springframework.rules.Constraint;
import org.springframework.rules.Rules;
import org.springframework.rules.constraint.CompoundBeanPropertyExpression;
import org.springframework.rules.constraint.bean.BeanPropertiesConstraint;
import org.springframework.rules.constraint.bean.BeanPropertyConstraint;
import org.springframework.rules.constraint.bean.BeanPropertyValueConstraint;
import org.springframework.rules.constraint.bean.ParameterizedBeanPropertyConstraint;
import org.springframework.rules.support.Algorithms;
import org.springframework.rules.support.ClosureWithoutResult;
import org.springframework.util.Assert;
import org.springframework.util.visitor.Visitor;

/**
 * @author Keith Donald
 */
public class BeanValidationResultsCollector extends ValidationResultsCollector
        implements Visitor {
    private Object bean;

    private BeanValidationResultsBuilder resultsBuilder;

    public BeanValidationResultsCollector(Object bean) {
        super();
        setBean(bean);
    }

    public void setBean(Object bean) {
        Assert.notNull(bean);
        this.bean = bean;
    }

    private void setResultsBuilder(BeanValidationResultsBuilder builder) {
        super.setResultsBuilder(builder);
        this.resultsBuilder = builder;
    }

    public BeanValidationResults collectResults(Rules rules) {
        Assert.notNull(rules);
        setResultsBuilder(new BeanValidationResultsBuilder(bean));
        Algorithms.instance().forEach(rules.iterator(),
                new ClosureWithoutResult() {
                    public void doCall(Object beanPropertyConstraint) {
                        collectPropertyResultsInternal((BeanPropertyConstraint)beanPropertyConstraint);
                    }
                });
        return resultsBuilder;
    }

    public PropertyResults collectPropertyResults(
            BeanPropertyConstraint propertyRootExpression) {
        Assert.notNull(propertyRootExpression);
        setResultsBuilder(new BeanValidationResultsBuilder(this.bean));
        return collectPropertyResultsInternal(propertyRootExpression);
    }

    private PropertyResults collectPropertyResultsInternal(
            BeanPropertyConstraint rootExpression) {
        resultsBuilder.setCurrentBeanPropertyExpression(rootExpression);
        setArgument(resultsBuilder.getCurrentPropertyValue());
        boolean result = ((Boolean)visitorSupport.invokeVisit(this,
                rootExpression)).booleanValue();
        if (logger.isDebugEnabled()) {
            logger.debug("Final validation result: " + result);
        }
        if (!result) {
            return resultsBuilder.getResults(rootExpression.getPropertyName());
        }
        else {
            return null;
        }
    }

    Boolean visit(CompoundBeanPropertyExpression rule) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating compound bean property expression ["
                    + rule + "]...");
        }
        return (Boolean)visitorSupport.invokeVisit(this, rule.getPredicate());
    }

    boolean visit(BeanPropertiesConstraint constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating bean properties expression [" + constraint
                    + "]...");
        }
        return testBeanPropertyExpression(constraint);
    }

    boolean visit(ParameterizedBeanPropertyConstraint constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating parameterized bean property expression ["
                    + constraint + "]...");
        }
        return testBeanPropertyExpression(constraint);
    }

    private boolean testBeanPropertyExpression(BeanPropertyConstraint constraint) {
        boolean result = constraint.test(bean);
        result = applyAnyNegation(result);
        if (!result) {
            resultsBuilder.push(constraint);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Constraint [" + constraint + "] "
                    + (result ? "passed" : "failed"));
        }
        return result;
    }

    Boolean visit(BeanPropertyValueConstraint valueConstraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating property value constraint ["
                    + valueConstraint + "]...");
        }
        return (Boolean)visitorSupport.invokeVisit(this, valueConstraint
                .getPredicate());
    }

    boolean visit(Constraint constraint) {
        return super.visit(constraint);
    }

}