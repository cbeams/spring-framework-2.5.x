package org.springframework.jmx.export;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.Assert;

import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * Helper class that aggregates a {@link javax.management.NotificationListener},
 * a {@link javax.management.NotificationFilter}, and an arbitrary handback
 * object.
 * <p/>
 * Also provides support for associating the encapsulated
 * {@link javax.management.NotificationListener} with any number of
 * MBeans from which it wishes to receive
 * {@link javax.management.Notification Notifications} via the
 * {@link #setMappedObjectNames mappedObjectNames} property.
 *
 * @author Rob Harrop
 */
public class NotificationListenerBean implements InitializingBean {

	private NotificationListener notificationListener;

	private NotificationFilter notificationFilter;

	private Object handback;

	private ObjectName[] mappedObjectNames;


	/**
	 * Creates a new instance of the {@link NotificationListenerBean} class.
	 */
	public NotificationListenerBean() {
	}

	/**
	 * Creates a new instance of the {@link NotificationListenerBean} class.
	 *
	 * @param notificationListener the encapsulated listener
	 */
	public NotificationListenerBean(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}


	/**
	 * Gets the {@link javax.management.NotificationListener}.
	 * 
	 * @return said {@link javax.management.NotificationListener}
	 */
	public NotificationListener getNotificationListener() {
		return notificationListener;
	}

	/**
	 * Sets the {@link javax.management.NotificationListener}.
	 * 
	 * @param notificationListener said {@link javax.management.NotificationListener}
	 */
	public void setNotificationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	/**
	 * Gets the {@link javax.management.NotificationFilter} associated
	 * with the encapsulated {@link #getNotificationFilter() NotificationFilter}.
	 * <p>
	 * May be <code>null</code>.
	 * 
	 * @return said {@link javax.management.NotificationFilter}
	 */
	public NotificationFilter getNotificationFilter() {
		return notificationFilter;
	}

	/**
	 * Sets the {@link javax.management.NotificationFilter} associated
	 * with the encapsulated {@link #getNotificationFilter() NotificationFilter}.
	 * <p>
	 * May be <code>null</code>.
	 * 
	 * @param notificationFilter said {@link javax.management.NotificationFilter}
	 */
	public void setNotificationFilter(NotificationFilter notificationFilter) {
		this.notificationFilter = notificationFilter;
	}

	/**
	 * Gets the (arbitrary) object that will be 'handed back' as-is by an
	 * {@link javax.management.NotificationBroadcaster} when notifying
	 * any {@link javax.management.NotificationListener}.
	 *
	 * @return the handback object
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, Object)
	 */
	public Object getHandback() {
		return handback;
	}

	/**
	 * Sets the (arbitrary) object that will be 'handed back' as-is by an
	 * {@link javax.management.NotificationBroadcaster} when notifying
	 * any {@link javax.management.NotificationListener}.
	 * <p>
	 * May be <code>null</code>.
	 *
	 * @param handback the handback object.
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, Object)
	 */
	public void setHandback(Object handback) {
		this.handback = handback;
	}

	/**
	 * Gets the list of {@link javax.management.ObjectName ObjectNames} for which
	 * the encapsulated {@link #getNotificationFilter() NotificationFilter} will
	 * be registered as a listener for
	 * {@link javax.management.Notification Notifications}.
	 *
	 * @return said list of {@link javax.management.ObjectName ObjectNames}
	 */
	public ObjectName[] getMappedObjectNames() {
		return mappedObjectNames;
	}

	/**
	 * Set the {@link javax.management.ObjectName} of the single MBean
	 * that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * will be registered with to listen for 
	 * {@link javax.management.Notification Notifications}.
	 * 
	 * @param mappedObjectName the {@link javax.management.ObjectName} identifying the
	 * target MBean that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * is to be registered with
	 * @throws IllegalArgumentException if the supplied {@link javax.management.ObjectName} is <code>null</code>
	 */
	public void setMappedObjectName(ObjectName mappedObjectName) {
		Assert.notNull(mappedObjectName);
		setMappedObjectNames(new ObjectName[]{mappedObjectName});
	}

	/**
	 * Sets the {@link javax.management.ObjectName ObjectNames} of the MBeans
	 * that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * will be registered with to listen for 
	 * {@link javax.management.Notification Notifications}.
	 * 
	 * @param mappedObjectNames the {@link javax.management.ObjectName ObjectName} identifying the
	 * target MBeans that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * is to be registered with
	 * @throws IllegalArgumentException if the supplied <code>mappedObjectNames</code> is a
	 * <code>null</code> or zero-length array
	 */
	public void setMappedObjectNames(ObjectName[] mappedObjectNames) {
		Assert.notEmpty(mappedObjectNames, "Property [mappedObjectNames] cannot be null or empty.");
		this.mappedObjectNames = mappedObjectNames;
	}

	/**
	 * Sets the {@link java.lang.String} array of
	 * {@link javax.management.ObjectName ObjectNames} of the MBeans
	 * that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * will be registered with to listen for 
	 * {@link javax.management.Notification Notifications}.
	 * 
	 * @param mappedObjectNames the {@link java.lang.String} array of
	 * {@link javax.management.ObjectName ObjectNames} identifying the
	 * target MBeans that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * is to be registered with
	 * @throws IllegalArgumentException if the supplied <code>mappedObjectNames</code> is a
	 * <code>null</code> or zero-length array
	 */
	public void setMappedObjectNames(String[] mappedObjectNames) throws MalformedObjectNameException {
		Assert.notEmpty(mappedObjectNames, "Property [mappedObjectNames] cannot be null or empty.");
		ObjectName[] objectNames = new ObjectName[mappedObjectNames.length];
		for (int i = 0; i < mappedObjectNames.length; i++) {
			String name = mappedObjectNames[i];
			Assert.notNull(name, "A mapped [ObjectName] string cannot be null");
			objectNames[i] = ObjectNameManager.getInstance(name);
		}
		this.mappedObjectNames = objectNames;
	}

	/**
	 * Checks that this {@link NotificationListenerBean} has been
	 * correctly configured.
	 *
	 * @throws IllegalArgumentException if a {@link javax.management.NotificationFilter}
	 * has not been supplied (either via the
	 * {@link NotificationListenerBean(NotificationListener) constructor} or the
	 * {@link #setNotificationListener(javax.management.NotificationListener) setter property})
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.notificationListener, "Property [notificationListener] is required.");
	}

}
