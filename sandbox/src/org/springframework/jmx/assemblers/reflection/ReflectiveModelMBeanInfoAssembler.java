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

import javax.management.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jmx.AbstractModelMBeanInfoAssembler;
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.exceptions.MBeanAssemblyException;

/**
 * @author Rob Harrop
 */
public class ReflectiveModelMBeanInfoAssembler extends
        AbstractModelMBeanInfoAssembler {

    /**
     * Gets the attributes (properties) of the supplied bean. All properties are
     * exposed to the JMX server using this assembler.
     */
    protected ModelMBeanAttributeInfo[] getAttributeInfo(Object bean) {

        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        PropertyDescriptor[] properties = wrapper.getPropertyDescriptors();

        ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[properties.length - 1];
        int attrCount = 0;

        try {
            for (int x = 0; x < properties.length; x++) {
                PropertyDescriptor pd = properties[x];

                // do not expose getClass() as an attribute!
                if ("class".equals(pd.getName())) {
                    continue;
                }

                attributes[attrCount++] = new ModelMBeanAttributeInfo(
                        pd.getName(), pd.getDisplayName(), pd.getReadMethod(),
                        pd.getWriteMethod());
            }
        } catch (IntrospectionException ex) {
            throw new MBeanAssemblyException(
                    "Unable to assemble MBean due to introspection error.", ex);
        }

        return attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jmx.AbstractModelMBeanInfoAssembler#getConstructorInfo(java.lang.Object)
     */
    protected ModelMBeanConstructorInfo[] getConstructorInfo(Object bean) {
        // TODO Auto-generated method stub
        return new ModelMBeanConstructorInfo[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jmx.AbstractModelMBeanInfoAssembler#getNotificationInfo(java.lang.Object)
     */
    protected ModelMBeanNotificationInfo[] getNotificationInfo(Object bean) {
        // TODO Auto-generated method stub
        return new ModelMBeanNotificationInfo[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jmx.AbstractModelMBeanInfoAssembler#getOperationInfo(java.lang.Object)
     */
    protected ModelMBeanOperationInfo[] getOperationInfo(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        ModelMBeanOperationInfo[] info = new ModelMBeanOperationInfo[methods.length];
        int attrCount = 0;

        for (int x = 0; x < methods.length; x++) {
            if (!JmxUtils.isProperty(methods[x])) {

                ModelMBeanOperationInfo inf = new ModelMBeanOperationInfo(
                        methods[x].getName(), methods[x]);
                info[attrCount++] = inf;
            }
        }

        ModelMBeanOperationInfo[] result = JmxUtils.shrink(info, attrCount);
        return result;
    }

    protected String getDescription(Object bean) {
        return "";
    }
}