/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

import java.util.Stack;

import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.predicates.BeanPropertiesExpression;
import org.springframework.rules.predicates.BeanPropertyExpression;
import org.springframework.rules.predicates.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.ParameterizedBeanPropertyExpression;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.util.ClassUtils;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;
import org.springframework.validation.Errors;

/**
 * @author Keith Donald
 */
public class ValidationResults implements Visitor {
    private ReflectiveVisitorSupport visitorSupport =
        new ReflectiveVisitorSupport();
    private Object bean;
    private Errors errors;
    private String propertyName;
    private Stack levels = new Stack();

    public ValidationResults(Object bean, Errors errors) {
        this.bean = bean;
        this.errors = errors;
    }

    public void visit(BeanPropertiesExpression rule) {
        if (!rule.test(bean)) {
            String errorCode = getErrorCode(rule.getPredicate());
            Object[] errorArgs =
                getArgs(rule.getPropertyName(), rule.getOtherPropertyName());
            String defaultMessage =
                getDefaultMessage(
                    rule.getPropertyName(),
                    rule.getPredicate(),
                    rule.getOtherPropertyName());
            errors.rejectValue(
                rule.getPropertyName(),
                errorCode,
                errorArgs,
                defaultMessage);
        }
    }

    private String getErrorCode(BinaryPredicate predicate) {
        return ClassUtils.getShortNameAsProperty(predicate.getClass());
    }

    private Object[] getArgs(Object arg1, Object arg2) {
        return new Object[] { arg1, arg2 };
    }

    private String getDefaultMessage(
        Object arg1,
        BinaryPredicate predicate,
        Object arg2) {
        return arg1 + " must be " + predicate.toString() + " " + arg2;
    }

    public void visit(ParameterizedBeanPropertyExpression rule) {
        if (!rule.test(bean)) {
            String errorCode = getErrorCode(rule.getPredicate());
            Object[] errorArgs =
                getArgs(rule.getPropertyName(), rule.getParameter());
            String defaultMessage =
                getDefaultMessage(
                    rule.getPropertyName(),
                    rule.getPredicate(),
                    rule.getParameter());
            errors.rejectValue(
                rule.getPropertyName(),
                errorCode,
                errorArgs,
                defaultMessage);
        }
    }

    public void visit(BeanPropertyValueConstraint valueConstraint) {
        this.propertyName = valueConstraint.getPropertyName();
        visitorSupport.invokeVisit(this, valueConstraint.getPredicate());
    }

    public boolean visit(UnaryPredicate predicate) {
        levels.push(predicate);
        return predicate.test(
            GetProperty.instance().evaluate(bean, propertyName));
    }

    public void visit(UnaryAnd and) {
        levels.push(and);
        Algorithms.forEach(and.iterator(), new UnaryProcedure() {
            public void run(Object predicate) {
                boolean result =
                    ((Boolean)visitorSupport
                        .invokeVisit(ValidationResults.this, predicate))
                        .booleanValue();
                UnaryPredicate top = (UnaryPredicate)levels.pop();
                boolean negated = (levels.peek() instanceof UnaryNot);
                result = negated ? !result: result;
                if (!result) {
                    errors.rejectValue(
                        propertyName,
                        getErrorCode(top),
                        getArgs(top),
                        getDefaultMessage(top));
                }
            }
        });
        levels.pop();
    }

    public String getErrorCode(UnaryPredicate predicate) {
        return ClassUtils.getShortNameAsProperty(predicate.getClass());
    }

    public Object[] getArgs(UnaryPredicate predicate) {
        return new Object[] { propertyName };
    }

    public String getDefaultMessage(UnaryPredicate predicate) {
        return propertyName + " is " + predicate.toString();
    }

    public void visit(UnaryNot not) {
        levels.push(not);
        visitorSupport.invokeVisit(this, not.getPredicate());
        levels.pop();
    }

    public void collectResults(BeanPropertyExpression rootExpression) {
        visitorSupport.invokeVisit(this, rootExpression);
    }
}
