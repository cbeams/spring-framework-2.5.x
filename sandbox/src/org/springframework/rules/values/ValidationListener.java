/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValidationListener.java,v 1.1 2004-06-12 22:11:56 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 22:11:56 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.reporting.ValidationResults;

/**
 * 
 * @author Keith Donald
 */
public interface ValidationListener {
    public void constraintSatisfied(UnaryPredicate constraint);

    public void constraintViolated(UnaryPredicate constraint,
            ValidationResults results);
}