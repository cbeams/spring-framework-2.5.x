package org.springframework.jms.support.destination;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.test.AssertThrows;

import javax.jms.ConnectionFactory;

/**
 * Unit tests for the {@link JmsDestinationAccessor} class.
 *
 * @author Rick Evans
 */
public final class JmsDestinationAccessorTests extends TestCase {

    public void testChokesIfDestinationResolverIsetToNullExplcitly() throws Exception {
        MockControl mockConnectionFactory = MockControl.createControl(ConnectionFactory.class);
        final ConnectionFactory connectionFactory = (ConnectionFactory) mockConnectionFactory.getMock();
        mockConnectionFactory.replay();

        new AssertThrows(IllegalArgumentException.class) {
            public void test() throws Exception {
                JmsDestinationAccessor accessor = new StubJmsDestinationAccessor();
                accessor.setConnectionFactory(connectionFactory);
                accessor.setDestinationResolver(null);
                accessor.afterPropertiesSet();
            }
        }.runTest();

        mockConnectionFactory.verify();
    }

    public void testSessionTransactedModeReallyDoesDefaultToFalse() throws Exception {
        JmsDestinationAccessor accessor = new StubJmsDestinationAccessor();
        assertFalse("The [pubSubDomain] property of JmsDestinationAccessor must default to " +
                "false (i.e. Queues are used by default). Change this test (and the " +
                "attendant Javadoc) if you have changed the default.",
                accessor.isPubSubDomain());
    }

    private static class StubJmsDestinationAccessor extends JmsDestinationAccessor {
    }
}
