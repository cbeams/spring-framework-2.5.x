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
package org.springframework.rules.functions;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.rules.BinaryFunction;

/**
 * Binary function that gets a bean property.
 * 
 * @author Keith Donald
 */
public class GetProperty implements BinaryFunction {

    private static final GetProperty INSTANCE = new GetProperty();

    /**
     * Returns a bean's property value.
     * 
     * @see org.springframework.rules.BinaryFunction#evaluate(java.lang.Object,
     *      java.lang.Object)
     */
    public Object evaluate(Object bean, Object propertyName) {
        return new BeanWrapperImpl(bean).getPropertyValue((String)propertyName);
    }

    /**
     * Returns a shared GetProperty instance.
     * 
     * @return the shared instance.
     */
    public static BinaryFunction instance() {
        return INSTANCE;
    }

}
