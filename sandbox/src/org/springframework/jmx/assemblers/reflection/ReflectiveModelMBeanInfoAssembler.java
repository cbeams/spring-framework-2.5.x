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
package org.springframework.jmx.assemblers.reflection;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;

import org.springframework.jmx.assemblers.AbstractReflectionBasedModelMBeanInfoAssembler;

/**
 * @author Rob Harrop
 */
public class ReflectiveModelMBeanInfoAssembler extends
        AbstractReflectionBasedModelMBeanInfoAssembler {

    
    protected String getDescription(Object bean) {
        return "";
    }

    protected boolean includeReadAttribute(Method method) {
        return true;
    }


    protected boolean includeWriteAttribute(Method method) {
        return true;
    }

    protected boolean includeOperation(Method method) {
        return true;
    }

    protected String getOperationDescription(Method method) {
        return method.getName();
    }

    protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getDisplayName();
    }
    
    protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, Object bean) {
       // no-op
    }
    
    
    protected void populateAttributeDescriptor(Descriptor descriptor,
            Method getter, Method setter) {
        // no-op
    }
    
    
    protected void populateOperationDescriptor(Descriptor descriptor,
            Method method) {
        // no-op
    }
}