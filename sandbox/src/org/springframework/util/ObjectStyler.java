/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

/**
 * Strategy interface encapsulating rules for styling the string form
 * of an object.
 * 
 * @author  Keith Donald
 */
public interface ObjectStyler {

    /**
     * Styles the string form of this object.
     * 
     * @param object
     *            The object to be styled.
     * @return The styled string.
     */
    public String style(Object value);
}