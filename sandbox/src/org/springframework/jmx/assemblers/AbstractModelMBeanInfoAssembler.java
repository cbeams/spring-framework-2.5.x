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
package org.springframework.jmx.assemblers;

import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;


/**
 * @author Rob Harrop
 */
public abstract class AbstractModelMBeanInfoAssembler implements
        ModelMBeanInfoAssembler {

    public ModelMBeanInfo getMBeanInfo(Object bean) {

        return new ModelMBeanInfoSupport(bean.getClass().getName(), getDescription(bean),
                getAttributeInfo(bean), getConstructorInfo(bean),
                getOperationInfo(bean), getNotificationInfo(bean));
    }

    protected abstract String getDescription(Object bean);
    
    protected abstract ModelMBeanAttributeInfo[] getAttributeInfo(Object bean);

    protected abstract ModelMBeanConstructorInfo[] getConstructorInfo(
            Object bean);

    protected abstract ModelMBeanOperationInfo[] getOperationInfo(Object bean);

    protected abstract ModelMBeanNotificationInfo[] getNotificationInfo(
            Object bean);

}