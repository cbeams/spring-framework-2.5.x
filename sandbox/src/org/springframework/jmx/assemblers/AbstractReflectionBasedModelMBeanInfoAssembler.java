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

import org.springframework.beans.BeanUtils;
import org.springframework.jmx.exceptions.MBeanAssemblyException;
import org.springframework.jmx.util.JmxUtils;

import javax.management.Descriptor;
import javax.management.IntrospectionException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author robh
 */
public abstract class AbstractReflectionBasedModelMBeanInfoAssembler extends
        AbstractModelMBeanInfoAssembler {

    private static final String VISIBILITY = "visibility";
    private static final String SETTER = "setter";
    private static final String GETTER = "getter";
    private static final String OPERATION = "operation";
    private static final String ROLE = "role";
    private static final Integer ATTRIBUTE_OPERATION_VISIBILITY = new Integer(4);

    private static final String GET_CLASS = "getClass";

    protected ModelMBeanAttributeInfo[] getAttributeInfo(String beanKey, Class beanClass) {
        PropertyDescriptor[] props = BeanUtils.getPropertyDescriptors(beanClass);
        ModelMBeanAttributeInfo[] info = new ModelMBeanAttributeInfo[props.length];
        int attrCount = 0;

        try {
            for (int x = 0; x < props.length; x++) {

                Method getter = props[x].getReadMethod();

                // check for getClass()
                if (getter != null && GET_CLASS.equals(getter.getName())) {
                    continue;
                }

                if (getter != null && !includeReadAttribute(getter)) {
                    getter = null;
                }

                Method setter = props[x].getWriteMethod();

                if (setter != null && !includeWriteAttribute(setter)) {
                    setter = null;
                }

                if ((getter != null) || (setter != null)) {
                    // if both getter and setter are null
                    // then this does not need exposing
                    ModelMBeanAttributeInfo inf = new ModelMBeanAttributeInfo(
                            props[x].getName(),
                            getAttributeDescription(props[x]), getter, setter);

                    Descriptor desc = inf.getDescriptor();

                    if (getter != null) {
                        desc.setField("getMethod", getter.getName());
                    }

                    if (setter != null) {
                        desc.setField("setMethod", setter.getName());
                    }
                    
                    populateAttributeDescriptor(desc, getter, setter);

                    inf.setDescriptor(desc);

                    info[attrCount++] = inf;
                }

            }
        } catch (IntrospectionException ex) {
            throw new MBeanAssemblyException("Unable to attribute info."
                    + " Check that you have specified valid metadata", ex);
        }

        // create a final array to hold the attributes
        ModelMBeanAttributeInfo[] result = JmxUtils.shrink(info, attrCount);

        // release the temp array
        info = null;

        return result;
    }

    protected ModelMBeanConstructorInfo[] getConstructorInfo(String beanKey, Class beanClass) {
        return new ModelMBeanConstructorInfo[] {};
    }

    protected ModelMBeanOperationInfo[] getOperationInfo(String beanKey, Class beanClass) {
        Method[] methods = beanClass.getMethods();
        ModelMBeanOperationInfo[] info = new ModelMBeanOperationInfo[methods.length];
        int attrCount = 0;

        for (int x = 0; x < methods.length; x++) {
            Method method = methods[x];
            ModelMBeanOperationInfo inf = null;

            if(method.getDeclaringClass() == Object.class) {
                continue;
            }

            if ((JmxUtils.isGetter(method) && includeReadAttribute(method))
                    || (JmxUtils.isSetter(method) && includeWriteAttribute(method))) {
                // attributes need to have their methods exposed as
                // operations to the JMX server as well.
                inf = new ModelMBeanOperationInfo(
                        getOperationDescription(method), method);

                
                Descriptor desc = inf.getDescriptor();
                desc.setField(VISIBILITY, ATTRIBUTE_OPERATION_VISIBILITY);
                
                if(JmxUtils.isGetter(method)) {
                    desc.setField(ROLE, GETTER);
                } else {
                    desc.setField(ROLE, SETTER);
                }
                
                inf.setDescriptor(desc);

            } else if (includeOperation(method)) {
                inf = new ModelMBeanOperationInfo(
                        getOperationDescription(method), method);
                
                Descriptor desc = inf.getDescriptor();
                desc.setField(ROLE, OPERATION);
                populateOperationDescriptor(desc, method);
                inf.setDescriptor(desc);
            }

            if (inf != null) {
                info[attrCount++] = inf;
            }

        }

        ModelMBeanOperationInfo[] result = JmxUtils.shrink(info, attrCount);
        return result;
    }

    protected ModelMBeanNotificationInfo[] getNotificationInfo(String beanKey, Class beanClass) {
        return new ModelMBeanNotificationInfo[] {};
    }

    protected abstract boolean includeReadAttribute(Method method);

    protected abstract boolean includeWriteAttribute(Method method);

    protected abstract boolean includeOperation(Method method);

    protected abstract String getOperationDescription(Method method);

    protected abstract String getAttributeDescription(
            PropertyDescriptor propertyDescriptor);

    protected abstract void populateAttributeDescriptor(Descriptor descriptor,
            Method getter, Method setter);
    
    protected abstract void populateOperationDescriptor(Descriptor descriptor, Method method);
}