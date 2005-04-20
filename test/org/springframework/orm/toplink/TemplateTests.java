/*
 * Created on Mar 20, 2005
 *
 */
package org.springframework.orm.toplink;

import java.sql.SQLException;

import junit.framework.TestCase;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;

import org.easymock.MockControl;
import org.springframework.orm.toplink.SessionFactory;
import org.springframework.orm.toplink.TopLinkCallback;
import org.springframework.orm.toplink.TopLinkTemplate;
import org.springframework.orm.toplink.mock.MockSessionFactory;
import org.springframework.orm.toplink.sessions.SessionHolder;
import org.springframework.orm.toplink.sessions.SpringSession;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * @author jclark
 *
 */
public class TemplateTests extends TestCase
{
    public void testTemplateNotAllowingCreate()
    {
        MockControl sessionControl = MockControl.createControl(SpringSession.class);
        SpringSession session = (SpringSession)sessionControl.getMock();
 
        SessionFactory factory = new MockSessionFactory(session);
 
        TopLinkTemplate template = new TopLinkTemplate();
        template.setAllowCreate(false);
        template.setSessionFactory(factory);
        try
        {
            template.execute(new TopLinkCallback(){
                public Object doInToplink(Session session) throws TopLinkException, SQLException
                {
                    return null;
                }}
            );
            fail();
        }
        catch(Exception e)
        {
        }
    }
    
    public void testTemplateWithCreate()
    {
        MockControl sessionControl = MockControl.createControl(SpringSession.class);
        SpringSession session = (SpringSession)sessionControl.getMock();
 
        SessionFactory factory = new MockSessionFactory(session);
        
        session.hasExternalTransactionController();
        sessionControl.setReturnValue(false,1);
        session.release();
        sessionControl.setVoidCallable(1);
        
        sessionControl.replay();

        TopLinkTemplate template = new TopLinkTemplate();
        template.setAllowCreate(true);
        template.setSessionFactory(factory);
        template.execute(new TopLinkCallback(){
            public Object doInToplink(Session session) throws TopLinkException, SQLException
            {
                assertTrue(session!=null);
                return null;
            }}
        );
        assertFalse(TransactionSynchronizationManager.hasResource(factory));
        
        sessionControl.verify();
    }
    
    public void testTemplateWithExistingSessionAndNoCreate()
    {
        MockControl sessionControl = MockControl.createControl(SpringSession.class);
        SpringSession session = (SpringSession)sessionControl.getMock();
 
        SessionFactory factory = new MockSessionFactory(session);
        
        sessionControl.replay();

        SessionHolder sessionHolder = new SessionHolder(factory.createSession());
        TransactionSynchronizationManager.bindResource(factory,sessionHolder);
        
        TopLinkTemplate template = new TopLinkTemplate();
        template.setAllowCreate(false);
        template.setSessionFactory(factory);
        template.execute(new TopLinkCallback(){
            public Object doInToplink(Session session) throws TopLinkException, SQLException
            {
                assertTrue(session!=null);
                return null;
            }}
        );
        assertTrue(TransactionSynchronizationManager.hasResource(factory));
        sessionControl.verify();
        TransactionSynchronizationManager.unbindResource(factory);
    }
    
    public void testTemplateWithExistingSessionAndCreateAllowed()
    {
        MockControl sessionControl = MockControl.createControl(SpringSession.class);
        SpringSession session = (SpringSession)sessionControl.getMock();
 
        SessionFactory factory = new MockSessionFactory(session);

        sessionControl.replay();

        SessionHolder sessionHolder = new SessionHolder(factory.createSession());
        TransactionSynchronizationManager.bindResource(factory,sessionHolder);
        
        TopLinkTemplate template = new TopLinkTemplate();
        template.setAllowCreate(true);
        template.setSessionFactory(factory);
        template.execute(new TopLinkCallback(){
            public Object doInToplink(Session session) throws TopLinkException, SQLException
            {
                assertTrue(session!=null);
                return null;
            }}
        );
        assertTrue(TransactionSynchronizationManager.hasResource(factory));
        sessionControl.verify();
        TransactionSynchronizationManager.unbindResource(factory);
    }
}
