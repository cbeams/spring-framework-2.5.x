/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/MutableFormModel.java,v 1.1 2004-06-14 16:06:22 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-14 16:06:22 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * 
 * @author Keith Donald
 */
public interface MutableFormModel extends FormModel {
    public void setFormProperties(String[] domainObjectProperties);

    public ValueModel getValueModel(String domainObjectProperty);

    public ValueModel add(String domainObjectProperty);
}