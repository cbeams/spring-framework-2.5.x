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
public class LazyCollectionInitializer extends ValueModelWrapper {

    private PropertyMetadata propertyMetadata;

    private ValueModel propertyValueModel;

    public LazyCollectionInitializer(PropertyMetadata metadata, ValueModel propertyValueModel) {
        super(propertyValueModel);
        this.propertyMetadata = metadata;
    }

    public Object getValue() {
        Object value = super.getValue();
        if (value == null) {
            setValue(propertyMetadata.newInstance());
            value = super.getValue();
        }
        return value;
    }

}