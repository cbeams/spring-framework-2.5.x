/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/FormModel.java,v 1.1 2004-06-12 07:27:08 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:08 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * 
 * @author Keith Donald
 */
public interface FormModel {
    public boolean hasErrors();

    public void commit();

    public void revert();
}