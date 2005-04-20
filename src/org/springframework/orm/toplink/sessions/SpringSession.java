/*
@license@
  */ 
package org.springframework.orm.toplink.sessions;

import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

/**
 * @author jclark
 */
public interface SpringSession extends Session
{
    UnitOfWork getTransaction();
    boolean hasTransaction();
    void resetTransaction();
    
    public boolean isTransactionSynchronized();
    public void setTransactionSynchronized();
}
