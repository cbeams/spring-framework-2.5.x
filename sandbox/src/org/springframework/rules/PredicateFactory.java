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
package org.springframework.rules;

import org.springframework.rules.predicates.BeanPropertiesExpression;
import org.springframework.rules.predicates.BeanPropertyExpression;
import org.springframework.rules.predicates.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.BinaryFunctionResultConstraint;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.EqualTo;
import org.springframework.rules.predicates.GreaterThan;
import org.springframework.rules.predicates.GreaterThanEqualTo;
import org.springframework.rules.predicates.LessThan;
import org.springframework.rules.predicates.LessThanEqualTo;
import org.springframework.rules.predicates.ParameterizedBeanPropertyExpression;
import org.springframework.rules.predicates.ParameterizedBinaryPredicate;
import org.springframework.rules.predicates.Range;
import org.springframework.rules.predicates.Required;
import org.springframework.rules.predicates.StringLengthConstraint;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryFunctionResultConstraint;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;

/**
 * A factory for easing the construction and composition of predicates.
 * 
 * @author Keith Donald
 */
public class PredicateFactory {

    // static utility class
    private PredicateFactory() {

    }

    /**
     * Bind the specified parameter to the second argument of a
     * BinaryPredicate, returning a UnaryPredicate which will test a single
     * variable argument against the constant parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the parameter value (constant)
     * @return The unary predicate
     */
    public static UnaryPredicate bind(
        BinaryPredicate predicate,
        Object parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Attaches a predicate that tests the result returned by evaluating the
     * specified unary function. This effectively attaches a constraint on the
     * function return value.
     * 
     * @param constraint
     *            the predicate to test the function result
     * @param function
     *            the function
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public static UnaryPredicate attachResultConstraint(
        UnaryPredicate constraint,
        UnaryFunction function) {
        return new UnaryFunctionResultConstraint(constraint, function);
    }

    /**
     * Attaches a predicate that tests the result returned by evaluating the
     * specified binary function. This effectively attaches a constraint on the
     * function return value.
     * 
     * @param constraint
     *            the predicate to test the function result
     * @param function
     *            the function
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public static BinaryPredicate attachResultConstraint(
        UnaryPredicate constraint,
        BinaryFunction function) {
        return new BinaryFunctionResultConstraint(constraint, function);
    }

    /**
     * Negate the specified predicate.
     * 
     * @param predicate
     *            The predicate to negate
     * @return The negated predicate.
     */
    public static UnaryPredicate negate(UnaryPredicate predicate) {
        if (predicate instanceof UnaryNot) {
            throw new IllegalArgumentException("Predicate is already negated");
        }
        return new UnaryNot(predicate);
    }

    /**
     * Returns a new, empty unary conjunction prototype, capable of composing
     * individual predicates where 'ALL' must test true.
     * 
     * @return the UnaryAnd
     */
    public static UnaryAnd conjunction() {
        return new UnaryAnd();
    }

    /**
     * Returns a new, empty unary disjunction prototype, capable of composing
     * individual predicates where 'ANY' must test true.
     * 
     * @return the UnaryAnd
     */
    public static UnaryOr disjunction() {
        return new UnaryOr();
    }

    /**
     * AND two predicates.
     * 
     * @param predicate1
     *            the first predicate
     * @param predicate2
     *            the second predicate
     * @return The compound AND predicate
     */
    public static UnaryPredicate and(
        UnaryPredicate predicate1,
        UnaryPredicate predicate2) {
        return new UnaryAnd(predicate1, predicate2);
    }

    /**
     * OR two predicates.
     * 
     * @param predicate1
     *            the first predicate
     * @param predicate2
     *            the second predicate
     * @return The compound OR predicate
     */
    public static UnaryPredicate or(
        UnaryPredicate predicate1,
        UnaryPredicate predicate2) {
        return new UnaryOr(predicate1, predicate2);
    }

    /**
     * Apply a "equal to" constraint to a bean property.
     * 
     * @param propertyName
     *            The first property
     * @param propertyValue
     *            The constraint value
     * @return The predicate
     */
    public static BeanPropertyExpression equals(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            EqualTo.instance(),
            propertyValue);
    }

    /**
     * Apply a "greater than" constraint to a bean property.
     * 
     * @param propertyName
     *            The first property
     * @param propertyValue
     *            The constraint value
     * @return The predicate
     */
    public static BeanPropertyExpression greaterThan(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            GreaterThan.instance(),
            propertyValue);
    }

    /**
     * Apply a "greater than equal to" constraint to a bean property.
     * 
     * @param propertyName
     *            The first property
     * @param propertyValue
     *            The constraint value
     * @return The predicate
     */
    public static BeanPropertyExpression greaterThanEqualTo(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            GreaterThanEqualTo.instance(),
            propertyValue);
    }

    /**
     * Apply a "less than" constraint to a bean property.
     * 
     * @param propertyName
     *            The first property
     * @param propertyValue
     *            The constraint value
     * @return The predicate
     */
    public static BeanPropertyExpression lessThan(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            LessThan.instance(),
            propertyValue);
    }

    /**
     * Apply a "less than equal to" constraint to a bean property.
     * 
     * @param propertyName
     *            The first property
     * @param propertyValue
     *            The constraint value
     * @return The predicate
     */
    public static BeanPropertyExpression lessThanEqualTo(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            LessThanEqualTo.instance(),
            propertyValue);
    }

    /**
     * Apply a "greater than" constraint to two properties
     * 
     * @param propertyName
     *            The first property
     * @param otherPropertyName
     *            The other property
     * @return The predicate
     */
    public static BeanPropertyExpression greaterThanProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertiesExpression(
            propertyName,
            GreaterThan.instance(),
            otherPropertyName);
    }

    /**
     * Apply a "equal to" constraint to two bean properties.
     * 
     * @param propertyName
     *            The first property
     * @param otherPropertyName
     *            The other property
     * @return The predicate
     */
    public static BeanPropertyExpression equalsProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertiesExpression(
            propertyName,
            EqualTo.instance(),
            otherPropertyName);
    }

    /**
     * Apply a "greater than or equal to" constraint to two properties.
     * 
     * @param propertyName
     *            The first property
     * @param otherPropertyName
     *            The other property
     * @return The predicate
     */
    public static BeanPropertyExpression greaterThanEqualToProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertiesExpression(
            propertyName,
            GreaterThanEqualTo.instance(),
            otherPropertyName);
    }

    /**
     * Apply a "less than" constraint to two properties.
     * 
     * @param propertyName
     *            The first property
     * @param otherPropertyName
     *            The other property
     * @return The predicate
     */
    public static BeanPropertyExpression lessThanProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertiesExpression(
            propertyName,
            LessThan.instance(),
            otherPropertyName);
    }

    /**
     * Apply a "less than or equal to" constraint to two properties.
     * 
     * @param propertyName
     *            The first property
     * @param otherPropertyName
     *            The other property
     * @return The predicate
     */
    public static BeanPropertyExpression lessThanEqualToProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertiesExpression(
            propertyName,
            LessThanEqualTo.instance(),
            otherPropertyName);
    }

    /**
     * Apply a inclusive "range" constraint to a bean property.
     * 
     * @param propertyName
     *            the property with the range constraint.
     * @param min
     *            the low edge of the range
     * @param max
     *            the high edge of the range
     * @return The range predicate constraint
     */
    public static BeanPropertyExpression inRange(
        String propertyName,
        Comparable min,
        Comparable max) {
        Range range = new Range(min, max);
        return new BeanPropertyValueConstraint(propertyName, range);
    }

    /**
     * Apply a inclusive "range" constraint between two other properties to a
     * bean property.
     * 
     * @param propertyName
     *            the property with the range constraint.
     * @param minPropertyName
     *            the low edge of the range
     * @param maxPropertyName
     *            the high edge of the range
     * @return The range predicate constraint
     */
    public static BeanPropertyExpression inRangeProperty(
        String propertyName,
        String minPropertyName,
        String maxPropertyName) {
        BeanPropertiesExpression min =
            new BeanPropertiesExpression(
                propertyName,
                GreaterThanEqualTo.instance(),
                minPropertyName);
        BeanPropertiesExpression max =
            new BeanPropertiesExpression(
                propertyName,
                LessThanEqualTo.instance(),
                maxPropertyName);
        return new CompoundBeanPropertyExpression(and(min, max));
    }

    /**
     * Returns a required predicate.
     * 
     * @return The required predicate instance.
     */
    public static UnaryPredicate required() {
        return Required.instance();
    }

    /**
     * Returns a maxlength predicate.
     * 
     * @param maxLength
     *            The maximum length in characters.
     * @return The configured maxlength predicate.
     */
    public static UnaryPredicate maxLength(int maxLength) {
        return new StringLengthConstraint(maxLength);
    }

    /**
     * Returns a minlength predicate.
     * 
     * @param minLength
     *            The minimum length in characters.
     * @return The configured minlength predicate.
     */
    public static UnaryPredicate minLength(int minLength) {
        return new StringLengthConstraint(
            RelationalOperator.GREATER_THAN_EQUAL_TO,
            minLength);
    }

}