/*
 * Created on 19-Nov-2004
 */
package org.springframework.jmx.assemblers;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.IntrospectionException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jmx.exceptions.MBeanAssemblyException;
import org.springframework.jmx.util.JmxUtils;

/**
 * @author robh
 */
public abstract class AbstractReflectionBasedModelMBeanInfoAssembler extends
        AbstractModelMBeanInfoAssembler {

    private static final Integer ATTRIBUTE_OPERATION_VISIBILITY = new Integer(4);
    
    private static final String GET_CLASS = "getClass";

    protected ModelMBeanAttributeInfo[] getAttributeInfo(Object bean) {
        Class beanClass = bean.getClass();
        BeanWrapper bw = new BeanWrapperImpl(bean);

        PropertyDescriptor[] props = bw.getPropertyDescriptors();
        ModelMBeanAttributeInfo[] info = new ModelMBeanAttributeInfo[props.length];
        int attrCount = 0;

        try {
            for (int x = 0; x < props.length; x++) {

                Method getter = props[x].getReadMethod();

                // check for getClass()
                if(getter != null && GET_CLASS.equals(getter.getName())) {
                    continue;
                }
                
                if (getter != null && !includeReadAttribute(getter)) {
                    getter = null;
                }

                Method setter = props[x].getWriteMethod();

                if (setter !=null && !includeWriteAttribute(setter)) {
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

    protected ModelMBeanConstructorInfo[] getConstructorInfo(Object bean) {
        return new ModelMBeanConstructorInfo[] {};
    }

    protected ModelMBeanOperationInfo[] getOperationInfo(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        ModelMBeanOperationInfo[] info = new ModelMBeanOperationInfo[methods.length];
        int attrCount = 0;

        for (int x = 0; x < methods.length; x++) {
            Method method = methods[x];
            ModelMBeanOperationInfo inf = null;

            if ((JmxUtils.isGetter(method) && includeReadAttribute(method))
                    || (JmxUtils.isSetter(method) && includeWriteAttribute(method))) {
                // attributes need to have their methods exposed as
                // operations to the JMX server as well. We set low
                // visibility to try and hide these operations
                // from any adapters.
                inf = new ModelMBeanOperationInfo(
                        getOperationDescription(method), method);

                Descriptor desc = inf.getDescriptor();
                desc.setField("visibility", ATTRIBUTE_OPERATION_VISIBILITY);
                inf.setDescriptor(desc);

            } else if (includeOperation(method)) {
                inf = new ModelMBeanOperationInfo(
                        getOperationDescription(method), method);
            }

            if (inf != null) {
                info[attrCount++] = inf;
            }

        }

        ModelMBeanOperationInfo[] result = JmxUtils.shrink(info, attrCount);
        return result;
    }

    protected ModelMBeanNotificationInfo[] getNotificationInfo(Object bean) {
        return new ModelMBeanNotificationInfo[] {};
    }

    protected abstract boolean includeReadAttribute(Method method);

    protected abstract boolean includeWriteAttribute(Method method);

    protected abstract boolean includeOperation(Method method);

    protected abstract String getOperationDescription(Method method);

    protected abstract String getAttributeDescription(
            PropertyDescriptor propertyDescriptor);
}