/*
@license@
  */ 

package org.springframework.orm.toplink;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import oracle.toplink.exceptions.ConcurrencyException;
import oracle.toplink.exceptions.ConversionException;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.OptimisticLockException;
import oracle.toplink.exceptions.QueryException;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.jts.AbstractExternalTransactionController;
import oracle.toplink.queryframework.DatabaseQuery;
import oracle.toplink.sessions.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.orm.toplink.exceptions.TopLinkJdbcException;
import org.springframework.orm.toplink.exceptions.TopLinkOptimisticLockingFailureException;
import org.springframework.orm.toplink.exceptions.TopLinkQueryException;
import org.springframework.orm.toplink.exceptions.TopLinkSystemException;
import org.springframework.orm.toplink.exceptions.TopLinkTypeMismatchDataAccessException;
import org.springframework.orm.toplink.sessions.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Utility methods used by the TopLinkInterceptor and the TopLinkTemplate to access 
 * the Thread-bound Session for a specific SessionFactory. 
 * 
 * These methods deal with ensuring that Sessions are properly bound to the current Thread
 * and that Resource cleanup is properly handled by either Spring Synchronization or JTA
 * Synchronization Objects (usually Spring ones).  
 * 
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 *
 */
public abstract class SessionFactoryUtils
{
	private static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);

	/**
	 * <p>Get the Thread-bound Session for a SessionFactory.  Only create a new Session if the allowCreate
	 * parameter is true.  If a new Session is created, the Session will be bound to the Thread and will register
	 * a SynchronizationListener if there is an active PlatformTransactionManager which is initialized to accept 
	 * Syncrhonization Listeners.
	 * 
	 * @param sessionFactory
	 * @param allowCreate if a new Session should be created when no Thread-bound one is found
	 * @return
	 * @throws DataAccessResourceFailureException
	 * @throws IllegalStateException
	 */
	public static Session getSession(SessionFactory sessionFactory, boolean allowCreate)
		throws DataAccessResourceFailureException, IllegalStateException
	{
		return getSession(sessionFactory, true, allowCreate);
	}

	/**
	 * <p>Get the Thread-bound Session for a specific SessionFactory.  This method creates a new Session if one
	 * is not currently bound.  This new Session will only be bound to the Thread if the allowSynchronization argument 
	 * is true.
	 * 
	 * <p>An example of code creating a new Session but not binding it to the current Thread would be when a 
	 * TopLinkTransactionManager creates a new TransactionObject. and then applies some Transaction specific 
	 * settings before handling the Synchronization itself.  
	 * 
	 * @param sessionFactory
	 * @param jdbcExceptionTranslator
	 * @param allowSynchronization determines whether a new SessionHolder is bound to the current Thread
	 * @return
	 * @throws DataAccessResourceFailureException
	 */
	public static Session getSession(SessionFactory sessionFactory,
		boolean allowSynchronization, boolean allowCreate)
			throws DataAccessResourceFailureException
	{
		SessionHolder sessionHolder = (SessionHolder)
			TransactionSynchronizationManager.getResource(sessionFactory);
		
		if (sessionHolder != null)
		{
		    logger.debug("found SessionHolder bound to Session");
		    
		    if(TransactionSynchronizationManager.isSynchronizationActive())
		    {
				if (allowSynchronization && !sessionHolder.isSynchronizedWithTransaction())
				{
				    // TopLinkTransactionManager
				    logger.debug("no Synchronization registered.  Registering new Synchronization with existing Session.");
				    TransactionSynchronizationManager.registerSynchronization(
							new SpringSessionSynchronization(
							        sessionHolder,
							        sessionFactory,
							        null,
							        false));
								
					sessionHolder.setSynchronizedWithTransaction(true);
				}
				else
				{
				    // probabaly JTA is active
				    logger.debug("not registered synchronization for pre-bound Session");
				}
		    }
			return sessionHolder.getSession();
		}

		if (!allowCreate)
		{
			throw new IllegalStateException(
					"No Toplink Session bound to thread, and configuration " +
					"does not allow creation of new one here");
		}

		logger.debug("Opening Toplink session");
		
		try
		{
			// If this is a server session, use it to acquire client session
			// else, just use the session (it is a database session)
			Session session = sessionFactory.createSession();

			if (allowSynchronization)
			{
				// Use same Session for further Toplink actions within the transaction.
				// Thread object will get removed by synchronization at transaction completion.
				if (TransactionSynchronizationManager.isSynchronizationActive())
				{
					// We're within a Spring-managed transaction, possibly from JtaTransactionManager.
					logger.debug("Registering new Spring transaction synchronization for new Toplink session");
					sessionHolder = new SessionHolder(session);
				    //oracle.toplink.publicinterface.UnitOfWork uowImpl = (oracle.toplink.publicinterface.UnitOfWork)sessionHolder.getSession().getActiveUnitOfWork();
				    //oracle.toplink.publicinterface.Session sessionImpl = (oracle.toplink.publicinterface.Session)sessionHolder.getSession();
					
				    TransactionSynchronizationManager.registerSynchronization(
						new SpringSessionSynchronization(
						        sessionHolder,
						        sessionFactory,
						        null,
						        true));
				    TransactionSynchronizationManager.bindResource(sessionFactory,
						sessionHolder);
			    
					sessionHolder.setSynchronizedWithTransaction(true);
				}
				else if (session.hasExternalTransactionController())
				{
				    // no Spring TransactionManager is active but there is still a JTA TM controlling the tx
				    logger.debug("register a JTA Synchronization to clean up Thread-bound resources");
				    AbstractExternalTransactionController etc = (AbstractExternalTransactionController)session.getExternalTransactionController();

					sessionHolder = new SessionHolder(session);

					try
					{
					    Transaction transaction = (Transaction)etc.getExternalTransaction();
					    transaction.registerSynchronization(new JTASessionSynchronization(sessionFactory,sessionHolder,true));
						sessionHolder.setSynchronizedWithTransaction(true);
					    TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
					    sessionHolder.setSynchronizedWithTransaction(true);
					}
					catch(Exception e)
					{
					    logger.error("unable to register listener with JTA Transaction",e);
						throw new DataAccessResourceFailureException("Could not register synchronization " +
								 "with JTA TransactionManager", e);
					}
				}
				else
				{
				    logger.debug("not registering synchronization because synchronizations are not initialized");
				}
			}
			else
			{
			    logger.debug("not registering Synchronization or binding SessionHolder to Thread");
			}
			return session;
		}
		catch (DatabaseException ex)
		{
			// SQLException underneath
			throw new DataAccessResourceFailureException("Could not open Toplink session",
				ex.getInternalException());
		}
		catch (TopLinkException ex)
		{
			throw new DataAccessResourceFailureException("Could not open Toplink session", ex);
		}
	}

	/**
	 * Apply the current transaction timeout, if any, to the given
	 * Toplink Query object.
	 * @param query the Toplink Query object
	 * @param session Toplink Session that the Query was created for
	 */
	public static void applyTransactionTimeout(DatabaseQuery query,
		Session session)
	{
		SessionHolder sessionHolder = (SessionHolder)
			TransactionSynchronizationManager.getResource(session);
		if (sessionHolder != null && sessionHolder.getDeadline() != null)
		{
			query.setQueryTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Convert the given TopLinkException to an appropriate exception from the
	 * org.springframework.dao hierarchy.
	 * @param ex TopLinkException that occured
	 * @return the corresponding DataAccessException instance
	 */
	public static DataAccessException convertToplinkAccessException(TopLinkException ex)
	{
		if (ex instanceof DatabaseException)
		{
			// SQLException during Toplink access: only passed in here from custom code,
			// as ToplinkTemplate will use SQLExceptionTranslator-based handling
			return new TopLinkJdbcException((DatabaseException) ex);
		}
		if (ex instanceof ConcurrencyException)
		{
		    return new TopLinkSystemException(ex);
		}
		if (ex instanceof ConversionException)
		{
		    return new TopLinkTypeMismatchDataAccessException(ex.getMessage(),ex);
		}
		if (ex instanceof QueryException)
		{
			return new TopLinkQueryException((QueryException) ex);
		}
		if (ex instanceof OptimisticLockException)
		{
			return new TopLinkOptimisticLockingFailureException((OptimisticLockException) ex);
		}
		// fallback
		return new TopLinkSystemException(ex);
	}

	/**
	 * This is called during cleanup at the end of using a TopLinkTemplate, a TopLinkInterceptor, or
	 * at the end of a managed Transaction.  It is important in cases where the current Session is not
	 * bound or synchronized to an external transaction.  For these cases, the SessionManagement code 
	 * must both acquire and commit the underlying UnitOfWork.  This method determines whether or not
	 * the Session is externally managed and if not, delegates the "closing" operation to the Session itself.
	 */
	public static void closeSessionIfNecessary(Session session, SessionFactory sessionFactory)
	    throws CleanupFailureDataAccessException
	{
		if (session == null || TransactionSynchronizationManager.hasResource(sessionFactory))
		{
			return;
		}
		logger.debug("Closing Toplink session "+session);
		try
		{
		    //session.getActiveUnitOfWork().release();
			session.release();
		}
		catch (DatabaseException ex)
		{
			// SQLException underneath
			throw new CleanupFailureDataAccessException("Could not close Toplink session",
				ex.getInternalException());
		}
		catch (TopLinkException ex)
		{
			throw new CleanupFailureDataAccessException("Could not close Toplink session", ex);
		}
	}

	/**
	 * Callback for resource cleanup at the end of a Spring-managed JTA transaction,
	 * i.e. when participating in a JtaTransactionManager transaction or a TopLinkTransactionManager
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class SpringSessionSynchronization implements TransactionSynchronization
	{
		private final SessionHolder sessionHolder;
		private final SessionFactory sessionFactory;
		private final SQLExceptionTranslator jdbcExceptionTranslator;
		private final boolean newSession;

		private SpringSessionSynchronization(SessionHolder sessionHolder,
			SessionFactory sessionFactory,
		    SQLExceptionTranslator jdbcExceptionTranslator,
			boolean newSession)
		{
			this.sessionHolder = sessionHolder;
			this.sessionFactory = sessionFactory;
			this.jdbcExceptionTranslator = jdbcExceptionTranslator;
			this.newSession = newSession;
		}

		public void suspend()
		{
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		public void resume()
		{
			TransactionSynchronizationManager.bindResource(this.sessionFactory,
				this.sessionHolder);
		}

		public void beforeCommit(boolean readOnly) throws DataAccessException
		{
		}

		public void beforeCompletion() throws CleanupFailureDataAccessException
		{
			if (this.newSession)
			{
				TransactionSynchronizationManager.unbindResource(this.sessionFactory);
			}
		}

		public void afterCompletion(int status)
		{
			Session session = this.sessionHolder.getSession();
			if (this.newSession)
			{
				closeSessionIfNecessary(session, sessionFactory);
			}
			this.sessionHolder.setSynchronizedWithTransaction(false);
			this.sessionHolder.clear();
		    this.sessionHolder.resetTransaction();
		}
	}

	/**
	 * Callback for resource cleanup at the end of a non-Spring JTA transaction,
	 * i.e. when plain JTA or EJB CMT is used without Spring's JtaTransactionManager.
	 */
	private static class JTASessionSynchronization implements Synchronization {
	    
	    private final SessionFactory sessionFactory;
	    private final SessionHolder sessionHolder;
	    private final boolean newSession;


		private JTASessionSynchronization(SessionFactory sessionFactory, SessionHolder sessionHolder, boolean newSession)
		{
		    this.sessionFactory = sessionFactory;
		    this.sessionHolder = sessionHolder;
		    this.newSession = newSession;
		}

		public void beforeCompletion() {
		}

		/**
		 * JTA afterCompletion callback: invoked after commit/rollback.
		 * <p>Needs to invoke SpringSessionSynchronization's beforeCompletion
		 * at this late stage, as there's no corresponding callback with JTA.
		 */
		public void afterCompletion(int status) {
			// unbind the SessionHolder from the thread
			if (this.newSession)
			{
				TransactionSynchronizationManager.unbindResource(this.sessionFactory);
			}

			// just reset the synchronizedWithTransaction flag
			Session session = this.sessionHolder.getSession();
			if (this.newSession)
			{
				closeSessionIfNecessary(session, sessionFactory);
			}
			this.sessionHolder.setSynchronizedWithTransaction(false);

		}
	}

}
