/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.struts;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * Override normal commons PropertyUtilsBean so that it is aware of BindingActionForm
 *  
 * @author Keith Donald
 * @author Colin Sampaleanu
 */

public class BindingAwarePropertyUtilsBean extends PropertyUtilsBean {
    public static final Logger logger = Logger.getLogger(BindingAwarePropertyUtilsBean.class.getName());

    /**
     * Override superclass method
     * @see org.apache.commons.beanutils.PropertyUtilsBean#getProperty(java.lang.Object, java.lang.String)
     */ 
    public Object getProperty(Object bean, String propertyPath) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (!(bean instanceof BindingActionForm)) {
            return super.getProperty(bean, propertyPath);
        }
        BindingActionForm form = (BindingActionForm)bean;
        return form.getFieldValue(propertyPath);
    }
    
    
    /* Override superclass method 
     * @see org.apache.commons.beanutils.PropertyUtilsBean#getNestedProperty(java.lang.Object, java.lang.String)
     */
    public Object getNestedProperty(Object bean, String propertyPath) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (!(bean instanceof BindingActionForm)) {
            return super.getNestedProperty(bean, propertyPath);
        }
        BindingActionForm form = (BindingActionForm)bean;
        return form.getFieldValue(propertyPath);
    }
}
