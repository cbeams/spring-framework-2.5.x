/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/NestingFormModel.java,v 1.1 2004-06-24 18:05:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-24 18:05:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * 
 * @author Keith Donald
 */
public interface NestingFormModel extends FormModel {
    public ValueModel findValueModelFor(FormModel delegatingChild,
            String domainObjectProperty);
}