/*
 * Created on Jul 21, 2004
 */
package org.springframework.jmx.metadata.support;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.jmx.util.JmxUtils;
import org.springframework.metadata.Attributes;

/**
 * @author robh
 */
public class MetadataReader {

    private MetadataReader() {
        
    }
    
    /**
     * If the specified class has a ManagedResource attribute, then it is returned.
     * Otherwise returns null. An InvalidMetadataException is thrown if more than one
     * ManagedResource attribute exists.
     * @param attributes The <tt>Attributes</tt> implementation to use for reading.
     * @param beanClass The Class to read the attribute data from.
     * @return The attribute if found, otherwise null.
     */
    public static ManagedResource getManagedResource(Attributes attributes, Class beanClass) {
        Collection attrs = attributes.getAttributes(beanClass, ManagedResource.class);
        
        if(attrs.isEmpty()) {
            return null;
        } else if(attrs.size() == 1) {
            return (ManagedResource)attrs.iterator().next();
        } else {
            throw new InvalidMetadataException("A Class can have only one ManagedResource attribute");
        }
    }
    
    /**
     * If the specified method has a ManagedAttribute attribute, then it is returned.
     * Otherwise returns null. An <tt>InvalidMetadataException</tt> is thrown if more than one
     * ManagedAttribute attribute exists, or if the supplied method does not represent a JavaBean
     * property.
     * @param method The <tt>Method</tt> to read the attribute data from.
     * @return The attribute if found, otherwise null.
     */
    public static ManagedAttribute getManagedAttribute(Attributes attributes, Method method) {
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
     * If the specified method has a ManagedOperation attribute, then it is returned.
     * Otherwise return null. An <tt>InvalidMetadataException</tt> is thrown if more than one
     * ManagedOperation attribute exists, or if the supplied method represents a JavaBean property.
     * @param attributes The <tt>Attributes</tt> implementation to use for reading.
     * @param method The <tt>Method</tt> to read attribute data from.
     * @return The attribute if found, otherwise null.
     * @see org.springframework.metadata.Attributes
     * @see org.springframework.jmx.metadata.support.ManagedOperation
     */
    public static ManagedOperation getManagedOperation(Attributes attributes, Method method) {
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
