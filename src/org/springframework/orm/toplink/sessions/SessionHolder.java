/*
@license@
  */ 

package org.springframework.orm.toplink.sessions;

import oracle.toplink.sessions.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Session holder, wrapping a Toplink Session and a UnitOfWork.
 * ToplinkTransactionManager binds instances of this class
 * to the thread, for a given Session.  The SessionHolder also tracks
 * whether or not the Session has registered a callback SynchronizationListener
 * with the current PlatformTransactionManager
 *
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 15.04.2004
 */
public class SessionHolder extends ResourceHolderSupport
{
	/**
	 * The Toplink session we are holding
	 */
	private final SpringSession session;
	private boolean synchronizedWithTransaction;
	private boolean hasTransaction = false;
	
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Create a session holder for the given session
	 * @param session The session to wrap
	 */
	public SessionHolder(Session session)
	{
		this.session = (SpringSession)session;
	}

	/**
	 * Return the wrapped session
	 * @return The wrapped session
	 */
	public Session getSession()
	{
		return session;
	}

	/**
	 * After registering a SynchronizationListener for a Session, this should be set to true
	 * for it's SessionHolder
	 * 
	 * @param synchronizedWithTransaction
	 */
	public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction)
	{
		this.synchronizedWithTransaction = synchronizedWithTransaction;
	}

	/**
	 * Used by SessionFactoryUtils to determine if this SessionHolder has already registered a 
	 * SyncrhonizationListener with the current PlatformTranactionManager
	 * 
	 * @return true if a SynchronizationListener has been registered for the currently bound Session
	 */
	public boolean isSynchronizedWithTransaction()
	{
		return synchronizedWithTransaction;
	}
	
    /**
     * @return
     */
    public Object getTransaction()
    {
        return this.session.getActiveUnitOfWork();
    }

    /**
     * 
     */
    public void resetTransaction()
    {
        if (this.hasTransaction())
        {
            this.session.resetTransaction();
        }
        this.hasTransaction = false;
    }
    
    public void transactionStarted()
    {
        this.hasTransaction = true;
    }

    /**
     * @return
     */
    public boolean hasTransaction()
    {
        return this.hasTransaction;
    }
}
