/*
 * Created on 30-Nov-2004
 */
package org.springframework.jmx.metadata.support.commons;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.jmx.metadata.support.InvalidMetadataException;
import org.springframework.jmx.metadata.support.JmxAttributeSource;
import org.springframework.jmx.metadata.support.ManagedAttribute;
import org.springframework.jmx.metadata.support.ManagedOperation;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.util.JmxUtils;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * Implementation of the <code>JmxAttributeSource</code> interface that 
 * reads metadata created using the Commons Attributes metatdaa mechanism.
 * @author Rob Harrop
 */
public class CommonsAttributesJmxAttributeSource implements JmxAttributeSource{

    private Attributes attributes = new CommonsAttributes();
 
    /**
     * If the specified class has a <code>ManagedResource</code> attribute, then it is returned.
     * Otherwise returns null. An <code>InvalidMetadataException</code> is thrown if more than one
     * ManagedResource attribute exists.
     * @param attributes The <code>Attributes</code> implementation to use for reading.
     * @param beanClass The <code>Class</code> to read the attribute data from.
     * @return The attribute if found, otherwise <code>null<code>.
     */
    public ManagedResource getManagedResource(Class cls) {
        Collection attrs = attributes.getAttributes(cls, ManagedResource.class);
        
        if(attrs.isEmpty()) {
            return null;
        } else if(attrs.size() == 1) {
            return (ManagedResource)attrs.iterator().next();
        } else {
            throw new InvalidMetadataException("A Class can have only one ManagedResource attribute");
        }
    }


    /**
     * If the specified method has a <code>ManagedAttribute</code> attribute, then it is returned.
     * Otherwise returns null. An <code>InvalidMetadataException</code> is thrown if more than one
     * <code>ManagedAttribute</code> attribute exists, or if the supplied method does not represent a JavaBean
     * property.
     * @param method The <code>Method</code> to read the attribute data from.
     * @return The attribute if found, otherwise <code>null</code>.
     */
    public ManagedAttribute getManagedAttribute(Method method) {
        if(!JmxUtils.isProperty(method)) {
            throw new InvalidMetadataException("The ManagedAttribute attribute is only valid for JavaBean properties. Use ManagedOperation for methods.");
        }
        
        Collection attrs = attributes.getAttributes(method, ManagedAttribute.class);
        
        if(attrs.isEmpty()) {
            return null;
        } else if(attrs.size() == 1) {
            return (ManagedAttribute)attrs.iterator().next();
        } else {
            throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
        }
    }

    /**
     * If the specified method has a <code>ManagedOperation</code> attribute, then it is returned.
     * Otherwise return null. An <code>InvalidMetadataException</code> is thrown if more than one
     * ManagedOperation attribute exists, or if the supplied method represents a JavaBean property.
     * @param attributes The <code>Attributes</code> implementation to use for reading.
     * @param method The <code>Method</code> to read attribute data from.
     * @return The attribute if found, otherwise <code>null</code>.
     * @see org.springframework.metadata.Attributes
     * @see org.springframework.jmx.metadata.support.ManagedOperation
     */
    public ManagedOperation getManagedOperation(Method method) {
        if(JmxUtils.isProperty(method)) {
            throw new InvalidMetadataException("The ManagedOperation attribute is not valid for JavaBean properties. Use ManagedAttribute instead.");
        }
        
        Collection attrs = attributes.getAttributes(method, ManagedOperation.class);
        
        if(attrs.isEmpty()) {
            return null;
        } else if(attrs.size() == 1) {
            return (ManagedOperation)attrs.iterator().next();
        } else {
            throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
        }
    }

}
