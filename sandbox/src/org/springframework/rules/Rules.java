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

import java.util.Iterator;

import org.springframework.rules.predicates.BeanPropertyExpression;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.util.Assert;
import org.springframework.util.Cache;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A factory for creating rules.
 * 
 * @author Keith Donald
 */
public class Rules implements UnaryPredicate, Validator {
    private Class beanClass;
    private Cache propertyRules = new Cache() {
        public Object create(Object key) {
            return new UnaryAnd();
        }
    };

    private Rules(Class beanClass) {
        Assert.notNull(beanClass);
        this.beanClass = beanClass;
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

    public Rules add(BeanPropertyExpression expression) {
        UnaryAnd and =
            (UnaryAnd)propertyRules.get(expression.getPropertyName());
        and.add(expression);
        return this;
    }

    /**
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object bean) {
        for (Iterator i = propertyRules.values(); i.hasNext();) {
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
        Algorithms.forEach(propertyRules.entries(), new UnaryProcedure() {
            public void run(Object predicate) {
                BeanPropertyExpression rule = (BeanPropertyExpression)predicate;
                ValidationResults results = new ValidationResults(bean, errors);
                results.collectResults(rule);
            }
        });
    }

}