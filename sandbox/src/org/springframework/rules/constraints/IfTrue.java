/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/constraints/IfTrue.java,v 1.1 2004-11-03 19:02:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-11-03 19:02:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.constraints;

import org.springframework.util.Assert;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.support.AbstractConstraint;

public class IfTrue extends AbstractConstraint {

	private Constraint constraint;

	private Constraint mustBeTrueConstraint;

	private Constraint elseTrueConstraint;

	public IfTrue(Constraint constraint, Constraint mustAlsoBeTrue) {
		Assert.notNull(constraint, "The constraint that may be true is required");
		Assert.notNull(mustAlsoBeTrue, "The constraint that must be true IF the first constraint is true is required");
		this.constraint = constraint;
		this.mustBeTrueConstraint = mustAlsoBeTrue;
	}

	public IfTrue(Constraint constraint, Constraint mustAlsoBeTrue, Constraint elseMustAlsoBeTrue) {
		Assert.notNull(constraint, "The constraint that may be true is required");
		Assert.notNull(mustAlsoBeTrue, "The constraint that must be true IF the first constraint is true is required");
		this.constraint = constraint;
		this.mustBeTrueConstraint = mustAlsoBeTrue;
		this.elseTrueConstraint = elseMustAlsoBeTrue;
	}

	public boolean test(Object argument) {
		if (constraint.test(argument)) {
			return mustBeTrueConstraint.test(argument);
		}
		else {
			if (elseTrueConstraint != null) {
				return elseTrueConstraint.test(argument);
			}
			else {
				return true;
			}
		}
	}

}