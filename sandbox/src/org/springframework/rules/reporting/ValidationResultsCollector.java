/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.rules.reporting;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.Algorithms;
import org.springframework.rules.Rules;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.UnaryProcedure;
import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.rules.predicates.beans.BeanPropertiesExpression;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.predicates.beans.BeanPropertyValueConstraint;
import org
    .springframework
    .rules
    .predicates
    .beans
    .ParameterizedBeanPropertyExpression;
import org.springframework.util.ToStringBuilder;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * @author Keith Donald
 */
public class ValidationResultsCollector implements Visitor {
    private static final Log logger =
        LogFactory.getLog(ValidationResultsCollector.class);
    private ReflectiveVisitorSupport visitorSupport =
        new ReflectiveVisitorSupport();
    private Object bean;
    private GetProperty getProperty;
    private boolean collectAllErrors;
    private ValidationResultsBuilder resultsBuilder;

    public ValidationResultsCollector(Object bean) {
        this.bean = bean;
        this.resultsBuilder = new ValidationResultsBuilder(bean);
        this.getProperty = new GetProperty(bean);
    }

    public void setCollectAllErrors(boolean collectAllErrors) {
        this.collectAllErrors = collectAllErrors;
    }

    public ValidationResults collectResults(Rules rules) {
        Algorithms.forEach(rules.iterator(), new UnaryProcedure() {
            public void run(Object beanPropertyConstraint) {
                collectPropertyResults(
                    (BeanPropertyExpression)beanPropertyConstraint);
            }
        });
        return resultsBuilder;
    }

    public PropertyResults collectPropertyResults(BeanPropertyExpression rootExpression) {
        resultsBuilder.setPropertyName(rootExpression.getPropertyName());
        boolean result =
            ((Boolean)visitorSupport.invokeVisit(this, rootExpression)).booleanValue();
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
            logger.debug(
                "Validating compound bean property expression ["
                    + rule
                    + "]...");
        }
        return (Boolean)visitorSupport.invokeVisit(this, rule.getPredicate());
    }

    boolean visit(BeanPropertiesExpression constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Validating bean properties expression ["
                    + constraint
                    + "]...");
        }
        return testBeanPropertyExpression(constraint);
    }

    boolean visit(ParameterizedBeanPropertyExpression constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Validating parameterized bean property expression ["
                    + constraint
                    + "]...");
        }
        return testBeanPropertyExpression(constraint);
    }

    private boolean testBeanPropertyExpression(BeanPropertyExpression constraint) {
        boolean result = constraint.test(bean);
        result = applyAnyNegation(result);
        if (!result) {
            resultsBuilder.push(constraint);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Constraint ["
                    + constraint
                    + "] "
                    + (result ? "passed" : "failed"));
        }
        return result;
    }

    Boolean visit(BeanPropertyValueConstraint valueConstraint) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Validating property value constraint ["
                    + valueConstraint
                    + "]...");
        }
        return (Boolean)visitorSupport.invokeVisit(
            this,
            valueConstraint.getPredicate());
    }

    boolean visit(UnaryAnd and) {
        resultsBuilder.pushAnd();
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [and]...");
        }
        boolean result = true;
        Iterator it = and.iterator();
        while (it.hasNext()) {
            boolean test =
                ((Boolean)visitorSupport
                    .invokeVisit(ValidationResultsCollector.this, it.next()))
                    .booleanValue();
            if (!test) {
                if (!collectAllErrors) {
                    resultsBuilder.pop(false);
                    return false;
                } else {
                    if (result) {
                        result = false;
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [and]...");
        }
        resultsBuilder.pop(result);
        return result;
    }

    boolean visit(UnaryOr or) {
        resultsBuilder.pushOr();
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [or]...");
        }
        Iterator it = or.iterator();
        while (it.hasNext()) {
            boolean result =
                ((Boolean)visitorSupport
                    .invokeVisit(ValidationResultsCollector.this, it.next()))
                    .booleanValue();
            if (result) {
                resultsBuilder.pop(result);
                return true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [or]...");
        }
        resultsBuilder.pop(false);
        return false;
    }

    Boolean visit(UnaryNot not) {
        resultsBuilder.pushNot();
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [not]...");
        }
        Boolean result =
            (Boolean)visitorSupport.invokeVisit(this, not.getPredicate());
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [not]...");
        }
        resultsBuilder.pop(result.booleanValue());
        return result;
    }

    boolean visit(UnaryPredicate constraint) {
        Object propertyValue =
            getProperty.evaluate(resultsBuilder.getPropertyName());
        boolean result = constraint.test(propertyValue);
        result = applyAnyNegation(result);
        if (!result) {
            resultsBuilder.push(constraint);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Constraint ["
                    + constraint
                    + "] "
                    + (result ? "passed" : "failed"));
        }
        return result;
    }

    private boolean applyAnyNegation(boolean result) {
        boolean negated = resultsBuilder.peek() instanceof UnaryNot;
        if (logger.isDebugEnabled()) {
            if (negated) {
                logger.debug("[negate result]");
            } else {
                logger.debug("[no negation]");
            }
        }
        return negated ? !result : result;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("bean", bean)
            .append("collectAllErrors", collectAllErrors)
            .append("validationResultsBuilder", resultsBuilder)
            .toString();
    }

}