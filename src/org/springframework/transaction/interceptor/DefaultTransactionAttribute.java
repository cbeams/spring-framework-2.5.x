/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.transaction.interceptor;

import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Transaction attribute that takes the EJB approach to rolling
 * back on runtime, but not checked, exceptions.
 * @author Rod Johnson
 * @since 16-Mar-2003
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
