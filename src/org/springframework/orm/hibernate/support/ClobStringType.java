package org.springframework.orm.hibernate.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.type.ImmutableType;
import net.sf.hibernate.util.EqualsHelper;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hibernate Type implementation for Strings that get mapped to CLOBs.
 * Retrieves the LobHandler to use from LocalSessionFactoryBean at config time.
 *
 * <p>Particularly useful for storing Strings with more than 4000 characters in an
 * Oracle database (only possible via CLOBs), in combination with OracleLobHandler.
 *
 * <p>Can also be defined in generic Hibernate mappings, as DefaultLobCreator will
 * work with most JDBC-compliant databases respectively drivers. In this case,
 * the field type does not have to be CLOB: For databases like MySQL, any large
 * enough text type will work.
 *
 * @author Juergen Hoeller
 * @since 12.01.2004
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setLobHandler
 * @see org.springframework.jdbc.support.lob.LobHandler
 */
public class ClobStringType extends ImmutableType {

	protected final LobHandler lobHandler;

	public ClobStringType() {
		this.lobHandler = LocalSessionFactoryBean.getConfigTimeLobHandler();
	}

	public Object get(ResultSet rs, String name) throws SQLException {
		return this.lobHandler.getClobAsString(rs, rs.findColumn(name));
	}

	public void set(PreparedStatement st, Object value, int index) throws SQLException {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("ClobStringType requires active transaction synchronization");
		}
		LobCreator lobCreator = this.lobHandler.getLobCreator();
		lobCreator.setClobAsString(st, index, (String) value);
		TransactionSynchronizationManager.registerSynchronization(new LobCreatorSynchronization(lobCreator));
	}


	public Class getReturnedClass() {
		return String.class;
	}

	public int sqlType() {
		return Types.CLOB;
	}

	public String getName() { return "string"; }

	public boolean equals(Object x, Object y) {
		return EqualsHelper.equals(x, y);
	}

	public String toString(Object value) {
		return (String) value;
	}

	public Object fromStringValue(String xml) {
		return xml;
	}


	/**
	 * Callback for resource cleanup at the end of a transaction.
	 * Invokes LobCreator.close to clean up temporary LOBs that might have been created.
	 * @see org.springframework.jdbc.support.lob.LobCreator#close
	 */
	private static class LobCreatorSynchronization implements TransactionSynchronization {

		private final LobCreator lobCreator;

		private LobCreatorSynchronization(LobCreator lobCreator) {
			this.lobCreator = lobCreator;
		}

		public void beforeCommit() {
		}

		public void beforeCompletion() {
			this.lobCreator.close();
		}

		public void afterCompletion(int status) {
		}
	}

}
