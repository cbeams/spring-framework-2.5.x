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

import org.springframework.transaction.TransactionDefinition;

/**
 * This interface adds a rollBackOn specification to TransactionDefinition.
 * As custom rollBackOn is only possible with AOP, this class resides
 * in the AOP transaction package.
 *
 * @author Rod Johnson
 * @since 16-Mar-2003
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 */
public interface TransactionAttribute extends TransactionDefinition {
	
	/**
	 * Should we roll back on a checked exception?
	 * @param ex the exception to evaluate
	 * @return boolean rollback or not
	 */
	boolean rollbackOn(Throwable ex);
	
}
