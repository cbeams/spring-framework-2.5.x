/*
 * Created on Jul 21, 2004
 */
package org.springframework.jmx.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.exceptions.ObjectNamingException;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.metadata.support.MetadataReader;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * @author robh
 */
public class MetadataNamingStrategy implements ObjectNamingStrategy {

    /**
     * The <tt>Attributes</tt> implementation. Uses Commons Attributes by
     * default.
     */
    private Attributes attributes = new CommonsAttributes();

    /**
     * Reads the ObjectName from the attribute's associated
     * with the managed resource's Class.
     */
    public ObjectName getObjectName(Object managedResource, String key)
            throws ObjectNamingException {
        ManagedResource mr = MetadataReader.getManagedResource(attributes,
                managedResource.getClass());

        // check that the managed resource attribute has been specified
        if (mr == null) {
            throw new ObjectNamingException(
                    "Your bean class ["
                            + managedResource.getClass().getName()
                            + "] must be marked with a valid ManagedResource attribute "
                            + "when using MetadataNamingStrategy");
        }

        // check that an object name has been specified
        String objectName = mr.getObjectName();

        if ((objectName == null) || (objectName.length() == 0)) {
            throw new ObjectNamingException(
                    "You must specify an ObjectName for Class: "
                            + managedResource.getClass().getName());
        }

        // now try to parse the name
        try {
            return ObjectName.getInstance(objectName);
        } catch (MalformedObjectNameException ex) {
            throw new ObjectNamingException("The specified object name:"
                    + objectName + " is malformed", ex);
        }
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
}