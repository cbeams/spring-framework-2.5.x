package org.springframework.orm.hibernate;

import java.sql.SQLException;

import net.sf.hibernate.JDBCException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Hibernate-specific subclass of DataAccessException, for JDBC exceptions
 * that Hibernate rethrew. Used by SessionFactoryUtils and HibernateTemplate.
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see SessionFactoryUtils#convertHibernateAccessException
 * @see HibernateTemplate#convertHibernateAccessException
 * @see org.springframework.dao.DataAccessException
 */
public class HibernateJdbcException extends UncategorizedDataAccessException {

	public HibernateJdbcException(JDBCException ex) {
		super("JDBC exception on Hibernate data access: " + ex.getMessage(), ex.getSQLException());
	}

	public HibernateJdbcException(SQLException ex) {
		super("Exception on direct JDBC access: " + ex.getMessage(), ex);
	}

}
