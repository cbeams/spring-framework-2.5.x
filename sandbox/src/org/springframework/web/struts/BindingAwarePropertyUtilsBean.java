/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 * 
 * This file is part of the NewBACS programme. (c) Voca 2004-5. All rights
 * reserved.
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
