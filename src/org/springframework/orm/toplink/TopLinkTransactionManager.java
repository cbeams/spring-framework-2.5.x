/*
@license@
  */ 

package org.springframework.orm.toplink;

import java.sql.SQLException;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.orm.toplink.exceptions.TopLinkJdbcException;
import org.springframework.orm.toplink.sessions.SessionHolder;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>PlatformTransactionManager implementation for single Toplink session factories.
 * Binds a Toplink Session from the specified factory to the thread, potentially
 * allowing for one thread Session per factory. ToplinkUtils and
 * ToplinkTemplate are aware of thread-bound Sessions and participate in such
 * transactions automatically. Using either is required for Toplink access code
 * that needs to support this transaction handling mechanism.
 *
 *<p>This implementation of PlatformTransactionManager is used when only one TopLink
 * Session is involved in global Transaction.  Any number of Data Access beans might participate
 * in this transaction but they will each access the same TopLink Session (from the same 
 * SessionFactoryBean)
 *
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @version $Revision: 1.1 $ $Date: 2005-04-20 23:20:30 $
 */
public class TopLinkTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean
{
	private SessionFactory sessionFactory;
	private SQLExceptionTranslator jdbcExceptionTranslator = new SQLStateSQLExceptionTranslator();

	/**
	 * Create a new ToplinkTransactionManager instance.
	 * A Session has to be set to be able to use it.
	 */
	public TopLinkTransactionManager()
	{
	}

	/**
	 * Create a new ToplinkTransactionManager instance.
	 * @param session Session to manage transactions for
	 */
	public TopLinkTransactionManager(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
		afterPropertiesSet();
	}

	/**
	 * Set the Session that this instance should manage transactions for.
	 */
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the SessionFactory that this instance should manage transactions for.
	 */
	public SessionFactory getSessionFactory()
	{
		return this.sessionFactory;
	}

	/**
	 * Set the JDBC exception translator for this transaction manager.
	 * Applied to SQLExceptions (wrapped by Toplink's DatabaseException)
	 * thrown by flushing on commit.
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
	 * Return the JDBC exception translator for this transaction manager.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator()
	{
		return this.jdbcExceptionTranslator;
	}

	public void afterPropertiesSet()
	{
		if (this.sessionFactory == null)
		{
			throw new IllegalArgumentException("TopLinkSessionFactory is required");
		}
	}

	/**
	 * Check the current Thread for a Session from this SessionFactory.  
	 * @return Object will contain a reference to the current SessionHolder if one is bound to the Thread
	 */
	protected Object doGetTransaction()
	{
		if (TransactionSynchronizationManager.hasResource(this.sessionFactory))
		{
			SessionHolder sessionHolder = (SessionHolder)
				TransactionSynchronizationManager.getResource(this.sessionFactory);
			logger.debug("Found thread-bound session [" +
				sessionHolder.getSession() + "] for Toplink transaction");
			// TopLinkTransactionObject will set newSessionHolder=false
			return new TransactionObject(sessionHolder);
		}
		else
		{
		    logger.debug("No thread-bound Session found.  Create empty TransactionObject");
			return new TransactionObject();
		}
	}

	/**
	 * Check whether the current Transaction already has a SessionHolder and that
	 * it has an active UnitOfWork
	 */
	protected boolean isExistingTransaction(Object transaction)
	{
	    return ((TransactionObject)transaction).hasTransaction();
	}

	/**
	 * doBegin is called when the Propogations settings determine that a new Transaction is required.
	 * This either occurs when when a brand new TopLink Session is being created or when an existing one has just
	 * been suspended.
	 * 
	 * Insure that there is an active Session bound to the current Thread.
	 */
	protected void doBegin(Object transaction, TransactionDefinition definition)
	{
		TransactionObject txObject = (TransactionObject) transaction;

		// cache to avoid repeated checks
		boolean debugEnabled = logger.isDebugEnabled();

		if (txObject.getSessionHolder() == null)
		{
		    // always returns a Session
		    // if no Session is bound to the current Thread, then we will create
		    // a Session but will NOT bind it to the current Thread
		    // Synchronization NOT allowed here
		    // 		synchronizationAllowed=false
		    //		allowCreate = true;
			Session session = SessionFactoryUtils.getSession(
				this.sessionFactory, false,true);
			if (debugEnabled)
			{
				logger.debug("Opened new session [" + session + "] for Toplink transaction");
			}
			
			txObject.setSessionHolder(new SessionHolder(session));
		}

		try
		{
		    // we bind this SessionHolder to the Thread below
		    // there might not be a SynchronizationListener registered
			txObject.getSessionHolder().setSynchronizedWithTransaction(true);
			Session session = txObject.getSessionHolder().getSession();
			if (debugEnabled)
			{
				logger.debug("Beginning Toplink transaction on session [" + session + "]");
			}

			// apply read-only
			if (txObject.isNewSessionHolder() && definition.isReadOnly())
			{
			    logger.debug("this transaction is marked read-only so the UOW will not commit");
			}

			// apply isolation level
			switch(definition.getIsolationLevel())
			{
				case TransactionDefinition.ISOLATION_READ_UNCOMMITTED:
				{
				    // TODO warn when queries are executed without the conformResultsInUnitOfWork setting 
				    break;
				}
				case TransactionDefinition.ISOLATION_REPEATABLE_READ:
				{
				    // TODO warn when queries are executed against a read-only Session
				    break;
				}
				case TransactionDefinition.ISOLATION_SERIALIZABLE:
				{
				    // TODO warn if the TransactionIsolation settings on the DatabaseLogin are wrong
				    break;
				}
			}

			// register transaction timeout
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT)
			{
				txObject.getSessionHolder().setTimeoutInSeconds(definition.getTimeout());
			}
			
			// 
			txObject.getSessionHolder().transactionStarted();

			// bind the session holder to the thread if we need to
			if (txObject.isNewSessionHolder())
			{
				TransactionSynchronizationManager.bindResource(this.sessionFactory,
					txObject.getSessionHolder());
			}
		}
		catch (TopLinkException ex)
		{
			throw new CannotCreateTransactionException("Could not create Toplink transaction", ex);
		}
	}

	/**
	 * Remove the SessionHolder from the TransactionObject and create a
	 * holder for the suspended resource. 
	 */
	protected Object doSuspend(Object transaction)
	{
		TransactionObject txObject = (TransactionObject) transaction;
		// nulling out the SessionHolder will also set "newSessionHolder" flag to false
		txObject.setSessionHolder(null);
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		
		return sessionHolder;
	}

	/**
	 * re-bind the SessionHolder from the suspended Transaction to the
	 * current Thread.
	 */
	protected void doResume(Object transaction, Object suspendedResources)
	{
		SessionHolder sessionHolder = (SessionHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(this.sessionFactory))
		{
			// from non-transactional code running in active transaction synchronization
			// -> can be safely removed, will be closed on transaction completion
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}
		TransactionSynchronizationManager.bindResource(this.sessionFactory,
			sessionHolder);
	}

	protected boolean isRollbackOnly(Object transaction)
	{
		return ((TransactionObject) transaction).getSessionHolder().isRollbackOnly();
	}

	/**
	 * Commit the UnitOfWork bound to the current Thread.
	 */
	protected void doCommit(DefaultTransactionStatus status)
	{
		TransactionObject txObject =
			(TransactionObject) status.getTransaction();
		if (status.isDebug())
		{
			logger.debug("Committing Toplink transaction on session [" +
				txObject.getSessionHolder().getSession() + "]");
		}
		try
		{
		    if (!status.isReadOnly())
		    {
				UnitOfWork uow = (UnitOfWork)txObject.getSessionHolder().getTransaction();
				uow.commit();
		    }

		    txObject.getSessionHolder().resetTransaction();
		    txObject.getSessionHolder().clear();
		}
		catch (DatabaseException ex)
		{
			throw convertJdbcAccessException(
				(SQLException) ex.getInternalException());
		}
		catch (TopLinkException ex)
		{
			throw convertToplinkAccessException(ex);
		}
	}

	/**
	 * When using commit(), TopLink will have properly rolled back the Transaction
	 * by this time.  There is nothing left to do here.
	 */
	protected void doRollback(DefaultTransactionStatus status)
	{
	    logger.debug("operation on current uow has been rolled back");
	    TransactionObject txObject = (TransactionObject)status.getTransaction();
	    txObject.getSessionHolder().resetTransaction();
	    txObject.getSessionHolder().clear();
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status)
	{
		TransactionObject txObject =
			(TransactionObject) status.getTransaction();
		if (status.isDebug())
		{
			logger.debug("Setting Toplink transaction on Session [" +
				txObject.getSessionHolder().getSession() + "] rollback-only");
		}
		txObject.getSessionHolder().setRollbackOnly();
	}

	/**
	 * There might be a TransactionSynchronization listener registered
	 * to do cleanup but if this is a new SessionHolder then we will have
	 * skipped registering the listener.  In this case, unbind resources 
	 * close TopLink session here.  
	 */
	protected void doCleanupAfterCompletion(Object transaction)
	{
		TransactionObject txObject = (TransactionObject) transaction;

		// remove the session holder from the thread
		if (txObject.isNewSessionHolder())
		{
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		Session session = txObject.getSessionHolder().getSession();
		if (txObject.isNewSessionHolder())
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Closing Toplink session [" + session + "] after transaction");
			}
			try
			{
				SessionFactoryUtils.closeSessionIfNecessary(session, this.sessionFactory);
			}
			catch (CleanupFailureDataAccessException ex)
			{
				// just log it, to keep a transaction-related exception
				logger.error("Count not close Toplink session after transaction", ex);
			}
		}
		else
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Not closing pre-bound Toplink session [" +
					session + "] after transaction");
			}
		}
	}

	/**
	 * Convert the given ToplinkException to an appropriate exception from
	 * the org.springframework.dao hierarchy. Can be overridden in subclasses.
	 * @param ex ToplinkException that occured
	 * @return the corresponding DataAccessException instance
	 */
	protected DataAccessException convertToplinkAccessException(
		TopLinkException ex)
	{
		return SessionFactoryUtils.convertToplinkAccessException(ex);
	}

	/**
	 * Convert the given SQLException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Uses a JDBC exception translater if set,
	 * and a generic ToplinkJdbcException else. Can be overridden in subclasses.
	 * @param ex SQLException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 */
	protected DataAccessException convertJdbcAccessException(SQLException ex)
	{
		if (this.jdbcExceptionTranslator != null)
		{
			return this.jdbcExceptionTranslator.translate("ToplinkTemplate", null, ex);
		}
		else
		{
			return new TopLinkJdbcException(ex);
		}
	}
	
	/**
	 * <p>This class is not intended to be used by applications.  It is used by the 
	 * TopLinkTransactionManager to access the currently active SessionHolder.
	 * 
	 * It allows the TopLinkTransactionManger to track whether or not a Session has been created
	 * specifically for the current TopLink Transaction, and whether or not the Session has an active
	 * UnitOfWork.
	 * 
	 * It's actualy important to extend JdbcTransactionObjectSupport because the AbstractTransactionManager
	 * uses this to determine whether or not to rollback participating transactions.
	 *
	 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
	 */
	private static class TransactionObject extends JdbcTransactionObjectSupport
	{
		private SessionHolder sessionHolder;
		private boolean newSessionHolder;

		/**
		 * Create ToplinkTransactionObject when there is no SessionHolder bound to the current Thread.
		 */
		protected TransactionObject()
		{
		}

		/**
		 * Create ToplinkTransactionObject for existing SessionHolder already bound to the current Thread.
		 */
		protected TransactionObject(SessionHolder sessionHolder)
		{
			this.sessionHolder = sessionHolder;
			this.newSessionHolder = false;
		}

		/**
		 * Set a new SessionHolder for this Transaction 
		 */
		protected void setSessionHolder(SessionHolder sessionHolder)
		{
			this.sessionHolder = sessionHolder;
			this.newSessionHolder = (sessionHolder != null);
		}

		public SessionHolder getSessionHolder()
		{
			return sessionHolder;
		}

		/**
		 * Check whether or not this TransactionObject represents a Session which was already bound
		 * to the Thread when this Transaction began, or represents a Session which was created
		 * specifically for this Transaction.
		 */
		public boolean isNewSessionHolder()
		{
			return newSessionHolder;
		}

		/**
		 * Check whether this SessionHolder has an active UnitOfWork.  This check is used by the 
		 * TopLinkTransactionManager when determining how to handle propogation levels when
		 * new Transactions are created.
		 */
		public boolean hasTransaction()
		{
			return (sessionHolder != null && 
		        sessionHolder.hasTransaction());
		}

        public boolean isRollbackOnly()
        {
			return getSessionHolder().isRollbackOnly();
        }
	}

}
