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

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class PropertyConstraints {
    private String propertyName;

    public PropertyConstraints(String propertyName) {
        setPropertyName(propertyName);
    }

    public void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public BeanPropertyExpression eq(Object value) {
        return Constraints.eq(propertyName, value);
    }

    public BeanPropertyExpression eqProperty(String otherPropertyName) {
        return Constraints.eqProperty(propertyName, otherPropertyName);
    }
    
    public BeanPropertyExpression lt(Object value) {
        return Constraints.lt(propertyName, value);
    }

    public BeanPropertyExpression ltProperty(String otherPropertyName) {
        return Constraints.ltProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression lte(Object value) {
        return Constraints.lte(propertyName, value);
    }

    public BeanPropertyExpression lteProperty(String otherPropertyName) {
        return Constraints.lteProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression gt(Object value) {
        return Constraints.gte(propertyName, value);
    }

    public BeanPropertyExpression gtProperty(String otherPropertyName) {
        return Constraints.gtProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression gte(Object value) {
        return Constraints.gte(propertyName, value);
    }

    public BeanPropertyExpression gteProperty(String otherPropertyName) {
        return Constraints.gteProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression inRange(Comparable min, Comparable max) {
        return Constraints.inRange(propertyName, min, max);
    }

    public BeanPropertyExpression inRangeProperties(String minProperty, String maxProperty) {
        return Constraints.inRangeProperties(propertyName, minProperty, maxProperty);
    }
    
}
