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

package org.springframework.orm.hibernate.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.UserType;
import net.sf.hibernate.util.EqualsHelper;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hibernate UserType implementation for Strings that get mapped to CLOBs.
 * Retrieves the LobHandler to use from LocalSessionFactoryBean at config time.
 *
 * <p>Particularly useful for storing Strings with more than 4000 characters in an
 * Oracle database (only possible via CLOBs), in combination with OracleLobHandler.
 *
 * <p>Can also be defined in generic Hibernate mappings, as DefaultLobCreator will
 * work with most JDBC-compliant databases respectively drivers. In this case,
 * the field type does not have to be CLOB: For databases like MySQL and MS SQL
 * Server, any large enough text type will work.
 *
 * @author Juergen Hoeller
 * @since 12.01.2004
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setLobHandler
 * @see org.springframework.jdbc.support.lob.LobHandler
 */
public class ClobStringType implements UserType {

	protected final LobHandler lobHandler;

	public ClobStringType() {
		this.lobHandler = LocalSessionFactoryBean.getConfigTimeLobHandler();
	}

	public int[] sqlTypes() {
		return new int[] {Types.CLOB};
	}

	public Class returnedClass() {
		return String.class;
	}

	public boolean equals(Object x, Object y) {
		return EqualsHelper.equals(x, y);
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException {
		if (this.lobHandler == null) {
			throw new IllegalStateException("No LobHandler found for configuration - " +
																			"lobHandler property must be set on LocalSessionFactoryBean");
		}
		return this.lobHandler.getClobAsString(rs, rs.findColumn(names[0]));
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException {
		if (this.lobHandler == null) {
			throw new IllegalStateException("No LobHandler found for configuration - " +
																			"lobHandler property must be set on LocalSessionFactoryBean");
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("ClobStringType requires active transaction synchronization");
		}
		LobCreator lobCreator = this.lobHandler.getLobCreator();
		lobCreator.setClobAsString(st, index, (String) value);
		TransactionSynchronizationManager.registerSynchronization(new LobCreatorSynchronization(lobCreator));
	}

	public Object deepCopy(Object value) {
		return value;
	}

	public boolean isMutable() {
		return false;
	}


	/**
	 * Callback for resource cleanup at the end of a transaction.
	 * Invokes LobCreator.close to clean up temporary LOBs that might have been created.
	 * @see org.springframework.jdbc.support.lob.LobCreator#close
	 */
	private static class LobCreatorSynchronization extends TransactionSynchronizationAdapter {

		private final LobCreator lobCreator;

		private LobCreatorSynchronization(LobCreator lobCreator) {
			this.lobCreator = lobCreator;
		}

		public void beforeCompletion() {
			this.lobCreator.close();
		}
	}

}
