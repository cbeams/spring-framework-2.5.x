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
package org.springframework.jmx.assemblers.metadata;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.management.IntrospectionException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jmx.AbstractModelMBeanInfoAssembler;
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.assemblers.metadata.exceptions.InvalidMetadataException;
import org.springframework.jmx.exceptions.MBeanAssemblyException;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * @author Rob Harrop
 */
public class MetadataModelMBeanInfoAssembler extends
        AbstractModelMBeanInfoAssembler {

    /**
     * Attributes implementation. Default is Commons Attributes
     */
    private Attributes attributes = new CommonsAttributes();

    /**
     * Set the <tt>Attributes</tt> implementation.
     * 
     * @param attributes
     */
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    protected ModelMBeanAttributeInfo[] getAttributeInfo(Object bean) {
        Class beanClass = bean.getClass();
        BeanWrapper bw = new BeanWrapperImpl(bean);

        PropertyDescriptor[] props = bw.getPropertyDescriptors();
        ModelMBeanAttributeInfo[] info = new ModelMBeanAttributeInfo[props.length];
        int attrCount = 0;

        try {
            for (int x = 0; x < props.length; x++) {

                Method getter = checkForAttribute(props[x].getReadMethod(), ManagedAttribute.class);
                Method setter = checkForAttribute(props[x].getWriteMethod(), ManagedAttribute.class);

                if ((getter != null) || (setter != null)) {
                    // if both getter and setter are null
                    // then this does not need exposing
                    ModelMBeanAttributeInfo inf = new ModelMBeanAttributeInfo(
                            props[x].getName(), props[x].getDisplayName(),
                            getter, setter);

                    info[attrCount++] = inf;
                }

            }
        } catch (IntrospectionException ex) {
            throw new MBeanAssemblyException(
                    "Unable to attribute info. Check that you have specified valid metadata",
                    ex);
        }

        // create a final array to hold the attributes
        ModelMBeanAttributeInfo[] result = JmxUtils.shrink(info, attrCount);

        // release the temp array
        info = null;

        return result;
    }

    /**
     * Checks to see if a method has the specified attribute defined. If so then
     * the method is returned back to the caller, otherwise null is returned.
     * 
     * @param method
     * @return
     */
    private Method checkForAttribute(Method method, Class attributeClass) {
        if (method == null)
            return null;
        
        Collection attrs = attributes.getAttributes(method, attributeClass);

        if (attrs.isEmpty()) {
            return null;
        } else if (attrs.size() == 1) {
            return method;
        } else {
            throw new InvalidMetadataException(
                    "A getter/setter should only contain one " + attributeClass.getName());
        }
    }

    protected ModelMBeanConstructorInfo[] getConstructorInfo(Object bean) {
        return new ModelMBeanConstructorInfo[] {};
    }

    protected String getDescription(Object bean) {
        Collection attrs = attributes.getAttributes(bean.getClass(),
                ManagedResource.class);

        if (attrs.isEmpty()) {
            // use empty description
            return "";
        } else if (attrs.size() == 1) {
            ManagedResource mr = (ManagedResource) attrs.iterator().next();
            return mr.getDescription();
        } else {
            // too many ManagedResource attrs
            throw new InvalidMetadataException(
                    "Only one ManagedResource attribute is allowed");
        }
    }

    protected ModelMBeanNotificationInfo[] getNotificationInfo(Object bean) {
        return new ModelMBeanNotificationInfo[] {};
    }

    protected ModelMBeanOperationInfo[] getOperationInfo(Object bean) {
        
        Method[] methods = bean.getClass().getDeclaredMethods();
        ModelMBeanOperationInfo[] info = new ModelMBeanOperationInfo[methods.length];
        int attrCount = 0;
        
        for(int x = 0; x < methods.length; x++) {
            if(!JmxUtils.isProperty(methods[x])) {
                Collection attrs = attributes.getAttributes(methods[x], ManagedOperation.class);
                
                if(attrs.isEmpty()) {
                    continue;
                } else if (attrs.size() == 1) {
                    ManagedOperation mo = (ManagedOperation)attrs.iterator().next();
                    
                    info[attrCount++] = new ModelMBeanOperationInfo(mo.getDescription(), methods[x]);
                 
                } else {
                    throw new InvalidMetadataException("Only one ManagedOperation attribute must be specified per operation");
                }
                
            }
        }
        
        ModelMBeanOperationInfo[] result = JmxUtils.shrink(info, attrCount);
        return result;
    }
}