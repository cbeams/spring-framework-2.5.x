/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/reporting/ValidationResults.java,v 1.7 2004-10-09 17:59:56 jhoeller Exp $
 * $Revision: 1.7 $
 * $Date: 2004-10-09 17:59:56 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.rules.reporting;

import org.springframework.rules.Constraint;

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