/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Transaction attribute that takes the EJB approach to rolling
 * back on runtime, but not checked, exceptions.
 * @author Rod Johnson
 * @since 16-Mar-2003
 * @version $Id: DefaultTransactionAttribute.java,v 1.3 2004-02-04 17:09:56 jhoeller Exp $
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
	 * Default behavior is as with EJB: rollback on unchecked exception.
	 * Additionally attempt to rollback on Error.
	 * Consistent with TransactionTemplate's behavior.
	 */
	public boolean rollbackOn(Throwable ex) {
		return (ex instanceof RuntimeException || ex instanceof Error);
	}

	/**
	 * Return a description of this transaction attribute.
	 * The format matches the one used by TransactionAttributeEditor,
	 * to be able to feed toString results into TransactionAttribute properties.
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public String toString() {
		StringBuffer result = getDefinitionDescription();
		result.append(',');
		result.append(ROLLBACK_RULE_PREFIX + "RuntimeException");
		return result.toString();
	}

}
