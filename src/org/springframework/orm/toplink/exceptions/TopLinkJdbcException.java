/*
@license@
  */ 

package org.springframework.orm.toplink.exceptions;

import java.sql.SQLException;

import oracle.toplink.exceptions.DatabaseException;

import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.orm.toplink.SessionFactoryUtils;
import org.springframework.orm.toplink.TopLinkTemplate;

/**
 * Toplink-specific subclass of DataAccessException, for JDBC exceptions
 * that Toplink rethrew. Used by SessionFactoryUtils and ToplinkTemplate.
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see SessionFactoryUtils#convertToplinkAccessException
 * @see TopLinkTemplate#convertToplinkAccessException
 * @see org.springframework.dao.DataAccessException
 */
public class TopLinkJdbcException extends UncategorizedDataAccessException
{
	public TopLinkJdbcException(DatabaseException ex)
	{
		super("JDBC exception on Toplink data access: " + ex.getMessage(),
			ex.getInternalException());
	}

	public TopLinkJdbcException(SQLException ex)
	{
		super("Exception on direct JDBC access: " + ex.getMessage(), ex);
	}
}
