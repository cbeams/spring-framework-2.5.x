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
package org.springframework.functor;

import org.springframework.functor.functions.GetProperty;
import org.springframework.functor.predicates.BeanPropertyExpression;
import org.springframework.functor.predicates.BinaryFunctionResultTester;
import org.springframework.functor.predicates.EqualTo;
import org.springframework.functor.predicates.GreaterThan;
import org.springframework.functor.predicates.GreaterThanEqualTo;
import org.springframework.functor.predicates.LessThan;
import org.springframework.functor.predicates.LessThanEqualTo;
import org
    .springframework
    .functor
    .predicates
    .ParameterizedBeanPropertyExpression;
import org.springframework.functor.predicates.ParameterizedBinaryPredicate;
import org.springframework.functor.predicates.Range;
import org.springframework.functor.predicates.UnaryAnd;
import org.springframework.functor.predicates.UnaryFunctionResultTester;
import org.springframework.functor.predicates.UnaryNot;
import org.springframework.functor.predicates.UnaryOr;

/**
 * A factory for easing the construction of predicates.
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
     * specified unary function.
     * 
     * @param tester
     *            the predicate to test the function result
     * @param function
     *            the function
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public static UnaryPredicate attachResultTester(
        UnaryPredicate tester,
        UnaryFunction function) {
        return new UnaryFunctionResultTester(tester, function);
    }

    /**
     * Attaches a predicate that tests the result returned by evaluating the
     * specified binary function.
     * 
     * @param tester
     *            the predicate to test the function result
     * @param function
     *            the function
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public static BinaryPredicate attachResultTester(
        UnaryPredicate tester,
        BinaryFunction function) {
        return new BinaryFunctionResultTester(tester, function);
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
    public static UnaryPredicate equals(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            propertyValue,
            EqualTo.instance());
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
    public static UnaryPredicate greaterThan(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            propertyValue,
            GreaterThan.instance());
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
    public static UnaryPredicate greaterThanEqualTo(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            propertyValue,
            GreaterThanEqualTo.instance());
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
    public static UnaryPredicate lessThan(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            propertyValue,
            LessThan.instance());
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
    public static UnaryPredicate lessThanEqualTo(
        String propertyName,
        Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(
            propertyName,
            propertyValue,
            LessThanEqualTo.instance());
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
    public static UnaryPredicate greaterThanProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertyExpression(
            propertyName,
            otherPropertyName,
            GreaterThan.instance());
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
    public static UnaryPredicate equalsProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertyExpression(
            propertyName,
            otherPropertyName,
            EqualTo.instance());
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
    public static UnaryPredicate greaterThanEqualToProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertyExpression(
            propertyName,
            otherPropertyName,
            GreaterThanEqualTo.instance());
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
    public static UnaryPredicate lessThanProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertyExpression(
            propertyName,
            otherPropertyName,
            LessThan.instance());
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
    public static UnaryPredicate lessThanEqualToProperty(
        String propertyName,
        String otherPropertyName) {
        return new BeanPropertyExpression(
            propertyName,
            otherPropertyName,
            LessThanEqualTo.instance());
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
    public static UnaryPredicate inRange(
        String propertyName,
        Comparable min,
        Comparable max) {
        Range range = new Range(min, max);
        BinaryPredicate valueTester =
            attachResultTester(range, GetProperty.instance());
        return new ParameterizedBinaryPredicate(valueTester, propertyName);
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
    public static UnaryPredicate inRangeProperty(
        String propertyName,
        String minPropertyName,
        String maxPropertyName) {
        BeanPropertyExpression min =
            new BeanPropertyExpression(
                propertyName,
                minPropertyName,
                GreaterThanEqualTo.instance());
        BeanPropertyExpression max =
            new BeanPropertyExpression(
                propertyName,
                maxPropertyName,
                LessThanEqualTo.instance());
        return and(min, max);
    }

}