/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor.predicates;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.functor.UnaryPredicate;

public class PropertyPresent implements UnaryPredicate {
    private String propertyName;

    public PropertyPresent(String propertyName) {
        this.propertyName = propertyName;
    }

    public boolean evaluate(Object bean) {
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        Object otherValue = wrapper.getPropertyValue(propertyName);
        return Required.instance().evaluate(otherValue);
    }

}