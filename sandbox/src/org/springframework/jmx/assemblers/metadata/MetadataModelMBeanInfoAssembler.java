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

import org.springframework.jmx.assemblers.AbstractReflectionBasedModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.metadata.support.ManagedAttribute;
import org.springframework.jmx.metadata.support.ManagedOperation;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.metadata.support.MetadataReader;
import org.springframework.jmx.util.JmxUtils;
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
        AbstractReflectionBasedModelMBeanInfoAssembler implements
        AutodetectCapableModelMBeanInfoAssembler {

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

    protected boolean includeReadAttribute(Method method) {
        return hasManagedAttribute(method);
    }

    protected boolean includeWriteAttribute(Method method) {
        return hasManagedAttribute(method);
    }

    protected boolean includeOperation(Method method) {
        if (JmxUtils.isProperty(method)) {
            return hasManagedAttribute(method);
        } else {
            return hasManagedOperation(method);
        }
    }

    protected String getOperationDescription(Method method) {

        if (JmxUtils.isProperty(method)) {
            ManagedAttribute ma = MetadataReader.getManagedAttribute(
                    attributes, method);

            if (ma == null) {
                return method.getName();
            } else {
                return ma.getDescription();
            }
        } else {
            ManagedOperation mo = MetadataReader.getManagedOperation(
                    attributes, method);

            if (mo == null) {
                return method.getName();
            } else {
                return mo.getDescription();
            }
        }
    }

    /**
     * Creates a description for the attribute corresponding to this property
     * descriptor. Attempts to create the description using metadata from either
     * the getter or setter attributes, otherwise uses the property name.
     * 
     * @param propertyDescriptor
     * @return
     */
    protected String getAttributeDescription(
            PropertyDescriptor propertyDescriptor) {

        Method readMethod = propertyDescriptor.getReadMethod();
        Method writeMethod = propertyDescriptor.getWriteMethod();

        ManagedAttribute getter = (readMethod != null) ? MetadataReader.getManagedAttribute(
                attributes, readMethod)
                : null;
        ManagedAttribute setter = (writeMethod != null) ? MetadataReader.getManagedAttribute(
                attributes, writeMethod)
                : null;

        StringBuffer sb = new StringBuffer();

        if ((getter != null) && (getter.getDescription() != null)
                && (getter.getDescription().length() > 0)) {
            return getter.getDescription();
        } else if ((setter != null) && (setter.getDescription() != null)
                && (setter.getDescription().length() > 0)) {
            return setter.getDescription();
        } else {
            return propertyDescriptor.getDisplayName();
        }
    }

    /**
     * Attempts to read managed resource description from the source level
     * metdata. Returns an empty <code>String</code> if no description can be
     * found.
     */
    protected String getDescription(Object bean) {
        ManagedResource mr = MetadataReader.getManagedResource(attributes,
                bean.getClass());

        if (mr == null) {
            return "";
        } else {
            return mr.getDescription();
        }
    }

    private boolean hasManagedAttribute(Method method) {
        ManagedAttribute ma = MetadataReader.getManagedAttribute(attributes,
                method);

        return (ma != null) ? true : false;
    }

    private boolean hasManagedOperation(Method method) {
        ManagedOperation mo = MetadataReader.getManagedOperation(attributes,
                method);

        return (mo != null) ? true : false;
    }

    /**
     * Used for auto detection of beans. Checks to see if the bean's class has a
     * ManagedResource attribute. If so it will add it list of included beans
     */
    public boolean includeBean(String beanName, Object bean) {
        if (MetadataReader.getManagedResource(attributes, bean.getClass()) != null) {
            return true;
        } else {
            return false;
        }
    }
}