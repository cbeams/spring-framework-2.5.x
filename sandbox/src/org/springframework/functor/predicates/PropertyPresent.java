/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.functor.predicates;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.functor.UnaryPredicate;
import org.springframework.util.Assert;

public class PropertyPresent implements UnaryPredicate {
    private String propertyName;

    public PropertyPresent(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public boolean evaluate(Object bean) {
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        Object value = wrapper.getPropertyValue(propertyName);
        return Required.instance().evaluate(value);
    }

}