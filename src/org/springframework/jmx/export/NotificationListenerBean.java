package org.springframework.jmx.export;

import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.FatalBeanException;

import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * @author Rob Harrop
 */
public class NotificationListenerBean implements InitializingBean {

	private NotificationListener notificationListener;

	private NotificationFilter notificationFilter;

	private Object handback;

	private ObjectName[] mappedObjectNames;

	public NotificationListenerBean() {
	}

	public NotificationListenerBean(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	public NotificationListener getNotificationListener() {
		return notificationListener;
	}

	public void setNotificationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	public NotificationFilter getNotificationFilter() {
		return notificationFilter;
	}

	public void setNotificationFilter(NotificationFilter notificationFilter) {
		this.notificationFilter = notificationFilter;
	}

	public Object getHandback() {
		return handback;
	}

	public void setHandback(Object handback) {
		this.handback = handback;
	}

	public ObjectName[] getMappedObjectNames() {
		return mappedObjectNames;
	}

	public void setMappedObjectName(ObjectName objectName) {
		setMappedObjectNames(new ObjectName[]{objectName});
	}

	public void setMappedObjectNames(ObjectName[] mappedObjectNames) {
		Assert.notEmpty(mappedObjectNames, "Property [mappedObjectNames] cannot be null or empty.");
		this.mappedObjectNames = mappedObjectNames;
	}

	public void setMappedObjectNames(String[] mappedObjectNames) throws MalformedObjectNameException {
		Assert.notEmpty(mappedObjectNames, "Property [mappedObjectNames] cannot be null or empty.");
		ObjectName[] objectNames = new ObjectName[mappedObjectNames.length];
		for (int i = 0; i < mappedObjectNames.length; i++) {
			String name = mappedObjectNames[i];
			objectNames[i] = ObjectNameManager.getInstance(name);
		}
		this.mappedObjectNames = objectNames;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.notificationListener, "Property [notificationListener] is required.");
	}
}
