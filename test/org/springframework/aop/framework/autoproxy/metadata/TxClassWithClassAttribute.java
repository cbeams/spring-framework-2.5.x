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

import org.springframework.transaction.interceptor.TransactionInterceptor;


/**
 * Illustrates class and method attributes
 * <br>The attribute syntax is that of Commons Attributes.
 * 
 * @@org.springframework.aop.framework.autoproxy.target.PoolingAttribute (10)
 * 
 * @@org.springframework.transaction.interceptor.DefaultTransactionAttribute ()
 * 
 * @author Rod Johnson
 */
public class TxClassWithClassAttribute {
	
	
	public int inheritClassTxAttribute(int i) {
		return i;
	}
	
	/**
	 * Inherits transaction attribute from class.
	 * Illustrates programmatic rollback.
	 * @param rollbackOnly
	 */
	public void rollbackOnly(boolean rollbackOnly) {
		if (rollbackOnly) {
			setRollbackOnly();
		}
	}
	
	/**
	 * Extracted in a protected method to facilitate testing
	 */
	protected void setRollbackOnly() {
		TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
	}

	/**
	 * @@org.springframework.transaction.interceptor.RuleBasedTransactionAttribute ()
	 * @@org.springframework.transaction.interceptor.RollbackRuleAttribute (Exception.class)
	 * @@org.springframework.transaction.interceptor.NoRollbackRuleAttribute ("ServletException")
	 */
	public void echoException(Exception ex) throws Exception {
		if (ex != null)
			throw ex;
	}

}
