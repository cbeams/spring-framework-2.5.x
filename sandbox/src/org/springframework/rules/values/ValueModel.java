/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValueModel.java,v 1.1 2004-06-12 07:27:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * A container for a value that can change. This container encapsulates /
 * abstracts away the logic necessary to access, set, and be notified of changes
 * in the contained value.
 * 
 * The value model pattern greatly simplifies client(s) access to a mutable
 * value, as clients access the value in a consistent manner (through this
 * common interface). Clients may also use this interface to subscribe for value
 * change events.
 * 
 * A good example of the ValueModel pattern is the ThreadLocal object. Another
 * example is a mediator between GUI controls and the domain model.
 * 
 * @author Keith Donald
 */
public interface ValueModel extends ValueChangeable {

    /**
     * Get the value contained by this model.
     * 
     * @return the current value
     */
    public Object get();

    /**
     * Set (or replace if the value is already set) the value contained by this
     * model.
     * 
     * @param value
     *            the new value
     */
    public void set(Object value);
}