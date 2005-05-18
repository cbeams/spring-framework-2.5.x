/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.execution;

import org.springframework.web.flow.RequestContext;

/**
 * Interface to demarcate an application transaction for a flow execution.
 * 
 * @see org.springframework.web.flow.execution.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface TransactionSynchronizer {

	/**
	 * Is the caller participating in the application transaction currently
	 * active in the flow execution.
	 * @param context the flow execution request context
	 * @param end indicates whether or not the transaction should end after
	 *        checking it
	 * @return true if it is participating in the active transaction, false
	 *         otherwise
	 */
	public boolean inTransaction(RequestContext context, boolean end);

	/**
	 * Assert that there is an active application transaction in the flow
	 * execution and that the caller is participating in it.
	 * @param context the flow execution request context
	 * @param end indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException there is no active transaction in the
	 *         flow execution
	 */
	public void assertInTransaction(RequestContext context, boolean end) throws IllegalStateException;

	/**
	 * Start a new transaction in the flow execution.
	 * @param context the flow execution request context
	 */
	public void beginTransaction(RequestContext context);

	/**
	 * End the active transaction in the flow execution.
	 * @param context the flow execution request context
	 */
	public void endTransaction(RequestContext context);

}