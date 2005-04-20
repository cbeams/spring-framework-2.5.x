/*
@license@
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
import org.springframework.orm.toplink.exceptions.TopLinkJdbcException;

/**
 * Base class for ToplinkTemplate and ToplinkInterceptor, defining common
 * properties.
 *
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @since 15.04.2004
 * @see TopLinkTemplate
 * @see TopLinkInterceptor
 */
public abstract class TopLinkAccessor implements InitializingBean
{
	protected final Log logger = LogFactory.getLog(getClass());
	/**
	 * The session set externally to be used for accessing the db.
	 */
	private SessionFactory sessionFactory;
	/**
	 * Exception translator
	 */
	private SQLExceptionTranslator jdbcExceptionTranslator =
		new SQLStateSQLExceptionTranslator();


	/**
	 * Set the JDBC exception translator for this instance.
	 * Applied to SQLExceptions thrown by callback code, be it direct
	 * SQLExceptions or wrapped Toplink DatabseExceptions.
	 * <p>The default exception translator evaluates the exception's SQLState.
	 * @param jdbcExceptionTranslator exception translator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator)
	{
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this instance.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator()
	{
		return this.jdbcExceptionTranslator;
	}

	/**
	 * Check that we were provided with a session to use
	 */
	public void afterPropertiesSet()
	{
		if (this.sessionFactory == null)
		{
			throw new IllegalArgumentException("Toplink sessionFactory is required");
		}
	}

	/**
	 * Convert the given ToplinkException to an appropriate exception from
	 * the org.springframework.dao hierarchy. Will automatically detect
	 * wrapped SQLExceptions and convert them accordingly.
	 * <p>The default implementation delegates to ToplinkUtils
	 * and convertJdbcAccessException. Can be overridden in subclasses.
	 * @param ex ToplinkException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #convertJdbcAccessException
	 * @see SessionFactoryUtils#convertToplinkAccessException
	 */
	public DataAccessException convertToplinkAccessException(TopLinkException ex)
	{
		// This is a database exception
		if (ex instanceof DatabaseException)
		{
			Throwable internalEx = ex.getInternalException();
			if (internalEx != null && internalEx instanceof SQLException)
			{
				return convertJdbcAccessException((SQLException) internalEx);
			}
		}
		return SessionFactoryUtils.convertToplinkAccessException(ex);
	}

	/**
	 * Convert the given SQLException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Uses a JDBC exception translater if set,
	 * and a generic ToplinkJdbcException else. Can be overridden in subclasses.
	 * <p>Note that SQLException can just occur here when callback code
	 * performs direct JDBC access via Session.connection().
	 * @param ex SQLException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 */
	protected DataAccessException convertJdbcAccessException(SQLException ex)
	{
		if (this.jdbcExceptionTranslator != null)
		{
			return this.jdbcExceptionTranslator.translate("ToplinkAccessor", null, ex);
		}
		else
		{
			return new TopLinkJdbcException(ex);
		}
	}
    
	/**
     * @return Returns the sessionFactory.
     */
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
    
    /**
     * @param sessionFactory The sessionFactory to set.
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}