/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

import java.lang.reflect.Method;

import net.sf.hibernate.mapping.Constraint;

/**
 * 
 * @author HP
 */
public interface PropertyMetadata {
    public String getName();

    public String getDescription();

    public Class getType();

    public boolean isReadable();

    public boolean isWriteable();

    public boolean isCollection();
    
    public boolean isBean();
    
    public BeanMetadata getBeanMetadata() throws IllegalStateException;
    
    public Method getReadMethod();
    
    public Method getWriteMethod();

    public boolean isBound();

    public boolean isConstrained();

    public Constraint getValueConstraint();

    public Object newInstance();
}