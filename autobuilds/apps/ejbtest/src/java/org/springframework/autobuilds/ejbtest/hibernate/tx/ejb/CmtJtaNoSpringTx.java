/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.autobuilds.ejbtest.hibernate.tx.ejb;

import org.springframework.dao.DataAccessException;

/**
 * <p>Business interface for EJB used to test proper session binding and unbinding in 
 * a CMT (Container Managed Transaction) environment with JTA, but no Spring
 * TransactionManager involved.</p>
 * 
 * <p>In this environment, as long as the Hibernate Configuration is set up with a 
 * TransactionManagerLookup so Hibernate (and Spring) can find the JTA
 * TransactionManager, Spring is still able to bind the Hibernate Session to the 
 * current transaction, and ensure that all Hibernate work in a transaction happens
 * within the same session (when using HibernateTemplate/SessionFactoryUtils.</p>
 * 
 * @author colin sampaleanu
 */
public interface CmtJtaNoSpringTx {

	public String echo(String input);

	public void testSameSessionReceivedInTwoRequests()
			throws TestFailureException;

	public void throwExceptionSoSessionUnbindCanBeVerified()
			throws DataAccessException;

}