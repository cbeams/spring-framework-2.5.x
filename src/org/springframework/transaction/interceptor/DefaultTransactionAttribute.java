/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Transaction attribute that takes EJB approach to rolling
 * back on runtime, but not checked, exceptions.
 * @author Rod Johnson
 * @since 16-Mar-2003
 * @version $Id: DefaultTransactionAttribute.java,v 1.2 2003-08-18 16:16:35 jhoeller Exp $
 */
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

	/** Prefix for rollback-on-exception rules in description strings */
	public static final String ROLLBACK_RULE_PREFIX = "-";

	/** Prefix for commit-on-exception rules in description strings */
	public static final String COMMIT_RULE_PREFIX = "+";

	public DefaultTransactionAttribute() {
	}

	public DefaultTransactionAttribute(int propagationBehavior) {
		super(propagationBehavior);
	}

	/**
	 * Default behaviour is as with EJB: rollback on unchecked exception.
	 * Consistent with TransactionTemplate's behavior.
	 */
	public boolean rollbackOn(Throwable t) {
		return (t instanceof RuntimeException);
	}

	/**
	 * Return a description of this transaction attribute.
	 * The format matches the one used by TransactionAttributeEditor,
	 * to be able to feed toString results into TransactionAttribut properties.
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public String toString() {
		StringBuffer result = getDefinitionDescription();
		result.append(',');
		result.append(ROLLBACK_RULE_PREFIX + "RuntimeException");
		return result.toString();
	}

}
