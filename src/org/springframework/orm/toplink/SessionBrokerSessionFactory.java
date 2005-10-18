package org.springframework.orm.toplink;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.sessionbroker.SessionBroker;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

/**
 * Spring SessionFactory implementation allowing users to 
 * inject a TopLink Session built from a TopLink SessionBroker.  
 * 
 * SessionBrokers are used identically to any other TopLink Session.  DAO code 
 * should never have to distinguish between Sessions which broker requests to 
 * multiple databases and Sessions which manage requests to a single database.
 * 
 * The only pertinent difference in the SessionBroker api involves the method
 * for obtaining a thread-safe "client" Session from the SessionBroker.  
 * Instead of the typical acquireClientSession
 * method, this SessionFactory implementation uses the 
 * acquireClientSessionBroker method.
 * If a SessionBroker aggregates non thread-safe DatabaseSessions, 
 * the factory will throw UnsupportedOperationExceptions
 * if used to create managed or transaction-aware Sessions.
 * 
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 1.3
 * @see org.springframework.orm.toplink.ServerSessionFactory
 * @see oracle.toplink.sessions.Session#acquireClientSession()
 * @see oracle.toplink.sessionbroker.SessionBroker#acquireClientSessionBroker()
 */
public class SessionBrokerSessionFactory implements SessionFactory
{
    private final SessionBroker sessionBroker;
     
    public SessionBrokerSessionFactory(SessionBroker broker)
    {
        this.sessionBroker = broker;
    }
    
    public Session createSession() throws TopLinkException
    {
        try
        {
            return this.sessionBroker.acquireClientSessionBroker();
        }
        catch(ValidationException e)
        {
            // thrown when SessionBroker is not built from thread-safe ServerSessions
            // allow users to access the single-threaded Session but throw UnsupportedOperationExceptions
            // if creating ManagedClientSessions or Transaction-aware Sessions
            return this.sessionBroker;
        }
    }

    public Session createManagedClientSession() throws TopLinkException
    {
        try
        {
            return (Session) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[] {Session.class},
                new ManagedClientSessionBrokerInvocationHandler(this.sessionBroker.acquireClientSessionBroker()));
        }
        catch(ValidationException e)
        {
            // SessionBroker contains either single-threaded DatabaseSessions or
            // Sessions configured to use an isolated cache
            throw new UnsupportedOperationException("SessionBrokerSessionFactory does not support managed client Sessions for SessionBrokers not composed only of ServerSessions");
        }
    }

    public Session createTransactionAwareSession() throws TopLinkException
    {
        try
        {
            return (Session) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[] {Session.class},
                new TransactionAwareInvocationHandler(this, this.sessionBroker.acquireClientSessionBroker()));
        }
        catch(ValidationException e)
        {
            // SessionBroker contains either single-threaded DatabaseSessions or
            // Sessions configured to use an isolated cache
            throw new UnsupportedOperationException("SessionBrokerSessionFactory does not support managed client Sessions for SessionBrokers not composed only of ServerSessions");
        }
    }

    public void close()
    {
        this.sessionBroker.logout();
        this.sessionBroker.release();
    }
    
    /**
     * Invocation handler that decorates a ClientSessionBroker with an "active" 
     * UnitOfWork.  For use in situations where a Spring TransactionManager requires
     * a "managed" thread-safe TopLink Session.
     */
    private static class ManagedClientSessionBrokerInvocationHandler implements InvocationHandler
    {
        private final Session clientSessionBroker;
        private final UnitOfWork uow;
        
        public ManagedClientSessionBrokerInvocationHandler(Session broker)
        {
            this.clientSessionBroker  = broker;
            this.uow = this.clientSessionBroker.acquireUnitOfWork();
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().equals("getActiveSession"))
            {
                return this.clientSessionBroker;
            }
            else if (method.getName().equals("getActiveUnitOfWork"))
            {
                return this.uow;
            }
            else if (method.getName().equals("release"))
            {
                this.uow.release();
                this.clientSessionBroker.release();
            }
            else if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
            }
            else if (method.getName().equals("hashCode")) {
                // Use hashCode of SessionFactory proxy.
                return new Integer(hashCode());
            }

            // Invoke method on target SessionFactory.
            try {
                return method.invoke(this.clientSessionBroker, args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
    
    /**
     * Invocation handler that delegates <code>getActiveSession</code> calls
     * to SessionFactoryUtils, for being aware of thread-bound transactions.
     */
    private static class TransactionAwareInvocationHandler implements InvocationHandler {

        private final SessionFactory sessionFactory;

        private final Session target;

        public TransactionAwareInvocationHandler(SessionFactory sessionFactory, Session target) {
            this.sessionFactory = sessionFactory;
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Invocation on Session interface coming in...

            if (method.getName().equals("getActiveSession")) {
                // Handle getActiveSession method: return transactional Session, if any.
                try {
                    return SessionFactoryUtils.doGetSession(this.sessionFactory, false);
                }
                catch (IllegalStateException ex) {
                    // getActiveSession is supposed to return the Session itself if no active one found.
                    return this.target;
                }
            }
            else if (method.getName().equals("getActiveUnitOfWork")) {
                // Handle getActiveUnitOfWork method: return transactional UnitOfWork, if any.
                try {
                    return SessionFactoryUtils.doGetSession(this.sessionFactory, false).getActiveUnitOfWork();
                }
                catch (IllegalStateException ex) {
                    // getActiveUnitOfWork is supposed to return null if no active one found.
                    return null;
                }
            }
            else if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
            }
            else if (method.getName().equals("hashCode")) {
                // Use hashCode of SessionFactory proxy.
                return new Integer(hashCode());
            }

            // Invoke method on target SessionFactory.
            try {
                return method.invoke(this.target, args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


}
