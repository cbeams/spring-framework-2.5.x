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

import org.springframework.rules.Algorithms;
import org.springframework.rules.Rules;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.UnaryProcedure;
import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.beans.BeanPropertiesExpression;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.predicates.beans.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.beans.ParameterizedBeanPropertyExpression;
import org.springframework.util.Assert;
import org.springframework.util.visitor.Visitor;

/**
 * @author Keith Donald
 */
public class BeanValidationResultsCollector extends ValidationResultsCollector
        implements Visitor {
    private Object bean;
    private GetProperty getProperty;
    private BeanValidationResultsBuilder resultsBuilder;

    public BeanValidationResultsCollector(Object bean) {
        super();
        this.bean = bean;
        this.getProperty = new GetProperty(bean);
    }

    public BeanValidationResults collectResults(Rules rules) {
        Assert.notNull(rules);
        setResultsBuilder(new BeanValidationResultsBuilder(bean));
        Algorithms.forEach(rules.iterator(), new UnaryProcedure() {
            public void run(Object beanPropertyConstraint) {
                collectPropertyResultsInternal((BeanPropertyExpression)beanPropertyConstraint);
            }
        });
        return resultsBuilder;
    }

    public void setResultsBuilder(BeanValidationResultsBuilder builder) {
        super.setResultsBuilder(builder);
        this.resultsBuilder = builder;
    }

    public PropertyResults collectPropertyResults(
            BeanPropertyExpression rootExpression) {
        Assert.notNull(rootExpression);
        setResultsBuilder(new BeanValidationResultsBuilder(bean));
        return collectPropertyResultsInternal(rootExpression);
    }

    private PropertyResults collectPropertyResultsInternal(
            BeanPropertyExpression rootExpression) {
        resultsBuilder.setPropertyName(rootExpression.getPropertyName());
        boolean result = ((Boolean)visitorSupport.invokeVisit(this,
                rootExpression)).booleanValue();
        if (logger.isDebugEnabled()) {
            logger.debug("Final validation result: " + result);
        }
        if (!result) {
            return resultsBuilder.getResults(rootExpression.getPropertyName());
        } else {
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

    boolean visit(BeanPropertiesExpression constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating bean properties expression [" + constraint
                    + "]...");
        }
        return testBeanPropertyExpression(constraint);
    }

    boolean visit(ParameterizedBeanPropertyExpression constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating parameterized bean property expression ["
                    + constraint + "]...");
        }
        return testBeanPropertyExpression(constraint);
    }

    Boolean visit(BeanPropertyValueConstraint valueConstraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating property value constraint ["
                    + valueConstraint + "]...");
        }
        return (Boolean)visitorSupport.invokeVisit(this, valueConstraint
                .getPredicate());
    }

    private boolean testBeanPropertyExpression(BeanPropertyExpression constraint) {
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

    boolean visit(UnaryPredicate constraint) {
        Object propertyValue = getProperty.evaluate(resultsBuilder
                .getPropertyName());
        setArgument(propertyValue);
        return super.visit(constraint);
    }

}