/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

/**
 * @author Keith Donald
 */
public interface BeanMetadata {
    public String getName();

    public String getDescription();

    public Class getType();

    public Object newInstance();
    
    public PropertyMetadata[] getProperties();
    
    public PropertyMetadata getProperty(String propertyPath);
}