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

package org.springframework.orm.toplink;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;

/**
 * Callback interface for TopLink code. To be used with TopLinkTemplate's execute
 * method, assumably often as anonymous classes within a method implementation.
 * The typical implementation will call TopLink Session CRUD to perform some
 * operations on persistent objects.
 *
 * <p>The Session that is passed into the <code>doInTopLink</code> method is usually
 * a thread-safe ClientSession. Since this provides access to the TopLink shared cache,
 * it is possible for implementations of this interface to return references to
 * <i>read-only objects from the shared cache</i>. These objects <i>must not be modified</i>
 * by application code outside of the DAO layer. If persistent objects need to be edited,
 * then they should be loaded from or registered with a TopLink UnitOfWork, or they should
 * be explicitly copied and merged back into a UnitOfWork at a later time.
 *
 * <p>Users can access a UnitOfWork by using the <code>getActiveUnitOfWork</code> method
 * on the <code>Session</code>. Normally, this will only be done when there is an active
 * non-read-only transaction being managed by Spring's TopLinkTransactionManager or by
 * an external transaction controller (usually a J2EE server's JTA provider, configured
 * in TopLink). <code>getActiveUnitOfWork</code> will return null outside a transaction.
 *
 * @author Juergen Hoeller
 * @author <a href="mailto:@james.x.clark@oracle.com">James Clark</a>
 * @see TopLinkTemplate
 * @see TopLinkTransactionManager
 */
public interface TopLinkCallback {

	/**
	 * Gets called by <code>TopLinkTemplate.execute</code> with an active Session.
	 * Does not need to care about activating or closing the TopLink Session,
	 * or handling transactions.
	 *
	 * <p>Note that write operations should usually be performed on the active
	 * UnitOfWork within an externally controlled transaction, through calling
	 * <code>getActiveUnitOfWork</code>. However, an implementation can also choose
	 * to use <code>acquireUnitOfWork</code> to create an independent UnitOfWork,
	 * which it needs to commit or release at the end of the operation.
	 *
	 * <p>Allows for returning a result object created within the callback,
	 * i.e. a domain object or a collection of domain objects.
	 * A thrown RuntimeException is treated as application exception,
	 * it gets propagated to the caller of the template.
	 *
	 * @param session active TopLink Session
	 * @return a result object, or <code>null</code> if none
	 * @throws TopLinkException in case of TopLink errors
	 * @see oracle.toplink.sessions.Session#getActiveUnitOfWork()
	 * @see oracle.toplink.sessions.Session#acquireUnitOfWork()
	 * @see TopLinkTemplate#execute
	 * @see TopLinkTransactionManager
	 */
	Object doInTopLink(Session session) throws TopLinkException;

}
