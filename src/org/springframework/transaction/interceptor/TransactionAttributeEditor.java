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

import java.beans.PropertyEditorSupport;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.StringUtils;

/**
 * PropertyEditor for TransactionAttribute objects. Takes Strings of form
 * <p><code>PROPAGATION_NAME,ISOLATION_NAME,readOnly,timeout_NNNN,+Exception1,-Exception2</code>
 * <p>where only propagation code is required. For example:
 * <p><code>PROPAGATION_MANDATORY,ISOLATION_DEFAULT</code>
 *
 * <p>The tokens can be in <strong>any</strong> order. Propagation and isolation codes
 * must use the names of the constants in the TransactionDefinition class. Timeout values
 * are in seconds. If no timeout is specified, the transaction manager will apply a default
 * timeout specific to the particular transaction manager.
 *
 * <p>A "+" before an exception name substring indicates that
 * transactions should commit even if this exception is thrown;
 * a "-" that they should roll back.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 24-Apr-2003
 * @see org.springframework.transaction.TransactionDefinition
 * @see org.springframework.core.Constants
 */
public class TransactionAttributeEditor extends PropertyEditorSupport {

	/**
	 * Format is PROPAGATION_NAME,ISOLATION_NAME,readOnly,timeout_NNNN,+Exception1,-Exception2.
	 * Null or the empty string means that the method is non transactional.
	 * @see java.beans.PropertyEditor#setAsText(java.lang.String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {
		if (s == null || "".equals(s)) {
			setValue(null);
		}
		else {	
			// tokenize it with ","
			String[] tokens = StringUtils.commaDelimitedListToStringArray(s);
			RuleBasedTransactionAttribute attr = new RuleBasedTransactionAttribute();

			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i].trim();
				if (token.startsWith(TransactionDefinition.PROPAGATION_CONSTANT_PREFIX)) {
					attr.setPropagationBehaviorName(token);
				}
				else if (token.startsWith(TransactionDefinition.ISOLATION_CONSTANT_PREFIX)) {
					attr.setIsolationLevelName(token);
				}
				else if (token.startsWith(DefaultTransactionAttribute.TIMEOUT_PREFIX)) {
					String value = token.substring(DefaultTransactionAttribute.TIMEOUT_PREFIX.length());
					attr.setTimeout(Integer.parseInt(value));
				}
				else if (token.equals(DefaultTransactionAttribute.READ_ONLY_MARKER)) {
					attr.setReadOnly(true);
				}
				else if (token.startsWith(DefaultTransactionAttribute.COMMIT_RULE_PREFIX)) {
					attr.getRollbackRules().add(new NoRollbackRuleAttribute(token.substring(1)));
				}
				else if (token.startsWith(DefaultTransactionAttribute.ROLLBACK_RULE_PREFIX)) {
					attr.getRollbackRules().add(new RollbackRuleAttribute(token.substring(1)));
				}
				else {
					throw new IllegalArgumentException("Illegal transaction attribute token: [" + token + "]");
				}
			}

			setValue(attr);
		}
	}

}
