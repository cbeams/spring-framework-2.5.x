/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.value.support;

import org.springframework.binding.PropertyMetadata;
import org.springframework.binding.value.ValueModel;

/**
 * 
 * @author HP
 */
public class PropertyValueGuard extends ValueModelWrapper {

    private PropertyMetadata propertyMetadata;
    
    private ValueModel propertyValueModel;
    
    public PropertyValueGuard(PropertyMetadata metadata, ValueModel propertyValueModel) {
        super(propertyValueModel);
        this.propertyMetadata = metadata;
    }
    
    public Object getValue() {
        if (propertyMetadata.isReadable()) {
            return super.getValue();
        } else {
            throw new IllegalStateException("Property not readable to you");
        }
    }
    
    public void setValue(Object value) {
        if (propertyMetadata.isWriteable()) {
            super.setValue(value);
        } else {
            throw new IllegalStateException("Property not writeable to you");
        }
    }
}