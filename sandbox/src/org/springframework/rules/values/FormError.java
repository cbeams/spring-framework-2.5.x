/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/FormError.java,v 1.1 2004-07-16 03:10:10 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-07-16 03:10:10 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.reporting.ValidationResults;

/**
 * @author Keith Donald
 */
public class FormError {
    private String formObjectProperty;
    private ValidationResults error;
    
    public FormError(String formObjectProperty, ValidationResults error) {
        this.formObjectProperty = formObjectProperty;
        this.error = error;
    }
    
    public String getProperty() {
        return formObjectProperty;
    }
    
    public ValidationResults getError() {
        return error;
    }
}
