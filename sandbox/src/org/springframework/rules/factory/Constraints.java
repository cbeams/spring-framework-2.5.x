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

import org.springframework.binding.MutablePropertyAccessStrategy;
import org.springframework.binding.support.BeanPropertyAccessStrategy;
import org.springframework.rules.BinaryConstraint;
import org.springframework.rules.Closure;
import org.springframework.rules.Constraint;
import org.springframework.rules.constraint.And;
import org.springframework.rules.constraint.ClosureResultConstraint;
import org.springframework.rules.constraint.EqualTo;
import org.springframework.rules.constraint.GreaterThan;
import org.springframework.rules.constraint.GreaterThanEqualTo;
import org.springframework.rules.constraint.InGroup;
import org.springframework.rules.constraint.LessThan;
import org.springframework.rules.constraint.LessThanEqualTo;
import org.springframework.rules.constraint.Like;
import org.springframework.rules.constraint.MethodInvokingConstraint;
import org.springframework.rules.constraint.Not;
import org.springframework.rules.constraint.Or;
import org.springframework.rules.constraint.ParameterizedBinaryConstraint;
import org.springframework.rules.constraint.Range;
import org.springframework.rules.constraint.RegexpConstraint;
import org.springframework.rules.constraint.RelationalOperator;
import org.springframework.rules.constraint.Required;
import org.springframework.rules.constraint.StringLengthConstraint;
import org.springframework.rules.constraint.Like.LikeType;
import org.springframework.rules.constraint.property.CompoundPropertyConstraint;
import org.springframework.rules.constraint.property.PropertiesConstraint;
import org.springframework.rules.constraint.property.NegatedPropertyConstraint;
import org.springframework.rules.constraint.property.ParameterizedPropertyConstraint;
import org.springframework.rules.constraint.property.PropertyConstraint;
import org.springframework.rules.constraint.property.PropertyValueConstraint;

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
	public Constraint bind(BinaryConstraint predicate, Object parameter) {
		return new ParameterizedBinaryConstraint(predicate, parameter);
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
	public Constraint bind(BinaryConstraint predicate, int parameter) {
		return new ParameterizedBinaryConstraint(predicate, parameter);
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
	public Constraint bind(BinaryConstraint predicate, float parameter) {
		return new ParameterizedBinaryConstraint(predicate, parameter);
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
	public Constraint bind(BinaryConstraint predicate, double parameter) {
		return new ParameterizedBinaryConstraint(predicate, parameter);
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
	public Constraint bind(BinaryConstraint predicate, boolean parameter) {
		return new ParameterizedBinaryConstraint(predicate, parameter);
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
	public Constraint testResultOf(Closure function, Constraint constraint) {
		return new ClosureResultConstraint(function, constraint);
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
	public Constraint and(Constraint predicate1, Constraint predicate2) {
		return new And(predicate1, predicate2);
	}

	/**
	 * Return the conjunction (all constraint) for all predicates.
	 *
	 * @param predicates
	 *            the predicates
	 * @return The compound AND predicate
	 */
	public Constraint all(Constraint[] predicates) {
		return new And(predicates);
	}

	/**
	 * Returns a new, empty unary conjunction prototype, capable of composing
	 * individual predicates where 'ALL' must test true.
	 *
	 * @return the UnaryAnd
	 */
	public And conjunction() {
		return new And();
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
	public Constraint or(Constraint predicate1, Constraint predicate2) {
		return new Or(predicate1, predicate2);
	}

	/**
	 * Return the disjjunction (any constraint) for all predicates.
	 *
	 * @param predicates
	 *            the predicates
	 * @return The compound AND predicate
	 */
	public Constraint any(Constraint[] predicates) {
		return new Or(predicates);
	}

	/**
	 * Negate the specified predicate.
	 *
	 * @param predicate
	 *            The predicate to negate
	 * @return The negated predicate.
	 */
	public Constraint not(Constraint predicate) {
		if (!(predicate instanceof Not)) {
			return new Not(predicate);
		}
		else {
			return ((Not) predicate).getPredicate();
		}
	}

	/**
	 * Returns a new, empty unary disjunction prototype, capable of composing
	 * individual predicates where 'ANY' must test true.
	 *
	 * @return the UnaryAnd
	 */
	public Or disjunction() {
		return new Or();
	}

	/**
	 * Returns a 'in' group (or set) predicate.
	 *
	 * @param group
	 *            the group items
	 * @return The InGroup predicate
	 */
	public Constraint inGroup(Set group) {
		return new InGroup(group);
	}

	/**
	 * Returns a 'in' group (or set) predicate.
	 *
	 * @param group
	 *            the group items
	 * @return The InGroup predicate.
	 */
	public Constraint inGroup(Object[] group) {
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
	public PropertyConstraint inGroup(String propertyName, Object[] group) {
		return value(propertyName, new InGroup(group));
	}

	/**
	 * Returns a 'like' predicate.
	 *
	 * @param encodedLikeString
	 *            the likeString
	 * @return The Like predicate.
	 */
	public Constraint like(String encodedLikeString) {
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
	public PropertyConstraint like(String property, LikeType likeType,
			String value) {
		return value(property, new Like(likeType, value));
	}

	/**
	 * Returns a required predicate.
	 *
	 * @return The required predicate instance.
	 */
	public Constraint required() {
		return Required.instance();
	}

	/**
	 * Returns a required bean property expression.
	 *
	 * @return The required predicate instance.
	 */
	public PropertyConstraint required(String property) {
		return value(property, required());
	}

	/**
	 * Returns a maxlength predicate.
	 *
	 * @param maxLength
	 *            The maximum length in characters.
	 * @return The configured maxlength predicate.
	 */
	public Constraint maxLength(int maxLength) {
		return new StringLengthConstraint(maxLength);
	}

	/**
	 * Returns a minlength predicate.
	 *
	 * @param minLength
	 *            The minimum length in characters.
	 * @return The configured minlength predicate.
	 */
	public Constraint minLength(int minLength) {
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
	public Constraint regexp(String regexp) {
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
	public Constraint regexp(String regexp, String type) {
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
	public Constraint method(Object target, String methodName,
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
	public PropertyConstraint value(String propertyName,
			Constraint valueConstraint) {
		return new PropertyValueConstraint(propertyName, valueConstraint);
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
	public PropertyConstraint all(String propertyName,
			Constraint[] constraints) {
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
	public PropertyConstraint any(String propertyName,
			Constraint[] constraints) {
		return value(propertyName, any(constraints));
	}

	/**
	 * Negate a bean property expression.
	 *
	 * @param e
	 *            the expression to negate
	 * @return The negated expression
	 */
	public PropertyConstraint not(PropertyConstraint e) {
		return new NegatedPropertyConstraint(e);
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
	public PropertyConstraint eq(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, EqualTo
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
	public PropertyConstraint gt(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName,
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
	public PropertyConstraint gte(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName,
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
	public PropertyConstraint lt(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, LessThan
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
	public PropertyConstraint lte(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName,
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
	public PropertyConstraint gtProperty(String propertyName,
			String otherPropertyName) {
		return new PropertiesConstraint(propertyName, GreaterThan
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
	public PropertyConstraint eqProperty(String propertyName,
			String otherPropertyName) {
		return new PropertiesConstraint(propertyName, EqualTo.instance(),
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
	public PropertyConstraint gteProperty(String propertyName,
			String otherPropertyName) {
		return new PropertiesConstraint(propertyName, GreaterThanEqualTo
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
	public PropertyConstraint ltProperty(String propertyName,
			String otherPropertyName) {
		return new PropertiesConstraint(propertyName, LessThan.instance(),
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
	public PropertyConstraint lteProperty(String propertyName,
			String otherPropertyName) {
		return new PropertiesConstraint(propertyName, LessThanEqualTo
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
	public PropertyConstraint inRange(String propertyName, Comparable min,
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
	public PropertyConstraint inRangeProperties(String propertyName,
			String minPropertyName, String maxPropertyName) {
		PropertiesConstraint min = new PropertiesConstraint(
				propertyName, GreaterThanEqualTo.instance(), minPropertyName);
		PropertiesConstraint max = new PropertiesConstraint(
				propertyName, LessThanEqualTo.instance(), maxPropertyName);
		return new CompoundPropertyConstraint(new And(min, max));
	}

	public PropertyConstraint unique(String propertyName) {
		return new UniqueValuePropertyConstraint(propertyName);
	}

	private static class UniqueValuePropertyConstraint implements
			PropertyConstraint {

		private String propertyName;

		private Map distinctTable;

		public UniqueValuePropertyConstraint(String propertyName) {
			this.propertyName = propertyName;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public boolean test(Object o) {
			Collection c = (Collection) o;
			distinctTable = new HashMap((int) (c.size() * .75));
			Iterator it = c.iterator();
			MutablePropertyAccessStrategy accessor = null;
			while (it.hasNext()) {
				Object bean = it.next();
				if (accessor == null) {
					accessor = createPropertyAccessStrategy(bean);
				}
				else {
					accessor.getDomainObjectHolder().setValue(bean);
				}
				Object value = accessor.getPropertyValue(propertyName);
				Integer hashCode;
				if (value == null) {
					hashCode = new Integer(0);
				}
				else {
					hashCode = new Integer(value.hashCode());
				}
				if (distinctTable.containsKey(hashCode)) {
					return false;
				}
				else {
					distinctTable.put(hashCode, value);
				}
			}
			return true;
		}

		protected MutablePropertyAccessStrategy createPropertyAccessStrategy(
				Object o) {
			return new BeanPropertyAccessStrategy(o);
		}

	}

}