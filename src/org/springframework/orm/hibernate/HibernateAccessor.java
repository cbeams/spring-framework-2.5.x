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

package org.springframework.orm.hibernate;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * Base class for HibernateTemplate and HibernateInterceptor, defining common
 * properties like SessionFactory and flushing behavior.
 *
 * <p>Not intended to be used directly. See HibernateTemplate and HibernateInterceptor.
 *
 * @author Juergen Hoeller
 * @since 29.07.2003
 * @see HibernateTemplate
 * @see HibernateInterceptor
 * @see #setFlushMode
 */
public abstract class HibernateAccessor implements InitializingBean {

	/**
	 * Never flush is a good strategy for read-only units of work.
	 * Hibernate will not track and look for changes in this case,
	 * avoiding any overhead of modification detection.
	 * @see #setFlushMode
	 */
	public static final int FLUSH_NEVER = 0;

	/**
	 * Automatic flushing is the default mode for a Hibernate session.
	 * A session will get flushed on transaction commit or session closing,
	 * and on certain find operations that might involve already modified
	 * instances, but not after each unit of work like with eager flushing.
	 * @see #setFlushMode
	 */
	public static final int FLUSH_AUTO = 1;

	/**
	 * Eager flushing leads to immediate synchronization with the database,
	 * even if in a transaction. This causes inconsistencies to show up and throw
	 * a respective exception immediately, and JDBC access code that participates
	 * in the same transaction will see the changes as the database is already
	 * aware of them then. But the drawbacks are:
	 * <ul>
	 * <li>additional communication roundtrips with the database, instead of a
	 * single batch at transaction commit;
	 * <li>the fact that an actual database rollback is needed if the Hibernate
	 * transaction rolls back (due to already submitted SQL statements).
	 * </ul>
	 * @see #setFlushMode
	 */
	public static final int FLUSH_EAGER = 2;


	protected final Log logger = LogFactory.getLog(getClass());

	/** Constants instance for HibernateAccessor */
	private static final Constants constants = new Constants(HibernateAccessor.class);

	private SessionFactory sessionFactory;

	private Interceptor entityInterceptor;

	private SQLExceptionTranslator jdbcExceptionTranslator;

	private int flushMode = FLUSH_AUTO;


	/**
	 * Set the Hibernate SessionFactory that should be used to create
	 * Hibernate Sessions.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the Hibernate SessionFactory that should be used to create
	 * Hibernate Sessions.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any <b>new</b> Session created by this object.
	 * <p>Such an interceptor can either be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean, or at the Session level, i.e. on
	 * HibernateTemplate, HibernateInterceptor, and HibernateTransactionManager.
	 * It's preferable to set it on LocalSessionFactoryBean or HibernateTransactionManager
	 * to avoid repeated configuration and guarantee consistent behavior in transactions.
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see HibernateTransactionManager#setEntityInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Return the current Hibernate entity interceptor, or null if none.
	 */
	public Interceptor getEntityInterceptor() {
		return entityInterceptor;
	}

	/**
	 * Set the JDBC exception translator for this instance.
	 * Applied to SQLExceptions thrown by callback code, be it direct
	 * SQLExceptions or wrapped Hibernate JDBCExceptions.
	 * <p>The default exception translator is either a SQLErrorCodeSQLExceptionTranslator
	 * if a DataSource is available, or a SQLStateSQLExceptionTranslator else.
	 * @param jdbcExceptionTranslator exception translator
	 * @see java.sql.SQLException
	 * @see net.sf.hibernate.JDBCException
	 * @see SessionFactoryUtils#newJdbcExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this instance.
	 * Creates a default one for the specified SessionFactory if none set.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		if (this.jdbcExceptionTranslator == null) {
			this.jdbcExceptionTranslator = SessionFactoryUtils.newJdbcExceptionTranslator(getSessionFactory());
		}
		return this.jdbcExceptionTranslator;
	}

	/**
	 * Set the flush behavior by the name of the respective constant
	 * in this class, e.g. "FLUSH_AUTO". Default is FLUSH_AUTO.
	 * Will get applied to any <b>new</b> Session created by this object.
	 * @param constantName name of the constant
	 * @see #setFlushMode
	 * @see #FLUSH_AUTO
	 */
	public void setFlushModeName(String constantName) {
		setFlushMode(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the flush behavior to one of the constants in this class.
	 * Default is FLUSH_AUTO. Will get applied to any <b>new</b> Session
	 * created by this object.
	 * @see #setFlushModeName
	 * @see #FLUSH_AUTO
	 */
	public void setFlushMode(int flushMode) {
		this.flushMode = flushMode;
	}

	/**
	 * Return if a flush should be forced after executing the callback code.
	 */
	public int getFlushMode() {
		return flushMode;
	}

	/**
	 * Eagerly initialize the exception translator, creating a default one
	 * for the specified SessionFactory if none set.
	 */
	public void afterPropertiesSet() {
		if (getSessionFactory() == null) {
			throw new IllegalArgumentException("sessionFactory is required");
		}
		getJdbcExceptionTranslator();
	}


	/**
	 * Flush the given Hibernate session if necessary.
	 * @param session the current Hibernate session
	 * @param existingTransaction if executing within an existing transaction
	 * @throws HibernateException in case of Hibernate flushing errors
	 */
	public void flushIfNecessary(Session session, boolean existingTransaction) throws HibernateException {
		if (getFlushMode() == FLUSH_EAGER || (!existingTransaction && getFlushMode() == FLUSH_AUTO)) {
			logger.debug("Eagerly flushing Hibernate session");
			session.flush();
		}
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from
	 * the org.springframework.dao hierarchy. Will automatically detect
	 * wrapped SQLExceptions and convert them accordingly.
	 * <p>The default implementation delegates to SessionFactoryUtils
	 * and convertJdbcAccessException. Can be overridden in subclasses.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #convertJdbcAccessException
	 * @see SessionFactoryUtils#convertHibernateAccessException
	 */
	public DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (ex instanceof JDBCException) {
			return convertJdbcAccessException(((JDBCException) ex).getSQLException());
		}
		else {
			return SessionFactoryUtils.convertHibernateAccessException(ex);
		}
	}

	/**
	 * Convert the given SQLException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Can be overridden in subclasses.
	 * <p>Note that SQLException can just occur here when callback code
	 * performs direct JDBC access via Session.connection().
	 * @param ex SQLException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 * @see net.sf.hibernate.Session#connection
	 */
	protected DataAccessException convertJdbcAccessException(SQLException ex) {
		return getJdbcExceptionTranslator().translate("Hibernate operation", null, ex);
	}

}
