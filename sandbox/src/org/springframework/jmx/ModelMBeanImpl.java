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
package org.springframework.jmx;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

import org.springframework.beans.BeanWrapper;

/**
 * ModelMBean implementation that uses reflection to invoke methods for
 * operations and attributes.
 * 
 * @author Rob Harrop
 */
public class ModelMBeanImpl implements ModelMBean {

    private MBeanInvoker invoker = null;
    
    private MBeanInfo beanInfo = null;

    private Object managedResource = null;

    private BeanWrapper resourceWrapper = null;

    public ModelMBeanImpl(MBeanInvoker invoker) {
        this.invoker = invoker;
    }
    
    /**
     * Store the ModelMBeanInfo for this MBean. This method builds a cache of
     * Method instances to service requests to invoke operations and read/write
     * attributes
     */
    public void setModelMBeanInfo(ModelMBeanInfo beanInfo)
            throws MBeanException, RuntimeOperationsException {
        this.beanInfo = (MBeanInfo) beanInfo;

    }

    /**
     * Specify the resource that is being managed by this instance.
     */
    public void setManagedResource(Object managedResource, String resourceType)
            throws MBeanException, RuntimeOperationsException,
            InstanceNotFoundException, InvalidTargetObjectTypeException {

        this.managedResource = managedResource;

        // pass the managed resource to the invoker
        invoker.setManagedResource(managedResource);
    }

    /**
     * Gets the value of an attribute (property)
     */
    public Object getAttribute(String attributeName)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        return invoker.getAttribute(attributeName);
    }

    /**
     * Set the value of an attribute
     */
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        invoker.setAttribute(attribute);
    }

    /**
     * Returns a list of attribute values. These values are retreived by
     * reflection
     */
    public AttributeList getAttributes(String[] attributeNames) {
        return invoker.getAttributes(attributeNames);
    }

    /**
     * Set the value of many attributes in one go.
     */
    public AttributeList setAttributes(AttributeList attributes) {
        return invoker.setAttributes(attributes);
    }


    public Object invoke(String method, Object[] args, String[] signature)
            throws MBeanException, ReflectionException {
        return invoker.invoke(method, args, signature);
    }


    public MBeanInfo getMBeanInfo() {
        return this.beanInfo;
    }


    public void load() throws MBeanException, RuntimeOperationsException,
            InstanceNotFoundException {

    }

    public void store() throws MBeanException, RuntimeOperationsException,
            InstanceNotFoundException {
    }


    public void sendNotification(Notification notification)
            throws MBeanException, RuntimeOperationsException {

    }


    public void sendNotification(String notification) throws MBeanException,
            RuntimeOperationsException {
    }


    public void sendAttributeChangeNotification(AttributeChangeNotification arg0)
            throws MBeanException, RuntimeOperationsException {

    }


    public void sendAttributeChangeNotification(Attribute arg0, Attribute arg1)
            throws MBeanException, RuntimeOperationsException {

    }


    public void addAttributeChangeNotificationListener(
            NotificationListener arg0, String arg1, Object arg2)
            throws MBeanException, RuntimeOperationsException,
            IllegalArgumentException {

    }


    public void removeAttributeChangeNotificationListener(
            NotificationListener arg0, String arg1) throws MBeanException,
            RuntimeOperationsException, ListenerNotFoundException {

    }


    public void addNotificationListener(NotificationListener arg0,
            NotificationFilter arg1, Object arg2)
            throws IllegalArgumentException {


    }


    public void removeNotificationListener(NotificationListener arg0)
            throws ListenerNotFoundException {


    }


    public MBeanNotificationInfo[] getNotificationInfo() {
        return null;
    }
}