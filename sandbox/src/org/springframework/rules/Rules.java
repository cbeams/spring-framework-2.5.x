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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.constraint.And;
import org.springframework.rules.constraint.CompoundConstraint;
import org.springframework.rules.constraint.property.CompoundPropertyConstraint;
import org.springframework.rules.constraint.property.PropertyConstraint;
import org.springframework.rules.constraint.property.PropertyValueConstraint;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.Constraint;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A factory for creating rules.
 * 
 * @author Keith Donald
 */
public class Rules implements Constraint, Validator {
	private static final Log logger = LogFactory.getLog(Rules.class);

	private Class beanClass;

	private Map propertyRules = new HashMap();

	public Rules() {

	}

	public Rules(Class beanClass) {
		setBeanClass(beanClass);
	}

	public Class getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class beanClass) {
		Assert.notNull(beanClass, "The beanClass property is required");
		this.beanClass = beanClass;
	}

	public void setPropertyRules(Map propertyRules) {
		for (Iterator i = propertyRules.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			String propertyName = (String)entry.getKey();
			Object val = entry.getValue();
			PropertyConstraint e;
			if (val instanceof List) {
				And and = new And();
				and.addAll((List)val);
				e = new PropertyValueConstraint(propertyName, and);
			}
			else {
				Constraint p = (Constraint)val;
				if (p instanceof CompoundConstraint) {
					e = new CompoundPropertyConstraint((CompoundConstraint)p);
				}
				else if (p instanceof PropertyConstraint) {
					e = (PropertyConstraint)p;
				}
				else {
					e = new PropertyValueConstraint(propertyName, p);
				}
			}
			internalSetRules(e);
		}
	}

	private void internalSetRules(PropertyConstraint e) {
		And and = new And();
		and.add(e);
		if (logger.isDebugEnabled()) {
			logger.debug("Configuring rules for property '" + e.getPropertyName() + "', rules -> [" + e + "]");
		}
		propertyRules.put(e.getPropertyName(), new CompoundPropertyConstraint(and));
	}

	public PropertyConstraint getRules(String property) {
		return (PropertyConstraint)propertyRules.get(property);
	}

	public Iterator iterator() {
		return propertyRules.values().iterator();
	}

	/**
	 * Factory method that creates a rules instance for a given java bean type.
	 * 
	 * @param propertyName
	 * @return A rule for a given property.
	 */
	public static Rules createRules(Class beanClass) {
		return new Rules(beanClass);
	}

	/**
	 * Adds the provided bean property expression (constraint) to the list of
	 * constraints for the constrained property.
	 * 
	 * @param expression
	 *            the bean property expression
	 * @return this, to support chaining.
	 */
	public Rules add(PropertyConstraint expression) {
		CompoundPropertyConstraint and = (CompoundPropertyConstraint)propertyRules.get(expression.getPropertyName());
		if (and == null) {
			internalSetRules(expression);
		}
		else {
			and.add(expression);
		}
		return this;
	}

	/**
	 * Adds a value constraint for the specified property.
	 * 
	 * @param propertyName
	 *            The property name.
	 * @param valueConstraint
	 *            The value constraint.
	 */
	public void add(String propertyName, Constraint valueConstraint) {
		add(new PropertyValueConstraint(propertyName, valueConstraint));
	}

	/**
	 * Adds the provided compound predicate, composed of BeanPropertyExpression
	 * objects, as a bean property constraint.
	 * 
	 * @param compoundPredicate
	 */
	public void add(Constraint compoundPredicate) {
		Assert.isTrue(compoundPredicate instanceof CompoundConstraint,
				"Argument must be a compound predicate composed of BeanPropertyExpression objects.");
		add(new CompoundPropertyConstraint((CompoundConstraint)compoundPredicate));
	}

	public boolean test(Object bean) {
		for (Iterator i = propertyRules.values().iterator(); i.hasNext();) {
			Constraint predicate = (Constraint)i.next();
			if (!predicate.test(bean)) {
				return false;
			}
		}
		return true;
	}

	public boolean supports(Class clazz) {
		return clazz.equals(this.beanClass);
	}

	public void validate(final Object bean, final Errors errors) {

	}

	public String toString() {
		return new ToStringCreator(this).append("beanClass", beanClass).append("propertyRules", propertyRules).toString();
	}

}