/*
 * Created on Jul 28, 2004
 */
package org.springframework.jmx;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.AttributeList;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * @author robh
 */
public class AttributeChangeNotificationTests extends AbstractJmxTests {

    private static final String OBJECT_NAME = "bean:name=testBean1";

    public AttributeChangeNotificationTests(String name) {
        super(name);
    }

    public void testNullListener() throws Exception {

        try {
            server.addNotificationListener(getObjectName(),
                    (NotificationListener) null, (NotificationFilter) null,
                    new Object());
            fail("Should not be able to add a null listener");
        } catch (Exception ex) {
            // success!
        }
    }

    public void testDefineSingleAttribute() throws Exception {
        TestListener tl = new TestListener();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        ObjectName objectName = getObjectName();

        server.addNotificationListener(objectName, tl, filter, new Object());
        server.setAttribute(objectName, new Attribute("name", "Luke Skywalker"));

        performNotificationAsserts(tl, AttributeChangeNotification.class, 1);

        server.setAttribute(objectName, new Attribute("age", new Integer(19)));

        assertEquals("The count should not have increased", 1, tl.count);

    }

    public void testDefineAllAttributes() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");
        filter.enableAttribute("age");

        server.addNotificationListener(objectName, tl, filter, new Object());

        server.setAttribute(objectName, new Attribute("name", "Joe Schmoe"));

        performNotificationAsserts(tl, AttributeChangeNotification.class, 1);

        server.setAttribute(objectName, new Attribute("age", new Integer(19)));

        performNotificationAsserts(tl, AttributeChangeNotification.class, 2);
    }

    public void testUsingSetAttributes() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");
        filter.enableAttribute("age");

        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("name", "Rob Harrop"));
        attrs.add(new Attribute("age", new Integer(101)));

        server.addNotificationListener(objectName, tl, filter, new Object());

        server.setAttributes(objectName, attrs);

        performNotificationAsserts(tl, AttributeChangeNotification.class, 2);
    }

    public void testUsingSetAttributesWithFilter() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("name", "Rob Harrop"));
        attrs.add(new Attribute("age", new Integer(101)));

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("age");

        server.addNotificationListener(objectName, tl, filter, new Object());

        server.setAttributes(objectName, attrs);

        performNotificationAsserts(tl, AttributeChangeNotification.class, 1);

        AttributeChangeNotification nt = (AttributeChangeNotification) tl.notification;
        assertEquals("The notification was sent for the wrong attribute",
                "age", nt.getAttributeName());
    }

    public void testWithoutFilter() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        server.addNotificationListener(objectName, tl, null, null);

        server.setAttribute(objectName, new Attribute("name", "Rob Harrop"));

        assertNull("No notification should have been received", tl.notification);
    }
    
    public void testHandback() throws Exception {
        TestListener tl = new TestListener();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        ObjectName objectName = getObjectName();
        Object handBack = new Object();

        server.addNotificationListener(objectName, tl, filter, handBack);
        server.setAttribute(objectName, new Attribute("name", "Luke Skywalker"));

        assertSame("The handback object is different", tl.handback, handBack);
    }

    public void testAddThenDisable() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        server.addNotificationListener(objectName, tl, filter, null);

        server.setAttribute(objectName, new Attribute("name", "Rob Harrop"));

        performNotificationAsserts(tl, AttributeChangeNotification.class, 1);

        filter.disableAttribute("name");

        server.setAttribute(objectName, new Attribute("name", "Rob Harrop"));

        performNotificationAsserts(tl, AttributeChangeNotification.class, 1);
    }

    public void testRemoveListener() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        server.addNotificationListener(objectName, tl, filter, null);
        server.removeNotificationListener(objectName, tl);

        server.setAttribute(objectName, new Attribute("name", "Rob Harrop"));

        assertNull("No notification should have been received", tl.notification);
    }

    public void testRemoveListenerWithFilter() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        server.addNotificationListener(objectName, tl, filter, null);
        server.removeNotificationListener(objectName, tl, filter, null);

        server.setAttribute(objectName, new Attribute("name", "Rob Harrop"));

        assertNull("No notification should have been received", tl.notification);
    }

    public void testRemoveWithDifferentHandback() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        server.addNotificationListener(objectName, tl, filter, new Object());

        try {
            server.removeNotificationListener(objectName, tl, filter,
                    new Object());
            fail("Should not be able to remove a listen with an invalid handback");
        } catch (ListenerNotFoundException ex) {
            // wanted
        }

    }
    
    public void testRemoveWithDifferentFilter() throws Exception {
        TestListener tl = new TestListener();
        ObjectName objectName = getObjectName();

        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("name");

        server.addNotificationListener(objectName, tl, filter, null);

        try {
            server.removeNotificationListener(objectName, tl, new AttributeChangeNotificationFilter(),
                    null);
            fail("Should not be able to remove a listen with an invalid filter");
        } catch (ListenerNotFoundException ex) {
            // wanted
        }

    }

    private void performNotificationAsserts(TestListener listener,
            Class desiredType, int desiredNumber) {
        assertNotNull("No notification was received", listener.notification);
        assertEquals("An incorrect number of notifications was received",
                desiredNumber, listener.count);
        assertTrue("The wrong type of notification was returned",
                (desiredType == listener.notification.getClass()));
    }

    private ObjectName getObjectName() throws Exception {
        return ObjectNameManager.getInstance(OBJECT_NAME);
    }

    private class TestListener implements NotificationListener {

        public int count = 0;

        public Notification notification = null;

        public Object handback = null;

        /*
         * (non-Javadoc)
         * 
         * @see javax.management.NotificationListener#handleNotification(javax.management.Notification,
         *      java.lang.Object)
         */
        public void handleNotification(Notification notification,
                Object handback) {
            this.notification = notification;
            this.handback = handback;
            this.count++;
        }

    }
}