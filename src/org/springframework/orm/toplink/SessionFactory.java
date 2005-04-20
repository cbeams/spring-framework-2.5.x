/*
@license@
  */ 

package org.springframework.orm.toplink;

import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.Session;
import oracle.toplink.threetier.Server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.toplink.sessions.SpringClientSession;

/**
 * The SessionFactory is a singleton used to create the Sessions that will be
 * used by application code.
 * 
 * @author jclark
 *  
 */
public class SessionFactory
{
    private static Log logger = LogFactory.getLog(SessionFactory.class);
    private DatabaseSession session;

    /**
     * The LocalSessionFactoryBean configures either a DatabaseSession or a
     * ServerSession, logs it in, and then creates this SessionFactory Object.
     * 
     * @param session
     */
    public SessionFactory(DatabaseSession session)
    {
        this.session = session;
    }

    /**
     * Logout the underlying the DatabaseSession. This would normally only occur
     * when a Session is being hot-deployed or when the Application is shutting
     * down.
     */
    public void close()
    {
        this.session.logout();
        this.session.release();
    }

    /**
     * Create a new Spring specific TopLink Session for the current application
     * context.
     * 
     * @return
     */
    public Session createSession()
    {
        Session newSession = null;
        if (this.session.isServerSession())
        {
            logger.debug("injecting Thread-safe Session");

            newSession = new SpringClientSession(
                    ((Server)this.session),
                    ((Server)this.session).getDefaultConnectionPolicy());
        }
        else if (this.session.isDatabaseSession())
        {
            // I've left this code in here but it is impossible for this to ever get called.  SessionFactoryBean is currently not allowing
            // users to inject single-threaded DatabaseSessions
            logger.warn("injecting a single-threaded DatabaseSession.  This should only be used in a test environment.  It is not Thread safe.");
            newSession = this.session;
        }

        return newSession;
    }

}