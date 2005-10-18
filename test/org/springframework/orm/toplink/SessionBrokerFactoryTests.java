package org.springframework.orm.toplink;

import junit.framework.TestCase;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.publicinterface.UnitOfWork;
import oracle.toplink.sessionbroker.SessionBroker;
import oracle.toplink.sessions.Project;
import oracle.toplink.sessions.Session;

public class SessionBrokerFactoryTests extends TestCase
{
    /*
     * When acquiring ClientSessionBrokers, the SessionBroker can throw RuntimeExceptions indicating
     * that this SessionBroker is not capable of creating "client" Sessions.  We need to handle 
     * these differently depending on how the SessionFactory is being used.  If we are creating a plain
     * Session that we can return the original SessionBroker.   
     *
     */
    public void testSessionBrokerThrowingValidationExceptions()
    {
        SessionBroker broker = new MockSingleSessionBroker();
        SessionBrokerSessionFactory factory = new SessionBrokerSessionFactory(broker);
        
        assertEquals(factory.createSession(),broker);
        try
        {
            factory.createManagedClientSession();
            fail();
        }
        catch(UnsupportedOperationException e)
        {
            // success
        }
        try
        {
            factory.createTransactionAwareSession();
            fail();
        }
        catch(UnsupportedOperationException e)
        {
            // success
        }
    }
    
    public void testManagedSessionBroker()
    {
        SessionBroker client = new MockClientSessionBroker();
        SessionBroker broker = new MockServerSessionBroker(client);
        SessionBrokerSessionFactory factory = new SessionBrokerSessionFactory(broker);
        
        assertEquals(client,factory.createSession());
        
        Session session = factory.createManagedClientSession();
        assertEquals(client,session.getActiveSession());
        assertEquals(session.getActiveUnitOfWork(),session.getActiveUnitOfWork());
    }
    
    private class MockSingleSessionBroker extends SessionBroker
    {
        public MockSingleSessionBroker()
        {
            super(new Project());
        }

        public SessionBroker acquireClientSessionBroker()
        {
            throw new ValidationException();
        }
    }
    
    private class MockServerSessionBroker extends SessionBroker
    {
        private SessionBroker client;
        public MockServerSessionBroker(SessionBroker client)
        {
            super(new Project());
            this.client = client;
        }
        
        public SessionBroker acquireClientSessionBroker()
        {
            return client;
        }
    }
    
    private class MockClientSessionBroker extends SessionBroker
    {
        public MockClientSessionBroker()
        {
            super(new Project());
        }

        public UnitOfWork acquireUnitOfWork()
        {
            return new UnitOfWork();
        }
    }
}
