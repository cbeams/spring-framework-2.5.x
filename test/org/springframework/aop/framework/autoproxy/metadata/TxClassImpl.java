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

package org.springframework.aop.framework.autoproxy.metadata;

// This import is purely to allow attributes to use constants defined in this class:
// and to avoid FQNs
// don't let your IDE remove it!
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.*;

/**
 * The attribute syntax is that of Commons Attributes.
 * @author Rod Johnson
 */
public class TxClassImpl implements TxClass {
	
	private int invocations;
	
	/**
	 * The following constant requires TransactionDefinition to be imported.
	 * Of course an FQN could be used...
	 * @@DefaultTransactionAttribute (TransactionDefinition.PROPAGATION_REQUIRED)
	 */
	public int defaultTxAttribute() {
		return ++invocations;
	}
	
	
	/**
	 * Don't put a space before string values...
	 * We don't need FQN because we imported this package.
	 * The first RollbackRuleAttribute shows the preferred constructor,
	 * taking a Class rather than a String. Normally this class wouldn't
	 * need to be imported, as it would be imported for use in the business method.
	 * Note that both FQN and relying on the import above work.
	 * 
	 * @@RuleBasedTransactionAttribute()
	 * @@RollbackRuleAttribute (Exception.class)
	 * @@org.springframework.transaction.interceptor.NoRollbackRuleAttribute ("ServletException")
	 */
	public void echoException(Exception ex) throws Exception {
		if (ex != null)
			throw ex;
	}

}
