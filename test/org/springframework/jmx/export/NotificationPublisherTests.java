package org.springframework.jmx.export;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.AbstractMBeanServerTests;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;
import org.springframework.jmx.support.ObjectNameManager;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

/**
 * @author Rob Harrop
 */
public class NotificationPublisherTests extends AbstractMBeanServerTests {

	private CountingNotificationListener listener = new CountingNotificationListener();

	public void testSimpleBean() throws Exception {
		// start the MBeanExporter
		ApplicationContext ctx = new ClassPathXmlApplicationContext("org/springframework/jmx/export/notificationPublisherTests.xml");

		// register test listener
		registerListener();

		// test
		MyNotificationPublisher publisher = (MyNotificationPublisher) ctx.getBean("publisher");
		assertNotNull("NotificationPublisher should not be null", publisher.getNotificationPublisher());
		publisher.sendNotification();
		assertEquals("Notification not sent", 1, listener.count);
	}

	public void testLazyInit() throws Exception {
		// start the MBeanExporter
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("org/springframework/jmx/export/notificationPublisherLazyTests.xml");

		assertFalse("Should not have instantiated the bean yet", ctx.getBeanFactory().containsSingleton("publisher"));

		// need touch the MBean proxy
		server.getAttribute(getObjectName(), "Name");

		// register test listener
		registerListener();

		// test
		MyNotificationPublisher publisher = (MyNotificationPublisher) ctx.getBean("publisher");
		assertNotNull("NotificationPublisher should not be null", publisher.getNotificationPublisher());
		publisher.sendNotification();
		assertEquals("Notification not sent", 1, listener.count);
	}

	private void registerListener() throws Exception {
		this.server.addNotificationListener(getObjectName(), listener, null, null);
	}

	private ObjectName getObjectName() throws MalformedObjectNameException {
		return ObjectNameManager.getInstance("spring:type=Publisher");
	}

	private static class CountingNotificationListener implements NotificationListener {

		private int count;

		private Notification lastNotification;

		public void handleNotification(Notification notification, Object handback) {
			this.lastNotification = notification;
			this.count++;
		}

		public int getCount() {
			return count;
		}

		public Notification getLastNotification() {
			return lastNotification;
		}
	}

	public static class MyNotificationPublisher implements NotificationPublisherAware {

		private NotificationPublisher notificationPublisher;

		public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
			this.notificationPublisher = notificationPublisher;
		}

		public NotificationPublisher getNotificationPublisher() {
			return notificationPublisher;
		}

		public void sendNotification() {
			this.notificationPublisher.sendNotification(new Notification("test", this, 1));
		}

		public String getName() {
			return "Rob Harrop";
		}
	}
}
