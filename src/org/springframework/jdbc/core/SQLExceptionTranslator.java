/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

/**
 * Interface to be implemented by classes that can translate
 * between SQLExceptions and our data access strategy-agnostic
 * org.springframework.dao.DataAccessException.
 *
 * <p>Implementations can be generic (for example, using SQLState
 * codes for JDBC) or proprietary (for example, using Oracle
 * error codes) for greater precision.
 *
 * @author Rod Johnson
 * @see org.springframework.dao.DataAccessException
 * @version $Id: SQLExceptionTranslator.java,v 1.1 2003-08-22 08:18:33 jhoeller Exp $
 */
public interface SQLExceptionTranslator {

	/** 
	 * Translate the given SQL exception into a generic
	 * data access exception.
	 * @param task readable text describing the task being attempted
	 * @param sql SQL query or update that caused the problem.
	 * May be null.
	 * @param sqlex SQLException encountered by JDBC implementation
	 */
	DataAccessException translate(String task, String sql, SQLException sqlex);

}
