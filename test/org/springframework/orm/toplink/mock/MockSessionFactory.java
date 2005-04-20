/*
 * Created on Mar 18, 2005
 *
 */
package org.springframework.orm.toplink.mock;

import oracle.toplink.sessions.Session;

import org.springframework.orm.toplink.SessionFactory;

/**
 * @author jclark
 *
 */
public class MockSessionFactory extends SessionFactory
{
    private Session session;
    public MockSessionFactory(Session session)
    {
        super(null);
        this.session = session;
    }
    public Session createSession()
    {
        return this.session;
    }
    public void setSession(Session session)
    {
        this.session = session;
    }
}
