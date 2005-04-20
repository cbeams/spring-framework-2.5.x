/*
@license@
  */ 
package org.springframework.orm.toplink.sessions;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.sessions.UnitOfWork;
import oracle.toplink.threetier.ClientSession;
import oracle.toplink.threetier.ConnectionPolicy;
import oracle.toplink.threetier.Server;
import oracle.toplink.threetier.ServerSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * This ClientSession insures that parallel UnitOfWork Objects are not used
 * in a single Thread/Transactional context.
 *
 *	<p>
 * In the Spring integration, TopLink Sessions become synchronized with 
 * a Thread so that they can be appropriately shared by the different
 * beans from the DataAccess layer.  This implementation of 
 * ClientSession insures that UnitOfWork Objects stay bound to the
 * current Transactional context (even when not using a TopLink 
 * ExternalTransactionController)
 * 
 * <p>
 * The most important difference is that <em>getActiveUnitOfWork</em>,
 * and <em>aquireUnitOfWork</em> now behave identically 
 * (whether there is an ExternalTransactionController or not).
 * 
 * <p>
 * Currently, we do not have any support for custom ConnectionPolicy
 * Objects.  We could add this by implementating a 
 * SessionFactoryWithConnectionPolicy bean factory that users could
 * configure in the Spring context.  This would allow users to inject
 * TopLink Sessions that use a specific ConnectionPolicy, rather than
 * the default one used here. 
 *  
 * @author jclark
 *
 */
public class SpringClientSession extends ClientSession implements SpringSession
{
    public void resetTransaction()
    {
        this.unitOfWork = null;
        this.hasTransaction = false;
    }
    private static Log logger = LogFactory.getLog(SpringClientSession.class);
    protected boolean hasTransaction = false;
    protected UnitOfWork unitOfWork = null;
    protected boolean isSynchronized;

    public UnitOfWork getTransaction()
    {
        if(this.hasTransaction)
        {
            return this.unitOfWork;
        }
        else
        {
            if (this.hasExternalTransactionController())
            {
                logger.debug("detected an ExternalTransactionController.  TopLink will manage mapping between UnitOfWork and UserTransaction.");
                this.unitOfWork = super.getActiveUnitOfWork();
                if (this.unitOfWork==null)
                {
                    logger.debug("UnitOfWork not current bound to transaction.  Acquire new synchronized UnitOfWork.");
                    this.unitOfWork = super.acquireUnitOfWork();
                }
            }
            else
            {
                logger.debug("acquireUnitOfWork(not managed by TopLink ExternalTransactionController.  Must be managed exclusively by Spring PlatformTransactionManager");
                this.unitOfWork = super.acquireUnitOfWork();
            }
            
            this.hasTransaction = true;
            if (this.isSynchronized)
            {
                ((oracle.toplink.publicinterface.UnitOfWork)this.unitOfWork).setSynchronized();
            }
            return this.unitOfWork;
        }
    }

    public boolean hasTransaction()
    {
        return this.hasTransaction;
    }

    public oracle.toplink.publicinterface.UnitOfWork acquireUnitOfWork()
    {
        return (oracle.toplink.publicinterface.UnitOfWork)this.getTransaction();
    }

    public UnitOfWork getActiveUnitOfWork()
    {
        return this.getTransaction();
    }
    
    /**
     * @param server
     * @param defaultConnectionPolicy
     */
    public SpringClientSession(Server server, ConnectionPolicy defaultConnectionPolicy)
    {
        super((ServerSession)server,defaultConnectionPolicy);
    }

    public void release() throws DatabaseException
    {
        super.release();
        if (this.hasTransaction)
        {
            this.hasTransaction = false;
            this.unitOfWork.release();
            this.unitOfWork=null;
        }
    }

    public boolean isTransactionSynchronized()
    {
        return isSynchronized;
    }

    public void setTransactionSynchronized()
    {
        this.isSynchronized = true;
        if (this.hasTransaction && !((oracle.toplink.publicinterface.UnitOfWork)this.unitOfWork).isSynchronized())
        {
            ((oracle.toplink.publicinterface.UnitOfWork)this.unitOfWork).setSynchronized();
        }
    }
}
