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
package org.springframework.rules.functions;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.rules.UnaryFunction;

/**
 * Binary function that gets a bean property.
 * 
 * @author Keith Donald
 */
public class GetProperty implements UnaryFunction {
    private BeanWrapper beanWrapper;

    public GetProperty(Object bean) {
        this.beanWrapper = new BeanWrapperImpl(bean);
    }

    /**
     * Returns a bean's property value.
     * 
     * @see org.springframework.rules.BinaryFunction#evaluate(java.lang.Object,
     *      java.lang.Object)
     */
    public Object evaluate(Object propertyName) {
        return beanWrapper.getPropertyValue((String)propertyName);
    }

    public String toString() {
        return "getProperty(" + beanWrapper + ")";
    }
}