/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.util.Assert;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.TransitionCriteria;

public class OgnlTransitionCondition implements TransitionCriteria {

	private String expressionString;

	public OgnlTransitionCondition(String expressionString) {
		this.expressionString = expressionString;
	}

	public boolean test(FlowExecutionContext context) {
		try {
			Object result = Ognl.getValue(this.expressionString, context);
			Assert.isInstanceOf(Boolean.class, result);
			return ((Boolean)result).booleanValue();
		}
		catch (OgnlException e) {
			IllegalArgumentException iae = new IllegalArgumentException("Invalid transition expression");
			iae.initCause(e);
			throw iae;
		}
	}
}
