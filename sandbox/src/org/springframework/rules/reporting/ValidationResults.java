/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/reporting/ValidationResults.java,v 1.9 2005-04-04 17:37:19 kdonald Exp $
 * $Revision: 1.9 $
 * $Date: 2005-04-04 17:37:19 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.rules.reporting;

import org.springframework.core.closure.Constraint;

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
	public Constraint getViolatedConstraint();

	/**
	 * @return Returns the violatedCount.
	 */
	public int getViolatedCount();

	/**
	 * @return Returns the severity.
	 */
	public Severity getSeverity();
}