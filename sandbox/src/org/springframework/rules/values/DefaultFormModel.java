/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/DefaultFormModel.java,v 1.3 2004-06-13 11:35:31 kdonald Exp $
 * $Revision: 1.3 $
 * $Date: 2004-06-13 11:35:31 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Keith Donald
 */
public class DefaultFormModel implements FormModel {
    protected static final Log logger = LogFactory.getLog(DefaultFormModel.class);

    private MutableAspectAccessStrategy domainObjectAccessStrategy;

    private Object domainObject;
    
    private ValueModel domainObjectHolder;

    private ValueModel commitTrigger;

    private Map formValueModels = new HashMap();

    private boolean bufferChanges = true;

    private boolean hasErrors;

    public DefaultFormModel(Object domainObject) {
        this(new BeanPropertyAccessStrategy(domainObject));
    }

    public DefaultFormModel(ValueModel domainObjectHolder) {
        this(new BeanPropertyAccessStrategy(domainObjectHolder));
        this.domainObjectHolder = domainObjectHolder;
    }

    public DefaultFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy) {
        this(domainObjectAccessStrategy, true);
    }

    public DefaultFormModel(
            MutableAspectAccessStrategy domainObjectAccessStrategy,
            boolean bufferChanges) {
        this.domainObjectAccessStrategy = domainObjectAccessStrategy;
        this.commitTrigger = new ValueHolder(null);
        this.bufferChanges = bufferChanges;
    }

    public void setFormProperties(String[] domainObjectProperties) {
        formValueModels.clear();
        for (int i = 0; i < domainObjectProperties.length; i++) {
            add(domainObjectProperties[i]);
        }
    }

    public ValueModel getValueModel(String domainObjectProperty) {
        return (ValueModel)formValueModels.get(domainObjectProperty);
    }

    public boolean hasErrors() {
        return false;
    }

    protected Class getDomainObjectClass() {
        return domainObjectAccessStrategy.getDomainObject().getClass();
    }

    protected MutableAspectAccessStrategy getAccessStrategy() {
        return domainObjectAccessStrategy;
    }

    public ValueModel add(String domainObjectProperty) {
        ValueModel formValueModel = new AspectAdapter(domainObjectHolder,
                domainObjectAccessStrategy, domainObjectProperty);
        if (bufferChanges) {
            formValueModel = new BufferedValueModel(formValueModel,
                    commitTrigger);
        }
        onNewFormValueModel(domainObjectProperty, formValueModel);
        formValueModels.put(domainObjectProperty, formValueModel);
        return formValueModel;
    }

    protected void onNewFormValueModel(String domainObjectProperty,
            ValueModel formValueModel) {

    }

    public void commit() {
        if (bufferChanges) {
            if (hasErrors()) { throw new IllegalStateException(
                    "Form has errors; submit not allowed"); }
            commitTrigger.set(Boolean.TRUE);
            commitTrigger.set(null);
        }
    }

    public void revert() {
        if (bufferChanges) {
            commitTrigger.set(Boolean.FALSE);
            commitTrigger.set(null);
        }
    }
}