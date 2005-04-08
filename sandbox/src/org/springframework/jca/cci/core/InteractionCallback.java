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

package org.springframework.jca.cci.core;

import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;

import org.springframework.dao.DataAccessException;

/**
 * Generic callback interface for code that operates on a CCI Interaction.
 * Allows to execute any number of operations on a single Interaction, for
 * example a single execute call or repeated execute calls with varying
 * parameters.
 * 
 * @author Thierry TEMPLIER
 */
public interface InteractionCallback {
	/**
	 * Gets called by CciTemplate.execute with an active CCI Interaction.
	 * Does not need to care about activating or closing the Connection, or
	 * handling transactions.
	 * 
	 * If called without a thread-bound CCI transaction (initiated by CciTransactionManager),
	 * the code will simply get executed on the CCI connection with its transactional
	 * semantics. If CciTemplate is configured to use a JTA-aware DataSource, the
	 * CCI connection and thus the callback code will be transactional if a JTA
	 * transaction is active.
	 * 
	 * Allows for returning a result object created within the callback, i.e. a domain
	 * object or a collection of domain objects.
	 *  
	 * @param interaction active CCI interaction
	 * @return a result object, or null if none
	 * @throws ResourceException
	 * @throws SQLException
	 * @throws DataAccessException
	 */
	public Object doInInteraction(CciSpecsHolder holder,Interaction interaction) throws ResourceException,SQLException,DataAccessException;
}
