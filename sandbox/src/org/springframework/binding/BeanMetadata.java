/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

import java.util.Collection;

/**
 * @author Keith Donald
 */
public interface BeanMetadata {
    public String getName();

    public String getCaption();

    public String getDescription();

    public Class getType();

    public Object newInstance();

    public Collection getPropertyMetadataCollection();

    public PropertyMetadata getPropertyMetadata(String propertyPath);
}