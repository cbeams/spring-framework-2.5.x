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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * ModelMBean implementation that uses reflection to invoke methods for
 * operations and attributes.
 * 
 * @author Rob Harrop
 * @since 1.2
 */
public class ModelMBeanImpl implements ModelMBean, NotificationEmitter {

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(ModelMBeanImpl.class);

    /**
     * MBeanInvoker responsible for invocations against this MBean
     */
    private MBeanInvoker invoker = null;

    /**
     * Management Interface metadata for this MBean
     */
    private MBeanInfo beanInfo = null;

    /**
     * Store a reference to the managed resource
     */
    private Object managedResource = null;

    /**
     * NotificationBroadcasterSupport for use with AttributeChangeNotifications
     */
    private NotificationBroadcasterSupport attributeNotificationBroadcaster = null;

    /**
     * Maintains a list of AttributeChangeNotificationFilters, keyed by
     * NotificationListener. This allows for the more fine grained control over
     * attribute notifications that is not supported by the
     * NotificationBroadcasterSupport.
     */
    private Map attributeNotificationFilters = new HashMap();

    /**
     * Sequence number for broadcasting notifications
     */
    private long sequenceNumber = 0;

    /**
     * Notification message for attribute change notifications.
     */
    private static final String MESSAGE = "Attribute Value Changed.";

    /**
     * Create a new instance using the supplier <tt>MBeanInvoker</tt>
     * 
     * @param invoker
     *            An implementation of MBeanInvoker used to invoke operations on
     *            the MBean
     */
    public ModelMBeanImpl(MBeanInvoker invoker) {
        this.invoker = invoker;

        // create broadcaster support object
        attributeNotificationBroadcaster = new NotificationBroadcasterSupport();
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
     * 
     * @param attributeName
     *            The name of the attribute whose value you want to return
     * @return The value of the attribute
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
        Attribute current = new Attribute(attribute.getName(),
                getAttribute(attribute.getName()));
        invoker.setAttribute(attribute);
        sendAttributeChangeNotification(current, attribute);
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
        AttributeList result = new AttributeList();
        for (int x = 0; x < attributes.size(); x++) {
            Attribute a = (Attribute) attributes.get(x);
            try {
                setAttribute(a);
                result.add(a);
            } catch (JMException ignored) {
                // failures are ignored
            }
        }
        return result;
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
        // TODO: Implement me
    }

    public void sendNotification(String notification) throws MBeanException,
            RuntimeOperationsException {
        // TODO: Implement me
    }

    public void addNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handBack)
            throws IllegalArgumentException {

        if(listener == null) {
            throw new IllegalArgumentException("The NotificationListener cannot be null");
        }
            
        if((filter != null) && (filter instanceof AttributeChangeNotificationFilter)) {
            attributeNotificationBroadcaster.addNotificationListener(listener, filter, handBack);
        }
    }

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        attributeNotificationBroadcaster.removeNotificationListener(listener);
    }
    

    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handBack) throws ListenerNotFoundException {
        attributeNotificationBroadcaster.removeNotificationListener(listener, filter, handBack);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return null;
    }

    public void sendAttributeChangeNotification(
            AttributeChangeNotification notification) throws MBeanException,
            RuntimeOperationsException {
        attributeNotificationBroadcaster.sendNotification(notification);
    }

    public void sendAttributeChangeNotification(Attribute originalAttr,
            Attribute newAttr) throws MBeanException,
            RuntimeOperationsException {

        // locate the attribute
        MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
        MBeanAttributeInfo attr = null;

        for (int x = 0; x < attrs.length; x++) {
            if (attrs[x].getName().equals(originalAttr.getName())) {
                attr = attrs[x];
                break;
            }
        }

        if (attr == null) {
            // just to be sure!
            throw new RuntimeOperationsException(
                    new NotificationException("Attribute: "
                            + originalAttr.getName() + " is unrecognised"));
        }

        AttributeChangeNotification notification = new AttributeChangeNotification(
                this, sequenceNumber++, System.currentTimeMillis(), MESSAGE,
                attr.getName(), attr.getType(), originalAttr, newAttr);

        sendAttributeChangeNotification(notification);
    }

    /**
     * Add a NotificationListener to listen for
     * changes in attribute values
     * @param listener The NotificationListener to notify of changes
     * @param attributeName The name of attribute to listen to or null to listen to all attributes 
     */
    public void addAttributeChangeNotificationListener(
            NotificationListener listener, String attributeName, Object handback)
            throws MBeanException, RuntimeOperationsException,
            IllegalArgumentException {

        // check for null listener as required
        if (listener == null) {
            throw new IllegalArgumentException("The listener cannot be null");
        }

        // must have beanInfo available
        if (beanInfo == null) {
            throw new RuntimeOperationsException(
                    new RuntimeException(
                            "No attribute information is available for this MBean."
                                    + " Ensure that the management information has been configured."));
        }
        
        MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
        MBeanAttributeInfo attribute = null;

        // see if we already have a filter for this listener
        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();

        // if an attribute name was
        // provided then we should check that it exists and
        // add it to the broadcaster. If the attribute
        // name was nullthen all attributes are added to
        // the broadcaster
        if (attributeName != null) {
            for (int x = 0; x < attrs.length; x++) {
                if (attrs[x].getName().equals(attributeName)) {
                    attribute = attrs[x];
                    break;
                }
            }

            // if we get to this point and no attriute
            // has been found then an
            // invalid attribute name was specified
            if (attribute == null) {
                throw new RuntimeOperationsException(new RuntimeException(
                        "Attribute name " + attributeName
                                + " is not recognised"));
            }

            // add to broadcaster
            filter.enableAttribute(attributeName);
            attributeNotificationBroadcaster.addNotificationListener(listener,
                    filter, handback);
        } else {

            for (int x = 0; x < attrs.length; x++) {
                filter.enableAttribute(attrs[x].getName());
            }

            attributeNotificationBroadcaster.addNotificationListener(listener,
                    filter, handback);

        }
        
        // cache the filter for this listener
        // allows us to disable just a specific
        // attribute name later on
        List filterList = (List)attributeNotificationFilters.get(listener);
        
        if(filterList == null) {
            filterList = new ArrayList();
            attributeNotificationFilters.put(listener, filterList);
        }
        
        filterList.add(filter);
    }


    public void removeAttributeChangeNotificationListener(
            NotificationListener listener, String attributeName)
            throws MBeanException, RuntimeOperationsException,
            ListenerNotFoundException {

        // check for null listener
        if (listener == null) {
            throw new ListenerNotFoundException(
                    "A null NotificationListener cannot be found");
        }

        // filter list
        List filters = (List)attributeNotificationFilters.get(listener);

        if(filters != null) {
            
            Iterator itr = filters.iterator();
            
            while(itr.hasNext()){
                AttributeChangeNotificationFilter filter = (AttributeChangeNotificationFilter)itr.next();
                
                if(attributeName == null) {
                    filter.disableAllAttributes();
                } else {
                    filter.disableAttribute(attributeName);
                }
            }
        }
    }
}