/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Collection;

/**
 * A simple interface for accessing attributes - helps prevent accidental
 * misuse/manipulation of more enabling interfaces like Map, for example --
 * through better encapsulation.
 * @author Keith Donald
 */
public interface AttributesAccessor {
    public Object getAttribute(String attributeName);

    public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException;

    public Object getRequiredAttribute(String attributeName) throws IllegalStateException;

    public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException;

    public boolean containsAttribute(String attributeName);
    
    public Collection attributeNames();
    
    public Collection attributeEntries();
    
    public Collection attributeValues();
}