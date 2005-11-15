package org.springframework.jmx.export;

import org.springframework.jmx.AbstractMBeanServerTests;
import org.springframework.jmx.JmxTestBean;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rob Harrop
 */
public class NotificationListenerTests extends AbstractMBeanServerTests {

	public void testRegisterNotificationListenerForMBean() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName.toString(), bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		Map notificationListeners = new HashMap();
		notificationListeners.put(objectName.toString(), listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(notificationListeners);
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));

		assertEquals("Listener not notified", 1, listener.getCount(attributeName));
	}

	public void testRegisterNotificationListenerWithWildcard() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName.toString(), bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		Map notificationListeners = new HashMap();
		notificationListeners.put("*", listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(notificationListeners);
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));

		assertEquals("Listener not notified", 1, listener.getCount(attributeName));

	}

	public void testRegisterNotificationListenerWithHandback() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName.toString(), bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		Object handback = new Object();

		NotificationListenerBean listenerBean = new NotificationListenerBean();
		listenerBean.setNotificationListener(listener);
		listenerBean.setMappedObjectName(objectName);
		listenerBean.setHandback(handback);


		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(new NotificationListenerBean[]{listenerBean});
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));

		assertEquals("Listener not notified", 1, listener.getCount(attributeName));
		assertEquals("Handback object not transmitted correctly", handback, listener.getLastHandback(attributeName));

	}

	public void testRegisterNotificationListenerForAllMBeans() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName.toString(), bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		NotificationListenerBean listenerBean = new NotificationListenerBean();
		listenerBean.setNotificationListener(listener);


		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(new NotificationListenerBean[]{listenerBean});
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));

		assertEquals("Listener not notified", 1, listener.getCount(attributeName));

	}

	public void testRegisterNotificationListenerWithFilter() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName.toString(), bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		NotificationListenerBean listenerBean = new NotificationListenerBean();
		listenerBean.setNotificationListener(listener);
		listenerBean.setNotificationFilter(new NotificationFilter() {
			public boolean isNotificationEnabled(Notification notification) {
				if (notification instanceof AttributeChangeNotification) {
					AttributeChangeNotification changeNotification = (AttributeChangeNotification) notification;
					return "Name".equals(changeNotification.getAttributeName());
				}
				else {
					return false;
				}
			}
		});


		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(new NotificationListenerBean[]{listenerBean});
		exporter.afterPropertiesSet();

		// update the attributes
		String nameAttribute = "Name";
		String ageAttribute = "Age";

		server.setAttribute(objectName, new Attribute(nameAttribute, "Rob Harrop"));
		server.setAttribute(objectName, new Attribute(ageAttribute, new Integer(90)));

		assertEquals("Listener not notified for Name", 1, listener.getCount(nameAttribute));
		assertEquals("Listener incorrectly notified for Age", 0, listener.getCount(ageAttribute));

	}

	private static class CountingAttributeChangeNotificationListener implements NotificationListener {

		private Map attributeCounts = new HashMap();

		private Map attributeHandbacks = new HashMap();

		public void handleNotification(Notification notification, Object handback) {
			if (notification instanceof AttributeChangeNotification) {
				AttributeChangeNotification attNotification = (AttributeChangeNotification) notification;
				String attributeName = attNotification.getAttributeName();

				Integer currentCount = (Integer) this.attributeCounts.get(attributeName);

				if (currentCount != null) {
					int count = currentCount.intValue() + 1;
					this.attributeCounts.put(attributeName, new Integer(count));
				}
				else {
					this.attributeCounts.put(attributeName, new Integer(1));
				}

				this.attributeHandbacks.put(attributeName, handback);
			}
		}

		public int getCount(String attribute) {
			Integer count = (Integer) this.attributeCounts.get(attribute);
			return (count == null) ? 0 : count.intValue();
		}

		public Object getLastHandback(String attributeName) {
			return this.attributeHandbacks.get(attributeName);
		}
	}

}
