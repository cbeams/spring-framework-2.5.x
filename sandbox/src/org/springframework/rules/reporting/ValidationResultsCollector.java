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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.UnaryFunction;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryFunctionResultConstraint;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.util.ToStringBuilder;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * @author Keith Donald
 */
public class ValidationResultsCollector implements Visitor {
    private static final Log logger = LogFactory
            .getLog(ValidationResultsCollector.class);
    protected ReflectiveVisitorSupport visitorSupport = new ReflectiveVisitorSupport();
    private boolean collectAllErrors;
    private ValidationResultsBuilder resultsBuilder;
    private ValidationResults results;
    private Object argument;

    public ValidationResultsCollector() {
    }

    public ValidationResults collect(final Object argument,
            final UnaryPredicate constraint) {
        this.resultsBuilder = new ValidationResultsBuilder() {
            public void constraintSatisfied() {
                results = new ValueValidationResults(argument);
            }

            public void constraintViolated(UnaryPredicate constraint) {
                results = new ValueValidationResults(argument, constraint);
            }
        };
        this.argument = argument;
        visitorSupport.invokeVisit(this, constraint);
        return results;
    }

    public void setCollectAllErrors(boolean collectAllErrors) {
        this.collectAllErrors = collectAllErrors;
    }

    protected void setResultsBuilder(ValidationResultsBuilder resultsBuilder) {
        this.resultsBuilder = resultsBuilder;
    }

    protected void setArgument(Object argument) {
        this.argument = argument;
    }

    boolean visit(UnaryAnd and) {
        resultsBuilder.pushAnd();
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [and]...");
        }
        boolean result = true;
        Iterator it = and.iterator();
        while (it.hasNext()) {
            boolean test = ((Boolean)visitorSupport.invokeVisit(
                    ValidationResultsCollector.this, it.next())).booleanValue();
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
            boolean result = ((Boolean)visitorSupport.invokeVisit(
                    ValidationResultsCollector.this, it.next())).booleanValue();
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
        Boolean result = (Boolean)visitorSupport.invokeVisit(this, not
                .getPredicate());
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [not]...");
        }
        resultsBuilder.pop(result.booleanValue());
        return result;
    }

    Boolean visit(UnaryFunctionResultConstraint ofConstraint) {
        UnaryFunction f = ofConstraint.getFunction();
        this.argument = f.evaluate(argument);
        return (Boolean)visitorSupport.invokeVisit(this, ofConstraint.getPredicate());
    }

    boolean visit(UnaryPredicate constraint) {
        boolean result = constraint.test(argument);
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

    protected boolean applyAnyNegation(boolean result) {
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
        return new ToStringBuilder(this).append("collectAllErrors",
                collectAllErrors).append("validationResultsBuilder",
                resultsBuilder).toString();
    }

}