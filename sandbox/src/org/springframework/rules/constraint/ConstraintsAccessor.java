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
package org.springframework.rules.constraint;

import java.util.Set;

import org.springframework.rules.closure.BinaryConstraint;
import org.springframework.rules.constraint.Like.LikeType;
import org.springframework.rules.constraint.property.PropertyConstraint;
import org.springframework.rules.factory.Constraints;
import org.springframework.util.closure.Closure;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.support.AlgorithmsAccessor;

/**
 * A convenience constraints factory accessor for easy subclassing.
 *
 * @author Keith Donald
 */
public class ConstraintsAccessor extends AlgorithmsAccessor {

	protected Constraints getConstraints() {
		return Constraints.instance();
	}

	public Constraint bind(BinaryConstraint constraint, Object parameter) {
		return getConstraints().bind(constraint, parameter);
	}

	public Constraint bind(BinaryConstraint constraint, int parameter) {
		return getConstraints().bind(constraint, parameter);
	}

	public Constraint bind(BinaryConstraint constraint, float parameter) {
		return getConstraints().bind(constraint, parameter);
	}

	public Constraint bind(BinaryConstraint constraint, double parameter) {
		return getConstraints().bind(constraint, parameter);
	}

	public Constraint bind(BinaryConstraint constraint, boolean parameter) {
		return getConstraints().bind(constraint, parameter);
	}

	public Constraint testResultOf(Closure closure, Constraint constraint) {
		return getConstraints().testResultOf(closure, constraint);
	}

	public Constraint and(Constraint constraint1, Constraint constraint2) {
		return getConstraints().and(constraint1, constraint2);
	}

	public Constraint all(Constraint[] predicates) {
		return getConstraints().all(predicates);
	}

	public And conjunction() {
		return getConstraints().conjunction();
	}

	public Constraint or(Constraint constraint1, Constraint constraint2) {
		return getConstraints().or(constraint1, constraint2);
	}

	public Constraint any(Constraint[] constraints) {
		return getConstraints().any(constraints);
	}

	public Constraint not(Constraint constraint) {
		return getConstraints().not(constraint);
	}

	public Or disjunction() {
		return getConstraints().disjunction();
	}

	public Constraint inGroup(Set group) {
		return getConstraints().inGroup(group);
	}

	public Constraint inGroup(Object[] group) {
		return getConstraints().inGroup(group);
	}

	public PropertyConstraint inGroup(String propertyName, Object[] group) {
		return getConstraints().inGroup(propertyName, group);
	}

	public Constraint like(String encodedLikeString) {
		return getConstraints().like(encodedLikeString);
	}

	public PropertyConstraint like(String property, LikeType likeType, String value) {
		return getConstraints().like(property, likeType, value);
	}

	public Constraint required() {
		return getConstraints().required();
	}

	public PropertyConstraint required(String property) {
		return getConstraints().required(property);
	}

	public Constraint maxLength(int maxLength) {
		return getConstraints().maxLength(maxLength);
	}

	public Constraint minLength(int minLength) {
		return getConstraints().minLength(minLength);
	}

	public Constraint regexp(String regexp) {
		return getConstraints().regexp(regexp);
	}

	public Constraint regexp(String regexp, String constraintType) {
		return getConstraints().regexp(regexp, constraintType);
	}

	public Constraint method(Object target, String methodName, String constraintType) {
		return getConstraints().method(target, methodName, constraintType);
	}

	public PropertyConstraint value(String propertyName, Constraint valueConstraint) {
		return getConstraints().value(propertyName, valueConstraint);
	}

	public PropertyConstraint all(String propertyName, Constraint[] constraints) {
		return getConstraints().all(propertyName, constraints);
	}

	public PropertyConstraint any(String propertyName, Constraint[] constraints) {
		return getConstraints().any(propertyName, constraints);
	}

	public PropertyConstraint not(PropertyConstraint constraint) {
		return getConstraints().not(constraint);
	}

	public PropertyConstraint eq(String propertyName, Object propertyValue) {
		return getConstraints().eq(propertyName, propertyValue);
	}

	public PropertyConstraint gt(String propertyName, Object propertyValue) {
		return getConstraints().gt(propertyName, propertyValue);
	}

	public PropertyConstraint gte(String propertyName, Object propertyValue) {
		return getConstraints().gte(propertyName, propertyValue);
	}

	public PropertyConstraint lt(String propertyName, Object propertyValue) {
		return getConstraints().lt(propertyName, propertyValue);
	}

	public PropertyConstraint lte(String propertyName, Object propertyValue) {
		return getConstraints().lte(propertyName, propertyValue);
	}

	public PropertyConstraint eqProperty(String propertyName, String otherPropertyName) {
		return getConstraints().eqProperty(propertyName, otherPropertyName);
	}

	public PropertyConstraint gtProperty(String propertyName, String otherPropertyName) {
		return getConstraints().gtProperty(propertyName, otherPropertyName);
	}

	public PropertyConstraint gteProperty(String propertyName, String otherPropertyName) {
		return getConstraints().gteProperty(propertyName, otherPropertyName);
	}

	public PropertyConstraint ltProperty(String propertyName, String otherPropertyName) {
		return getConstraints().ltProperty(propertyName, otherPropertyName);
	}

	public PropertyConstraint lteProperty(String propertyName, String otherPropertyName) {
		return getConstraints().lteProperty(propertyName, otherPropertyName);
	}

	public PropertyConstraint inRange(String propertyName, Comparable min, Comparable max) {
		return getConstraints().inRange(propertyName, min, max);
	}

	public PropertyConstraint inRangeProperties(String propertyName, String minPropertyName, String maxPropertyName) {
		return getConstraints().inRangeProperties(propertyName, minPropertyName, maxPropertyName);
	}

	public PropertyConstraint unique(String propertyName) {
		return getConstraints().unique(propertyName);
	}

}