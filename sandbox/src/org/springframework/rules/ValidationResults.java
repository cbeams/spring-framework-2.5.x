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

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
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
    private static final Log logger = LogFactory.getLog(ValidationResults.class);
    
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
        visitorSupport.invokeVisit(this, rootExpression);
        return errors;
    }

    void visit(BeanPropertiesExpression rule) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating bean property expression [" + rule + "]...");
        }
        if (!rule.test(bean)) {
            String errorCode = getErrorCode(rule.getPredicate());
            Object[] errorArgs = getArgs(rule.getPropertyName(), rule
                    .getOtherPropertyName());
            String defaultMessage = getDefaultMessage(rule.getPropertyName(),
                    rule.getPredicate(), rule.getOtherPropertyName());
            errors.rejectValue(rule.getPropertyName(), errorCode, errorArgs,
                    defaultMessage);
        }
    }
    
    void visit(CompoundBeanPropertyExpression rule) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating 1bean property expression [" + rule + "]...");
        }
    }

    void visit(ParameterizedBeanPropertyExpression rule) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating parameterized bean property expression [" + rule + "]...");
        }
        if (!rule.test(bean)) {
            String errorCode = getErrorCode(rule.getPredicate());
            Object[] errorArgs = getArgs(rule.getPropertyName(), rule
                    .getParameter());
            String defaultMessage = getDefaultMessage(rule.getPropertyName(),
                    rule.getPredicate(), rule.getParameter());
            errors.rejectValue(rule.getPropertyName(), errorCode, errorArgs,
                    defaultMessage);
        }
    }

    void visit(BeanPropertyValueConstraint valueConstraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating property value constraint [" + valueConstraint + "]...");
        }
        this.propertyName = valueConstraint.getPropertyName();
        visitorSupport.invokeVisit(this, valueConstraint.getPredicate());
    }

    void visit(UnaryAnd and) {
        levels.push(and);
        if (logger.isDebugEnabled()) {
            logger.debug("Starting <and>...");
        }
        Algorithms.forEach(and.iterator(), new UnaryProcedure() {
            public void run(Object predicate) {
                boolean result = ((Boolean)visitorSupport.invokeVisit(
                        ValidationResults.this, predicate)).booleanValue();
                UnaryPredicate top = (UnaryPredicate)levels.pop();
                if (!result) {
                    errors.rejectValue(propertyName, getErrorCode(top),
                            getArgs(top), getDefaultMessage(top));
                }
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("Finished <and>...");
        }
        levels.pop();
    }

    boolean visit(UnaryPredicate constraint) {
        boolean negated = (levels.peek() instanceof UnaryNot);
        if (logger.isDebugEnabled()) {
            if (negated) {
                logger.debug("Result will be negated");
            }
        }
        levels.push(constraint);
        boolean result = constraint.test(getProperty.evaluate(propertyName));
        if (logger.isDebugEnabled()) {
            logger.debug("Single constraint [" + constraint + "] returned " + result);
        }
        return negated ? !result : result;
    }

    Boolean visit(UnaryNot not) {
        levels.push(not);
        if (logger.isDebugEnabled()) {
            logger.debug("Starting <not>...");
        }
        Boolean result = (Boolean)visitorSupport.invokeVisit(this, not.getPredicate());
        if (logger.isDebugEnabled()) {
            logger.debug("Finished <not>...");
        }
        levels.pop();
        return result;
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