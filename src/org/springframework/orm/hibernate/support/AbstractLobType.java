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

import net.sf.hibernate.UserType;
import net.sf.hibernate.util.EqualsHelper;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Abstract base class for Hibernate UserType implementations that map to LOBs.
 * Retrieves the LobHandler to use from LocalSessionFactoryBean at config time.
 *
 * <p>Offers template methods for getting and setting that pass in the LobHandler
 * respectively LobCreator to use.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setLobHandler
 */
public abstract class AbstractLobType implements UserType {

	private final LobHandler lobHandler;


	/**
	 * Constructor used by Hibernate: fetches config-time LobHandler
	 * from LocalSessionFactoryBean.
	 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#getConfigTimeLobHandler
	 */
	protected AbstractLobType() {
		this(LocalSessionFactoryBean.getConfigTimeLobHandler());
	}

	/**
	 * Constructor used for testing: takes an explicit LobHandler.
	 */
	protected AbstractLobType(LobHandler lobHandler) {
		if (lobHandler == null) {
			throw new IllegalStateException("No LobHandler found for configuration - " +
																			"lobHandler property must be set on LocalSessionFactoryBean");
		}
		this.lobHandler = lobHandler;
	}


	/**
	 * This implementation delegates to the Hibernate EqualsHelper.
	 * @see net.sf.hibernate.util.EqualsHelper#equals
	 */
	public boolean equals(Object x, Object y) {
		return EqualsHelper.equals(x, y);
	}

	/**
	 * This implementation returns the passed-in value as-is.
	 */
	public Object deepCopy(Object value) {
		return value;
	}

	/**
	 * This implementation returns false.
	 */
	public boolean isMutable() {
		return false;
	}


	/**
	 * This implementation delegates to nullSafeGetInternal,
	 * passing in the LobHandler of this type.
	 * @see #nullSafeGetInternal
	 */
	public final Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException {
		return nullSafeGetInternal(rs, rs.findColumn(names[0]), this.lobHandler);
	}

	/**
	 * This implementation delegates to nullSafeSetInternal,
	 * passing in a transaction-synchronized LobCreator for the
	 * LobHandler of this type.
	 * @see #nullSafeSetInternal
	 */
	public final void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("Active transaction synchronization required");
		}
		LobCreator lobCreator = this.lobHandler.getLobCreator();
		nullSafeSetInternal(st, index, value, lobCreator);
		TransactionSynchronizationManager.registerSynchronization(new LobCreatorSynchronization(lobCreator));
	}

	/**
	 * Template method to extract a value from the given result set.
	 * @param rs the ResultSet to extract from
	 * @param index the index in the ResultSet
	 * @param lobHandler the LobHandler to use
	 * @return the extracted value
	 * @throws SQLException if thrown by JDBC methods
	 */
	protected abstract Object nullSafeGetInternal(ResultSet rs, int index, LobHandler lobHandler)
			throws SQLException;

	/**
	 * Template method to set the given value on the given statement.
	 * @param ps the PreparedStatement to set on
	 * @param index the statement parameter index
	 * @param value the value to set
	 * @param lobCreator the LobCreator to use
	 * @throws SQLException if thrown by JDBC methods
	 */
	protected abstract void nullSafeSetInternal(PreparedStatement ps, int index, Object value, LobCreator lobCreator)
			throws SQLException;


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
