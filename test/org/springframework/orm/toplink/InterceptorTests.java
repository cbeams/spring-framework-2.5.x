/*
 * Created on Mar 20, 2005
 *
 */
package org.springframework.orm.toplink;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.orm.toplink.SessionFactory;
import org.springframework.orm.toplink.TopLinkInterceptor;
import org.springframework.orm.toplink.mock.MockSessionFactory;
import org.springframework.orm.toplink.sessions.SpringSession;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * @author jclark
 *
 */
public class InterceptorTests extends TestCase
{

    public void testInterceptorWithNoSessionBoundAndNoSynchronizations()
    {
        MockControl sessionControl = MockControl.createControl(SpringSession.class);
        SpringSession session = (SpringSession)sessionControl.getMock();
        MockControl methodInvocationControl = MockControl.createControl(MethodInvocation.class);
        MethodInvocation methodInvocation = (MethodInvocation)methodInvocationControl.getMock();

        SessionFactory factory = new MockSessionFactory(session);
        
        TopLinkInterceptor interceptor = new TopLinkInterceptor();
        interceptor.setSessionFactory(factory);

        session.hasExternalTransactionController();
        sessionControl.setReturnValue(false,1);
        try
        {
            methodInvocation.proceed();
        }
        catch(Throwable e)
        {
            fail();
        }
        methodInvocationControl.setReturnValue(null,1);
        session.release();
        sessionControl.setVoidCallable(1);
        
        methodInvocationControl.replay();
        sessionControl.replay();
        
        try
        {
            interceptor.invoke(methodInvocation);
        }
        catch(Throwable t)
        {
            System.out.println(t);
            t.printStackTrace();
            fail();
        }

        assertFalse(TransactionSynchronizationManager.hasResource(factory));
        
        sessionControl.verify();
        methodInvocationControl.verify();
        sessionControl.verify();
    }

    public void testInterceptorWithNoSessionBoundAndSynchronizationsActive()
    {
        MockControl sessionControl = MockControl.createControl(SpringSession.class);
        SpringSession session = (SpringSession)sessionControl.getMock();
        MockControl methodInvocationControl = MockControl.createControl(MethodInvocation.class);
        MethodInvocation methodInvocation = (MethodInvocation)methodInvocationControl.getMock();

        SessionFactory factory = new MockSessionFactory(session);
        
        TopLinkInterceptor interceptor = new TopLinkInterceptor();
        interceptor.setSessionFactory(factory);
        
        try
        {
            methodInvocation.proceed();
        }
        catch(Throwable e)
        {
            fail();
        }
        methodInvocationControl.setReturnValue(null,1);
        
        methodInvocationControl.replay();
        sessionControl.replay();

       TransactionSynchronizationManager.initSynchronization();
        try
        {
            interceptor.invoke(methodInvocation);
        }
        catch(Throwable t)
        {
            fail();
        }

        assertTrue(TransactionSynchronizationManager.hasResource(factory));
        assertTrue(TransactionSynchronizationManager.getSynchronizations().size()==1);
        
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.unbindResource(factory);
        
        sessionControl.verify();
        methodInvocationControl.verify();
    }

}
