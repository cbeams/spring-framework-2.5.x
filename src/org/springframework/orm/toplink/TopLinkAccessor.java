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

import java.sql.SQLException;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.TopLinkException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

/**
 * Base class for TopLinkTemplate and TopLinkInterceptor, defining common properties
 * like SessionFactory and JDBC exception translator.
 *
 * <p>Not intended to be used directly. See TopLinkTemplate and TopLinkInterceptor.
 *
 * <p>Thanks to Slavik Markovich for implementing the initial TopLink support prototype!
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see TopLinkTemplate
 * @see TopLinkInterceptor
 */
public abstract class TopLinkAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private SessionFactory sessionFactory;

	private SQLExceptionTranslator jdbcExceptionTranslator;


	/**
	 * Set the the TopLink SessionFactory that should be used to create TopLink
	 * Sessions. This will usually be a ServerSessionFactory in a multi-threaded
	 * environment, but can also be a SingleSessionFactory for testing purposes
	 * or for standalone execution.
	 * <p>The passed-in SessionFactory will usually be asked for a plain Session
	 * to perform data access on, unless an active transaction with a thread-bound
	 * Session is found.
	 * @see ServerSessionFactory
	 * @see SingleSessionFactory
	 * @see SessionFactory#createSession()
	 * @see SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the TopLink SessionFactory that should be used to create
	 * TopLink Sessions.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set the JDBC exception translator for this instance.
	 * Applied to TopLink DatabaseExceptions thrown by callback code.
	 * <p>The default exception translator is a SQLStateSQLExceptionTranslator.
	 * @param jdbcExceptionTranslator the exception translator
	 * @see oracle.toplink.exceptions.DatabaseException
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this instance.
	 * <p>Creates a default SQLStateSQLExceptionTranslator,
	 * if no exception translator explicitly specified.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		if (this.jdbcExceptionTranslator == null) {
			this.jdbcExceptionTranslator = new SQLStateSQLExceptionTranslator();
		}
		return this.jdbcExceptionTranslator;
	}


	/**
	 * Check that we were provided with a session to use
	 */
	public void afterPropertiesSet() {
		if (this.sessionFactory == null) {
			throw new IllegalArgumentException("sessionFactory is required");
		}
	}


	/**
	 * Convert the given TopLinkException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Will automatically detect
	 * wrapped SQLExceptions and convert them accordingly.
	 * <p>The default implementation delegates to TopLinkUtils
	 * and convertJdbcAccessException. Can be overridden in subclasses.
	 * @param ex TopLinkException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #convertJdbcAccessException
	 * @see SessionFactoryUtils#convertTopLinkAccessException
	 */
	public DataAccessException convertTopLinkAccessException(TopLinkException ex) {
		// This is a database exception
		if (ex instanceof DatabaseException) {
			Throwable internalEx = ex.getInternalException();
			if (internalEx != null && internalEx instanceof SQLException) {
				return convertJdbcAccessException((SQLException) internalEx);
			}
		}
		return SessionFactoryUtils.convertTopLinkAccessException(ex);
	}

	/**
	 * Convert the given SQLException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Can be overridden in subclasses.
	 * @param ex SQLException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 */
	protected DataAccessException convertJdbcAccessException(SQLException ex) {
		return getJdbcExceptionTranslator().translate("TopLinkAccessor", null, ex);
	}

}
