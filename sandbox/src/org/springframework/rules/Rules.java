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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.predicates.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.CompoundUnaryPredicate;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A factory for creating rules.
 * 
 * @author Keith Donald
 */
public class Rules implements UnaryPredicate, Validator {
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
        Assert.notNull(beanClass);
        this.beanClass = beanClass;
    }

    public void setPropertyRules(Map propertyRules) {
        for (Iterator i = propertyRules.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            String propertyName = (String)entry.getKey();
            UnaryPredicate p = (UnaryPredicate)entry.getValue();
            BeanPropertyExpression e;
            if (p instanceof CompoundUnaryPredicate) {
                e = new CompoundBeanPropertyExpression(
                        (CompoundUnaryPredicate)p);
            } else if (p instanceof BeanPropertyExpression) {
                e = (BeanPropertyExpression)p;
            } else {
                e = new BeanPropertyValueConstraint(propertyName, p);
            }
            internalSetRules(e);
        }
    }

    private void internalSetRules(BeanPropertyExpression e) {
        UnaryAnd and = new UnaryAnd();
        and.add(e);
        if (logger.isDebugEnabled()) {
            logger.debug("Configuring rules for property '"
                    + e.getPropertyName() + "', rules -> [" + e + "]");
        }
        propertyRules.put(e.getPropertyName(), e);
    }

    public BeanPropertyExpression getRules(String property) {
        return (BeanPropertyExpression)propertyRules.get(property);
    }

    /**
     * Still a work in progress!
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
    public Rules add(BeanPropertyExpression expression) {
        UnaryAnd and = (UnaryAnd)propertyRules
                .get(expression.getPropertyName());
        if (and == null) {
            internalSetRules(expression);
        } else {
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
    public void add(String propertyName, UnaryPredicate valueConstraint) {
        add(new BeanPropertyValueConstraint(propertyName, valueConstraint));
    }

    /**
     * Adds the provided compound predicate, composed of BeanPropertyExpression
     * objects, as a bean property constraint.
     * 
     * @param compoundPredicate
     */
    public void add(UnaryPredicate compoundPredicate) {
        Assert
                .isTrue(
                        compoundPredicate instanceof CompoundUnaryPredicate,
                        "Argument must be a compound predicate composed of BeanPropertyExpression objects.");
        add(new CompoundBeanPropertyExpression(
                (CompoundUnaryPredicate)compoundPredicate));
    }

    /**
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        for (Iterator i = propertyRules.values().iterator(); i.hasNext();) {
            UnaryPredicate predicate = (UnaryPredicate)i.next();
            if (!predicate.test(bean)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    public boolean supports(Class clazz) {
        return clazz.equals(this.beanClass);
    }

    /**
     * @see org.springframework.validation.Validator#validate(java.lang.Object,
     *      org.springframework.validation.Errors)
     */
    public void validate(final Object bean, final Errors errors) {
        Algorithms.forEach(propertyRules.values(), new UnaryProcedure() {
            public void run(Object predicate) {
                BeanPropertyExpression rule = (BeanPropertyExpression)predicate;
                ValidationResults results = new ValidationResults(bean, errors);
                results.collectResults(rule);
            }
        });
    }

    public String toString() {
        return new ToStringBuilder(this).append("beanClass", beanClass).append(
                "propertyRules", propertyRules).toString();
    }

}