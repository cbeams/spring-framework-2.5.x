/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.values;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Aspect access strategy that accesses bean property values.  An "aspect"
 * in this case is a single bean property.
 * 
 * @author Keith Donald
 */
public class BeanPropertyAccessStrategy implements MutableAspectAccessStrategy {
    private static final Log logger = LogFactory
            .getLog(BeanPropertyAccessStrategy.class);

    private Map listeners = new HashMap();

    private ValueModel beanHolder;

    private BeanWrapper beanWrapper;

    public BeanPropertyAccessStrategy(Object bean) {
        this.beanWrapper = new BeanWrapperImpl(bean);
        this.beanHolder = new ValueHolder(bean);
    }

    public BeanPropertyAccessStrategy(final ValueModel beanHolder) {
        this.beanWrapper = new BeanWrapperImpl(beanHolder.get());
        this.beanHolder = beanHolder;
        if (logger.isDebugEnabled()) {
            logger.debug("[Bean accessor attaching to mutable bean holder.]");
        }
        this.beanHolder.addValueListener(new ValueListener() {
            public void valueChanged() {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("[Updating the enclosed bean wrapper's target object]");
                }
                beanWrapper.setWrappedInstance(beanHolder.get());
            }
        });
    }

    public void addValueListener(ValueListener l, String aspect) {
        if (beanHolder.get() instanceof PropertyChangePublisher) {
            ValuePropertyChangeListenerMediator listener = new ValuePropertyChangeListenerMediator(
                    l, aspect, beanHolder);
            listeners.put(createListenerKey(l, aspect), listener);
        }
    }

    public void removeValueListener(ValueListener l, String aspect) {
        if (beanHolder.get() instanceof PropertyChangePublisher) {
            Set listenerKey = createListenerKey(l, aspect);
            ValuePropertyChangeListenerMediator listener = (ValuePropertyChangeListenerMediator)listeners
                    .get(listenerKey);
            if (listener != null) {
                listener.unsubscribe();
                listeners.remove(listenerKey);
            }
        }
    }

    private Set createListenerKey(ValueListener l, String aspect) {
        LinkedHashSet key = new LinkedHashSet();
        key.add(l);
        key.add(aspect);
        return key;
    }

    /**
     * @see org.springframework.rules.values.MutableAspectAccessStrategy#getValue(java.lang.String)
     */
    public Object getValue(String aspect) {
        return beanWrapper.getPropertyValue(aspect);
    }

    /**
     * @see org.springframework.rules.values.MutableAspectAccessStrategy#setValue(java.lang.String,
     *      java.lang.Object)
     */
    public void setValue(String aspect, Object value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting aspect '" + aspect + "' = " + value);
        }
        this.beanWrapper.setPropertyValue(aspect, value);
    }
    
    public Object getDomainObject() {
        return beanHolder.get();
    }
    
    public ValueModel getDomainObjectHolder() {
        return beanHolder;
    }

}