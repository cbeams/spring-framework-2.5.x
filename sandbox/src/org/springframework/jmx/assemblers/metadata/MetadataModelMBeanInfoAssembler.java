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

import javax.management.IntrospectionException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jmx.AbstractModelMBeanInfoAssembler;
import org.springframework.jmx.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.exceptions.MBeanAssemblyException;
import org.springframework.jmx.metadata.support.ManagedAttribute;
import org.springframework.jmx.metadata.support.ManagedOperation;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.metadata.support.MetadataReader;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * Implementation of <tt>ModelMBeanInfoAssembler</tt> that reads the
 * management interface information from source level metadata. Uses Spring's
 * metadata abstraction layer so that metadata can be read using any supported
 * implementation.
 * 
 * @author Rob Harrop
 */
public class MetadataModelMBeanInfoAssembler extends
        AbstractModelMBeanInfoAssembler implements AutodetectCapableModelMBeanInfoAssembler{

    /**
     * Attributes implementation. Default is Commons Attributes
     */
    private Attributes attributes = new CommonsAttributes();

    /**
     * Set the <tt>Attributes</tt> implementation.
     * 
     * @param attributes
     * @see org.springframework.metadata.Attributes
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

                Method getter = checkForManagedAttribute(props[x].getReadMethod());
                Method setter = checkForManagedAttribute(props[x].getWriteMethod());

                if ((getter != null) || (setter != null)) {
                    // if both getter and setter are null
                    // then this does not need exposing
                    ModelMBeanAttributeInfo inf = new ModelMBeanAttributeInfo(
                            props[x].getName(), 
                            getDescription(props[x]),
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
     * Creates a description for the attribute corresponding
     * to this property descriptor. Attempts to 
     * create the description using metadata from either the getter
     * or setter attributes, otherwise uses the property name.
     * @param pd
     * @return
     */
    private String getDescription(PropertyDescriptor pd) {
        
        ManagedAttribute getter = MetadataReader.getManagedAttribute(attributes, pd.getReadMethod());
        ManagedAttribute setter = MetadataReader.getManagedAttribute(attributes, pd.getWriteMethod());
        
        StringBuffer sb = new StringBuffer();
        
        if((getter.getDescription() != null) && (getter.getDescription().length() > 0)) {
            return getter.getDescription();
        } else if((setter.getDescription() != null) && (setter.getDescription().length() > 0)) {
            return setter.getDescription();
        } else {
            return pd.getDisplayName();
        }
    }
    
    /**
     * Checks to see if a method has a ManagedAttribute defined. If so then
     * the method is returned back to the caller, otherwise null is returned.
     * 
     * @param method
     * @return
     */
    private Method checkForManagedAttribute(Method method) {
        if (method == null)
            return null;

        ManagedAttribute ma = MetadataReader.getManagedAttribute(attributes, method);
        
        if(ma == null) {
            return null;
        } else {
            return method;
        }
    }

    protected ModelMBeanConstructorInfo[] getConstructorInfo(Object bean) {
        return new ModelMBeanConstructorInfo[] {};
    }

    protected String getDescription(Object bean) {
        ManagedResource mr = MetadataReader.getManagedResource(attributes,
                bean.getClass());

        if (mr == null) {
            return "";
        } else {
            return mr.getDescription();
        }
    }

    protected ModelMBeanNotificationInfo[] getNotificationInfo(Object bean) {
        return new ModelMBeanNotificationInfo[] {};
    }

    protected ModelMBeanOperationInfo[] getOperationInfo(Object bean) {

        Method[] methods = bean.getClass().getDeclaredMethods();
        ModelMBeanOperationInfo[] info = new ModelMBeanOperationInfo[methods.length];
        int attrCount = 0;

        for (int x = 0; x < methods.length; x++) {
            if (!JmxUtils.isProperty(methods[x])) {
                ManagedOperation mo = MetadataReader.getManagedOperation(
                        attributes, methods[x]);

                if (mo != null) {
                    info[attrCount++] = new ModelMBeanOperationInfo(
                            mo.getDescription(), methods[x]);
                }
            }
        }

        ModelMBeanOperationInfo[] result = JmxUtils.shrink(info, attrCount);
        return result;
    }

    /**
     * Used for auto detection of beans. Checks to see if
     * the bean's class has a ManagedResource attribute. If so
     * it will add it list of included beans
     */
    public boolean includeBean(String beanName, Object bean) {
        if(MetadataReader.getManagedResource(attributes, bean.getClass()) != null) {
            return true;
        } else {
            return false;
        }
    }
}