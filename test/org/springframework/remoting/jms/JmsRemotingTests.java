package org.springframework.remoting.jms;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;
import org.codehaus.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import java.lang.reflect.InvocationTargetException;

/**
 * @author James Strachan
 * @version $Revision: 1.1 $
 */
public class JmsRemotingTests extends TestCase {
    private ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    private QueueConnection connection;

    public void testJmsProxyFactoryBeanAndServiceExporter() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setTemplate(createJmsTemplate());
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        QueueSession queueSession = createQueueSession();
        pfb.setSession(queueSession);
        pfb.setQueue(queueSession.createQueue(getName()));
        pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);
        assertEquals(50, proxy.getAge());

        try {
            proxy.exceptional(new IllegalStateException());
            fail("Should have thrown IllegalStateException");
        }
        catch (IllegalStateException ex) {
            // expected
        }
        try {
            proxy.exceptional(new IllegalAccessException());
            fail("Should have thrown IllegalAccessException");
        }
        catch (IllegalAccessException ex) {
            // expected
        }
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithJMSException() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setTemplate(createJmsTemplate());
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        QueueSession queueSession = createQueueSession();
        pfb.setSession(queueSession);
        pfb.setQueue(queueSession.createQueue(getName()));
        pfb.afterPropertiesSet();
        ITestBean proxy = (ITestBean) pfb.getObject();

        // lets force an exception by closing the session
        queueSession.close();
        try {
            proxy.setAge(50);
            fail("Should have thrown RemoteAccessException");
        }
        catch (RemoteAccessException ex) {
            // expected
            assertTrue(ex.getCause() instanceof JMSException);
        }
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithInvocationAttributes() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setTemplate(createJmsTemplate());
        exporter.setRemoteInvocationExecutor(new DefaultRemoteInvocationExecutor() {
            public Object invoke(RemoteInvocation invocation, Object targetObject)
                    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                assertNotNull(invocation.getAttributes());
                assertEquals(1, invocation.getAttributes().size());
                assertEquals("myValue", invocation.getAttributes().get("myKey"));
                assertEquals("myValue", invocation.getAttribute("myKey"));
                return super.invoke(invocation, targetObject);
            }
        });
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        QueueSession session = createQueueSession();
        pfb.setSession(session);
        pfb.setQueue(session.createQueue(getName()));
        pfb.setRemoteInvocationFactory(new RemoteInvocationFactory() {
            public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
                RemoteInvocation invocation = new RemoteInvocation(methodInvocation);
                invocation.addAttribute("myKey", "myValue");
                try {
                    invocation.addAttribute("myKey", "myValue");
                    fail("Should have thrown IllegalStateException");
                }
                catch (IllegalStateException ex) {
                    // expected: already defined
                }
                assertNotNull(invocation.getAttributes());
                assertEquals(1, invocation.getAttributes().size());
                assertEquals("myValue", invocation.getAttributes().get("myKey"));
                assertEquals("myValue", invocation.getAttribute("myKey"));
                return invocation;
            }
        });

        pfb.afterPropertiesSet();
        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithCustomInvocationObject() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setTemplate(createJmsTemplate());
        exporter.setRemoteInvocationExecutor(new DefaultRemoteInvocationExecutor() {
            public Object invoke(RemoteInvocation invocation, Object targetObject)
                    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                assertTrue(invocation instanceof JmsRemotingTests.TestRemoteInvocation);
                assertNull(invocation.getAttributes());
                assertNull(invocation.getAttribute("myKey"));
                return super.invoke(invocation, targetObject);
            }
        });
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        QueueSession queueSession = createQueueSession();
        pfb.setSession(queueSession);
        pfb.setQueue(queueSession.createQueue(getName()));
        pfb.setRemoteInvocationFactory(new RemoteInvocationFactory() {
            public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
                RemoteInvocation invocation = new JmsRemotingTests.TestRemoteInvocation(methodInvocation);
                assertNull(invocation.getAttributes());
                assertNull(invocation.getAttribute("myKey"));
                return invocation;
            }
        });
        pfb.afterPropertiesSet();
        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
    }

    public void testJmsInvokerWithSpecialLocalMethods() throws Exception {
        String serviceUrl = "http://myurl";
        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl(serviceUrl);

        QueueSession session = createQueueSession();
        pfb.setSession(session);
        pfb.setQueue(session.createQueue(getName()));
        pfb.afterPropertiesSet();
        ITestBean proxy = (ITestBean) pfb.getObject();

        // shouldn't go through to remote service
        assertTrue(proxy.toString().indexOf("JMS invoker") != -1);
        assertTrue(proxy.toString().indexOf(serviceUrl) != -1);
        assertEquals(proxy.hashCode(), proxy.hashCode());
        assertTrue(proxy.equals(proxy));

        // lets force an exception by closing the session
        session.close();
        try {
            proxy.setAge(50);
            fail("Should have thrown RemoteAccessException");
        }
        catch (RemoteAccessException ex) {
            // expected
            assertTrue(ex.getCause() instanceof JMSException);
        }
    }

    protected void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        connectionFactory.stop();
        super.tearDown();
    }


    protected void subscribeToQueue(JmsServiceExporter exporter, String queueName) throws JMSException {
        QueueSession serverSession = createQueueSession();
        Queue queue = serverSession.createQueue(queueName);
        MessageConsumer consumer = serverSession.createConsumer(queue);
        consumer.setMessageListener(exporter);
    }

    protected JmsTemplate createJmsTemplate() {
        JmsTemplate answer = new JmsTemplate();
        answer.setConnectionFactory(connectionFactory);
        answer.setPubSubDomain(false);
        return answer;
    }

    protected QueueSession createQueueSession() throws JMSException {
        return getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    protected QueueConnection getConnection() throws JMSException {
        if (connection == null) {
            connection = connectionFactory.createQueueConnection();
            connection.start();
        }
        return connection;
    }

    private static class TestRemoteInvocation extends RemoteInvocation {

        public TestRemoteInvocation(MethodInvocation methodInvocation) {
            super(methodInvocation);
        }

    }
}