/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/reporting/ValidationResults.java,v 1.5 2004-04-22 15:18:03 kdonald Exp $
 * $Revision: 1.5 $
 * $Date: 2004-04-22 15:18:03 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.rules.reporting;

import org.springframework.rules.UnaryPredicate;

/**
 * @author  Keith Donald
 */
public interface ValidationResults {
    
    /**
     * @return Returns the rejectedValue.
     */
    public Object getRejectedValue();

    /**
     * @return Returns the violatedConstraint.
     */
    public UnaryPredicate getViolatedConstraint();

    /**
     * @return Returns the violatedCount.
     */
    public int getViolatedCount();

    /**
     * @return Returns the severity.
     */
    public Severity getSeverity();
}