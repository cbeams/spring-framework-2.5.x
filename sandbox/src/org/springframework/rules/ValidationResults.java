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
package org.springframework.rules;

import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.rules.predicates.beans.BeanPropertiesExpression;
import org.springframework.rules.predicates.beans.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.beans.ParameterizedBeanPropertyExpression;
import org.springframework.util.ClassUtils;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;
import org.springframework.validation.Errors;

/**
 * @author Keith Donald
 */
public class ValidationResults implements Visitor {
    private static final Log logger = LogFactory
            .getLog(ValidationResults.class);

    private ReflectiveVisitorSupport visitorSupport = new ReflectiveVisitorSupport();
    private Object bean;
    private String propertyName;
    private Errors errors;
    private Stack levels = new Stack();
    GetProperty getProperty;

    public ValidationResults(Object bean, Errors errors) {
        this.bean = bean;
        this.errors = errors;
        this.getProperty = new GetProperty(bean);
    }

    public Errors collectResults(BeanPropertyExpression rootExpression) {
        Boolean result = (Boolean)visitorSupport.invokeVisit(this, rootExpression);
        if (logger.isDebugEnabled()) {
            logger.debug("Final validation result: " + result);
        }
        return errors;
    }

    boolean visit(BeanPropertiesExpression constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating bean properties expression [" + constraint
                    + "]...");
        }
        boolean result = applyAnyNegation(constraint.test(bean));
        if (!result) {
            String errorCode = getErrorCode(constraint.getPredicate());
            Object[] errorArgs = getArgs(constraint.getPropertyName(), constraint
                    .getOtherPropertyName());
            String defaultMessage = getDefaultMessage(constraint.getPropertyName(),
                    constraint.getPredicate(), constraint.getOtherPropertyName());
            errors.rejectValue(constraint.getPropertyName(), errorCode, errorArgs,
                    defaultMessage);
        }
        levels.push(constraint);
        if (logger.isDebugEnabled()) {
            logger.debug("Constraint [" + constraint + "] "  
                    + (result ? "passed" : "failed"));
        }
        return result;
    }
    

    Boolean visit(CompoundBeanPropertyExpression rule) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating compound bean property expression [" + rule
                    + "]...");
        }
        return (Boolean)visitorSupport.invokeVisit(this, rule.getPredicate());
    }

    boolean visit(ParameterizedBeanPropertyExpression constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating parameterized bean property expression ["
                    + constraint + "]...");
        }
        boolean result = constraint.test(bean);
        if (result) {
            String errorCode = getErrorCode(constraint.getPredicate());
            Object[] errorArgs = getArgs(constraint.getPropertyName(), constraint
                    .getParameter());
            String defaultMessage = getDefaultMessage(constraint.getPropertyName(),
                    constraint.getPredicate(), constraint.getParameter());
            errors.rejectValue(constraint.getPropertyName(), errorCode, errorArgs,
                    defaultMessage);
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
        this.propertyName = valueConstraint.getPropertyName();
        return (Boolean)visitorSupport.invokeVisit(this, valueConstraint.getPredicate());
    }

    boolean visit(UnaryAnd and) {
        levels.push(and);
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [and]...");
        }
        Iterator it = and.iterator();
        while (it.hasNext()) {
            boolean result = ((Boolean)visitorSupport.invokeVisit(
                    ValidationResults.this, it.next())).booleanValue();
            UnaryPredicate top = (UnaryPredicate)levels.pop();
            if (!result) {
                errors.rejectValue(propertyName, getErrorCode(top),
                        getArgs(top), getDefaultMessage(top));
                return false;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [and]...");
        }
        levels.pop();
        return true;
    }

    boolean visit(UnaryOr or) {
        levels.push(or);
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [or]...");
        }
        Iterator it = or.iterator();
        while (it.hasNext()) {
            boolean result = ((Boolean)visitorSupport.invokeVisit(
                    ValidationResults.this, it.next())).booleanValue();
            levels.pop();
            if (result) {
                return true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [or]...");
        }
        levels.pop();
        return false;
    }

    Boolean visit(UnaryNot not) {
        levels.push(not);
        if (logger.isDebugEnabled()) {
            logger.debug("Starting [not]...");
        }
        Boolean result = (Boolean)visitorSupport.invokeVisit(this, not
                .getPredicate());
        if (logger.isDebugEnabled()) {
            logger.debug("Finished [not]...");
        }
        levels.pop();
        return result;
    }

    boolean visit(UnaryPredicate constraint) {
        boolean result = constraint.test(getProperty.evaluate(propertyName));
        result = applyAnyNegation(result);
        if (logger.isDebugEnabled()) {
            logger.debug("Constraint [" + constraint + "] "  
                    + (result ? "passed" : "failed"));
        }
        levels.push(constraint);
        return result;
    }

    private boolean applyAnyNegation(boolean result) {
        boolean negated = (levels.peek() instanceof UnaryNot);
        if (logger.isDebugEnabled()) {
            if (negated) {
                logger.debug("[negate result]");
            }
        }
        return negated ? !result : result;
    }

    private String getErrorCode(BinaryPredicate predicate) {
        return ClassUtils.getShortNameAsProperty(predicate.getClass());
    }

    private Object[] getArgs(Object arg1, Object arg2) {
        return new Object[] { arg1, arg2 };
    }

    private String getDefaultMessage(Object arg1, BinaryPredicate predicate,
            Object arg2) {
        return arg1 + " must be " + predicate.toString() + " " + arg2;
    }

    private String getErrorCode(UnaryPredicate predicate) {
        return ClassUtils.getShortNameAsProperty(predicate.getClass());
    }

    private Object[] getArgs(UnaryPredicate predicate) {
        return new Object[] { propertyName };
    }

    private String getDefaultMessage(UnaryPredicate predicate) {
        return propertyName + " is " + predicate.toString();
    }

}