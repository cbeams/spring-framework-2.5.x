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
package org.springframework.rules.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.rules.BinaryFunction;
import org.springframework.rules.BinaryPredicate;
import org.springframework.rules.RelationalOperator;
import org.springframework.rules.UnaryFunction;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.predicates.BinaryFunctionResultConstraint;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.EqualTo;
import org.springframework.rules.predicates.GreaterThan;
import org.springframework.rules.predicates.GreaterThanEqualTo;
import org.springframework.rules.predicates.InGroup;
import org.springframework.rules.predicates.LessThan;
import org.springframework.rules.predicates.LessThanEqualTo;
import org.springframework.rules.predicates.Like;
import org.springframework.rules.predicates.MethodInvokingConstraint;
import org.springframework.rules.predicates.ParameterizedBinaryPredicate;
import org.springframework.rules.predicates.Range;
import org.springframework.rules.predicates.RegexpConstraint;
import org.springframework.rules.predicates.Required;
import org.springframework.rules.predicates.StringLengthConstraint;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryFunctionResultConstraint;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.rules.predicates.Like.LikeType;
import org.springframework.rules.predicates.beans.BeanPropertiesExpression;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.predicates.beans.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.beans.NegatedBeanPropertyExpression;
import org.springframework.rules.predicates.beans.ParameterizedBeanPropertyExpression;
import org.springframework.rules.values.BeanPropertyAccessStrategy;
import org.springframework.rules.values.MutablePropertyAccessStrategy;

/**
 * A factory for easing the construction and composition of predicates.
 * 
 * @author Keith Donald
 */
public class Constraints {

    private static final Constraints INSTANCE = new Constraints();

    public Constraints() {

    }

    public static Constraints instance() {
        return INSTANCE;
    }

    /**
     * Bind the specified parameter to the second argument of the
     * <code>BinaryPredicate</code>. The result is a
     * <code>UnaryPredicate</code> which will test a single variable argument
     * against the constant parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the parameter value (constant)
     * @return The unary predicate
     */
    public UnaryPredicate bind(BinaryPredicate predicate, Object parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Bind the specified <code>int</code> parameter to the second argument of
     * the <code>BinaryPredicate</code>. The result is a
     * <code>UnaryPredicate</code> which will test a single variable argument
     * against the constant <code>int</code> parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the <code>int</code> parameter value (constant)
     * @return The unary predicate
     */
    public UnaryPredicate bind(BinaryPredicate predicate, int parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Bind the specified <code>float</code> parameter to the second argument
     * of the <code>BinaryPredicate</code>. The result is a
     * <code>UnaryPredicate</code> which will test a single variable argument
     * against the constant <code>float</code> parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the <code>float</code> parameter value (constant)
     * @return The unary predicate
     */
    public UnaryPredicate bind(BinaryPredicate predicate, float parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Bind the specified <code>double</code> parameter to the second argument
     * of the <code>BinaryPredicate</code>. The result is a
     * <code>UnaryPredicate</code> which will test a single variable argument
     * against the constant <code>double</code> parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the <code>double</code> parameter value (constant)
     * @return The unary predicate
     */
    public UnaryPredicate bind(BinaryPredicate predicate, double parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Bind the specified <code>boolean</code> parameter to the second
     * argument of the <code>BinaryPredicate</code>. The result is a
     * <code>UnaryPredicate</code> which will test a single variable argument
     * against the constant <code>boolean</code> parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the <code>boolean</code> parameter value (constant)
     * @return The unary predicate
     */
    public UnaryPredicate bind(BinaryPredicate predicate, boolean parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Attaches a predicate that tests the result returned by evaluating the
     * specified unary function. This effectively attaches a constraint on the
     * function return value.
     * 
     * @param function
     *            the function
     * @param constraint
     *            the predicate to test the function result
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public UnaryPredicate testResultOf(UnaryFunction function,
            UnaryPredicate constraint) {
        return new UnaryFunctionResultConstraint(function, constraint);
    }

    /**
     * Attaches a predicate that tests the result returned by evaluating the
     * specified binary function. This effectively attaches a constraint on the
     * function return value.
     * 
     * @param function
     *            the function
     * @param constraint
     *            the predicate to test the function result
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public BinaryPredicate testResultOf(BinaryFunction function,
            UnaryPredicate constraint) {
        return new BinaryFunctionResultConstraint(function, constraint);
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
    public UnaryPredicate and(UnaryPredicate predicate1,
            UnaryPredicate predicate2) {
        return new UnaryAnd(predicate1, predicate2);
    }

    /**
     * Return the conjunction (all constraint) for all predicates.
     * 
     * @param predicates
     *            the predicates
     * @return The compound AND predicate
     */
    public UnaryPredicate all(UnaryPredicate[] predicates) {
        return new UnaryAnd(predicates);
    }

    /**
     * Returns a new, empty unary conjunction prototype, capable of composing
     * individual predicates where 'ALL' must test true.
     * 
     * @return the UnaryAnd
     */
    public UnaryAnd conjunction() {
        return new UnaryAnd();
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
    public UnaryPredicate or(UnaryPredicate predicate1,
            UnaryPredicate predicate2) {
        return new UnaryOr(predicate1, predicate2);
    }

    /**
     * Return the disjjunction (any constraint) for all predicates.
     * 
     * @param predicates
     *            the predicates
     * @return The compound AND predicate
     */
    public UnaryPredicate any(UnaryPredicate[] predicates) {
        return new UnaryOr(predicates);
    }

    /**
     * Negate the specified predicate.
     * 
     * @param predicate
     *            The predicate to negate
     * @return The negated predicate.
     */
    public UnaryPredicate not(UnaryPredicate predicate) {
        if (!(predicate instanceof UnaryNot)) {
            return new UnaryNot(predicate);
        }
        else {
            return ((UnaryNot)predicate).getPredicate();
        }
    }

    /**
     * Returns a new, empty unary disjunction prototype, capable of composing
     * individual predicates where 'ANY' must test true.
     * 
     * @return the UnaryAnd
     */
    public UnaryOr disjunction() {
        return new UnaryOr();
    }

    /**
     * Returns a 'in' group (or set) predicate.
     * 
     * @param group
     *            the group items
     * @return The InGroup predicate
     */
    public UnaryPredicate inGroup(Set group) {
        return new InGroup(group);
    }

    /**
     * Returns a 'in' group (or set) predicate.
     * 
     * @param group
     *            the group items
     * @return The InGroup predicate.
     */
    public UnaryPredicate inGroup(Object[] group) {
        return new InGroup(group);
    }

    /**
     * Returns a 'in' group (or set) predicate appled to the provided property.
     * 
     * @param propertyName
     *            the property
     * @param group
     *            the group items
     * @return The InGroup predicate.
     */
    public BeanPropertyExpression inGroup(String propertyName, Object[] group) {
        return value(propertyName, new InGroup(group));
    }

    /**
     * Returns a 'like' predicate.
     * 
     * @param encodedLikeString
     *            the likeString
     * @return The Like predicate.
     */
    public UnaryPredicate like(String encodedLikeString) {
        return new Like(encodedLikeString);
    }

    /**
     * Return a 'like' predicate applied as a value constraint to the provided
     * property.
     * 
     * @param property
     *            The property to constrain
     * @param likeType
     *            The like type
     * @param value
     *            The like string value to match
     * @return The Like predicate
     */
    public BeanPropertyExpression like(String property, LikeType likeType,
            String value) {
        return value(property, new Like(likeType, value));
    }

    /**
     * Returns a required predicate.
     * 
     * @return The required predicate instance.
     */
    public UnaryPredicate required() {
        return Required.instance();
    }

    /**
     * Returns a required bean property expression.
     * 
     * @return The required predicate instance.
     */
    public BeanPropertyExpression required(String property) {
        return value(property, required());
    }

    /**
     * Returns a maxlength predicate.
     * 
     * @param maxLength
     *            The maximum length in characters.
     * @return The configured maxlength predicate.
     */
    public UnaryPredicate maxLength(int maxLength) {
        return new StringLengthConstraint(maxLength);
    }

    /**
     * Returns a minlength predicate.
     * 
     * @param minLength
     *            The minimum length in characters.
     * @return The configured minlength predicate.
     */
    public UnaryPredicate minLength(int minLength) {
        return new StringLengthConstraint(
                RelationalOperator.GREATER_THAN_EQUAL_TO, minLength);
    }

    /**
     * Creates a constraint backed by a regular expression.
     * 
     * @param regexp
     *            The regular expression string.
     * @return The constraint.
     */
    public UnaryPredicate regexp(String regexp) {
        return new RegexpConstraint(regexp);
    }

    /**
     * Creates a constraint backed by a regular expression, with a type for
     * reporting.
     * 
     * @param regexp
     *            The regular expression string.
     * @return The constraint.
     */
    public UnaryPredicate regexp(String regexp, String type) {
        RegexpConstraint c = new RegexpConstraint(regexp);
        c.setType(type);
        return c;
    }

    /**
     * Returns a constraint whose test is determined by a boolean method on a
     * target object.
     * 
     * @param targetObject
     *            The targetObject
     * @param methodName
     *            The method name
     * @return The predicate.
     */
    public UnaryPredicate method(Object target, String methodName,
            String constraintType) {
        return new MethodInvokingConstraint(target, methodName, constraintType);
    }

    /**
     * Attach a value constraint for the provided bean property.
     * 
     * @param propertyName
     *            the bean property name
     * @param valueConstraint
     *            the value constraint
     * @return The bean property expression that tests the constraint
     */
    public BeanPropertyExpression value(String propertyName,
            UnaryPredicate valueConstraint) {
        return new BeanPropertyValueConstraint(propertyName, valueConstraint);
    }

    /**
     * Apply an "all" value constraint to the provided bean property.
     * 
     * @param propertyName
     *            The bean property name
     * @param constraints
     *            The constraints that form a all conjunction
     * @return
     */
    public BeanPropertyExpression all(String propertyName,
            UnaryPredicate[] constraints) {
        return value(propertyName, all(constraints));
    }

    /**
     * Apply an "any" value constraint to the provided bean property.
     * 
     * @param propertyName
     *            The bean property name
     * @param constraints
     *            The constraints that form a all disjunction
     * @return
     */
    public BeanPropertyExpression any(String propertyName,
            UnaryPredicate[] constraints) {
        return value(propertyName, any(constraints));
    }

    /**
     * Negate a bean property expression.
     * 
     * @param e
     *            the expression to negate
     * @return The negated expression
     */
    public BeanPropertyExpression not(BeanPropertyExpression e) {
        return new NegatedBeanPropertyExpression(e);
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
    public BeanPropertyExpression eq(String propertyName, Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(propertyName, EqualTo
                .instance(), propertyValue);
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
    public BeanPropertyExpression gt(String propertyName, Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(propertyName,
                GreaterThan.instance(), propertyValue);
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
    public BeanPropertyExpression gte(String propertyName, Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(propertyName,
                GreaterThanEqualTo.instance(), propertyValue);
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
    public BeanPropertyExpression lt(String propertyName, Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(propertyName, LessThan
                .instance(), propertyValue);
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
    public BeanPropertyExpression lte(String propertyName, Object propertyValue) {
        return new ParameterizedBeanPropertyExpression(propertyName,
                LessThanEqualTo.instance(), propertyValue);
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
    public BeanPropertyExpression gtProperty(String propertyName,
            String otherPropertyName) {
        return new BeanPropertiesExpression(propertyName, GreaterThan
                .instance(), otherPropertyName);
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
    public BeanPropertyExpression eqProperty(String propertyName,
            String otherPropertyName) {
        return new BeanPropertiesExpression(propertyName, EqualTo.instance(),
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
    public BeanPropertyExpression gteProperty(String propertyName,
            String otherPropertyName) {
        return new BeanPropertiesExpression(propertyName, GreaterThanEqualTo
                .instance(), otherPropertyName);
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
    public BeanPropertyExpression ltProperty(String propertyName,
            String otherPropertyName) {
        return new BeanPropertiesExpression(propertyName, LessThan.instance(),
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
    public BeanPropertyExpression lteProperty(String propertyName,
            String otherPropertyName) {
        return new BeanPropertiesExpression(propertyName, LessThanEqualTo
                .instance(), otherPropertyName);
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
    public BeanPropertyExpression inRange(String propertyName, Comparable min,
            Comparable max) {
        Range range = new Range(min, max);
        return value(propertyName, range);
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
    public BeanPropertyExpression inRangeProperties(String propertyName,
            String minPropertyName, String maxPropertyName) {
        BeanPropertiesExpression min = new BeanPropertiesExpression(
                propertyName, GreaterThanEqualTo.instance(), minPropertyName);
        BeanPropertiesExpression max = new BeanPropertiesExpression(
                propertyName, LessThanEqualTo.instance(), maxPropertyName);
        return new CompoundBeanPropertyExpression(new UnaryAnd(min, max));
    }

    public BeanPropertyExpression unique(String propertyName) {
        return new UniquePropertyValueCollectionConstraint(propertyName);
    }

    private static class UniquePropertyValueCollectionConstraint implements
            BeanPropertyExpression {
        private String propertyName;

        private Map distinctTable;

        public UniquePropertyValueCollectionConstraint(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public boolean test(Object o) {
            Collection c = (Collection)o;
            distinctTable = new HashMap((int)(c.size() * .75));
            Iterator it = c.iterator();
            MutablePropertyAccessStrategy accessor = null;
            while (it.hasNext()) {
                Object bean = it.next();
                if (accessor == null) {
                    accessor = createPropertyAccessStrategy(bean);
                } else {
                    accessor.getDomainObjectHolder().set(bean);
                }
                Object value = accessor.getValue(propertyName);
                Integer hashCode = new Integer(value.hashCode());
                if (distinctTable.containsKey(hashCode)) {
                    return false;
                } else {
                    distinctTable.put(hashCode, value);
                }
            }
            return true;
        }
        
        protected MutablePropertyAccessStrategy createPropertyAccessStrategy(Object o) {
            return new BeanPropertyAccessStrategy(o);
        }


    }

}